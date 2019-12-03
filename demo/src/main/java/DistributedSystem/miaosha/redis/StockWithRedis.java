package DistributedSystem.miaosha.redis;

import DistributedSystem.miaosha.pojo.Stock;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Transaction;

import javax.xml.stream.FactoryConfigurationError;
import java.util.List;

@Slf4j
public class StockWithRedis {

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


    public static boolean updateStockWithRedis(Stock stock) throws Exception {
        JedisCluster jedis = null;
        try {
            Integer id = stock.getId();
            jedis = RedisPool.getJedis();
            //Transaction transaction = jedis.multi();
            //TODO Jedis Cluster 不支持事务 可以考虑加锁
            //开始事务
            jedis.decr(STOCK_COUNT +  id);
            jedis.incr(STOCK_SALE +  id);
            jedis.incr(STOCK_VERSION +  id);
            //transaction.exec();
            return true;
        } catch (Exception e) {
            System.out.printf("updateStock fail %s ", e);
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 重置缓存 缓存预热
     */
    public static void initRedisBefore() throws Exception {
       JedisCluster jedis = null;
        try {
            jedis = RedisPool.getJedis();
            // 开始事务
            //Transaction transaction = jedis.multi();

            jedis.set(STOCK_COUNT + 1, "10");
            jedis.set(STOCK_SALE + 1, "0");
            jedis.set(STOCK_VERSION + 1, "0");
            // 结束事务
            //List<Object> list = transaction.exec();
        } catch (Exception e) {
            System.out.println("initRedis 获取 Jedis 实例失败："+ e);
        } finally {
        }
    }



}
