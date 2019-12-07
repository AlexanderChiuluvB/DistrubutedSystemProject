package DistributedSystem.miaosha.redis;

import DistributedSystem.miaosha.pojo.Stock;
import lombok.extern.slf4j.Slf4j;

import redis.clients.jedis.JedisCluster;

import static DistributedSystem.miaosha.redis.RedisPool.initCluster;

@Slf4j
public class StockWithRedis {

    private final static String STOCK_LOCK_KEY = "stock_lock_key_";

    private final static String STOCK_LOCK_VALUE = "stock_lock_value_";

    /**
     * 库存值
     */
    public final static String STOCK_COUNT = "stock_count_";

    /**
     * 销售值
     */
    public final static String STOCK_SALE = "stock_sale_";

    /**
     * 版本号
     */
    public final static String STOCK_VERSION = "stock_version_";


    public static boolean updateStockWithRedis(Integer id) throws Exception {

        try {
            //Integer id = stock.getId();
            JedisCluster jedis = initCluster();
            //Transaction transaction = jedis.multi();
            //TODO Jedis Cluster 不支持事务 可以考虑加锁
            //开始事务
            if (RedisPool.tryLock(STOCK_LOCK_KEY + id, STOCK_LOCK_VALUE + id, 1000 * 100, 1000 * 20)) {
                try {
                    System.out.println("执行Redis事务");
                    jedis.decr(STOCK_COUNT + id);
                    jedis.incr(STOCK_SALE + id);
                    jedis.incr(STOCK_VERSION + id);
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    System.out.println("Redis事务结束");
                }
                if (!RedisPool.tryUnLock(jedis, STOCK_LOCK_KEY + id, STOCK_LOCK_VALUE + id)) {
                    throw new RuntimeException("UNLOCK FAILED");
                }

                return true;
            }
            //transaction.exec();
        } catch (Exception e) {
            System.out.printf("updateStock fail %s ", e);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 重置缓存 缓存预热
     */
    public static int initRedisBefore(int id, int count) throws Exception {
        try {
            JedisCluster jedis = initCluster();
            // 开始事务
            // TODO Redis集群不支持事务
            //Transaction transaction = jedis.multi();
            jedis.set(STOCK_COUNT + id, String.valueOf(count));
            jedis.set(STOCK_SALE + id, "0");
            jedis.set(STOCK_VERSION + id, "0");
            //jedis.del(STOCK_LOCK_KEY + id, STOCK_LOCK_VALUE + id);
            // 结束事务
            //List<Object> list = transaction.exec();
            return 1;
        } catch (Exception e) {
            System.out.println("initRedis 获取 Jedis 实例失败：" + e);
            return 0;
        }
    }


}
