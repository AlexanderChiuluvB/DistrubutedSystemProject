package redis;

import lombok.extern.slf4j.Slf4j;
import pojo.Stock;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

@Slf4j
public class StockWithRedis {

    /**
     * 库存值
     */
    public final static String STOCK_COUNT = "stock_count_";

    /**
     * 库存值
     */
    public final static String STOCK_SALE = "stock_sale_";

    /**
     * 库存值
     */
    public final static String STOCK_VERSION = "stock_version_";


    public static void updateStockWithRedis(Stock stock) {
        Jedis jedis = null;
        try {
            jedis = RedisPool.getJedis();
            Transaction transaction = jedis.multi();
            //开始事物
            RedisPool.decr(STOCK_COUNT + stock.getCount());
            RedisPool.incr(STOCK_SALE + stock.getCount());
            RedisPool.incr(STOCK_VERSION + stock.getVersion());
            transaction.exec();
        } catch (Exception e) {
            log.error("updateStock fail", e);
            e.printStackTrace();
        }finally {
            RedisPool.jedisPoolClose(jedis);
        }
    }
    /**
     * 重置缓存
     */
    public static void initRedisBefore() {
        Jedis jedis = null;
        try {
            jedis = RedisPool.getJedis();
            // 开始事务
            Transaction transaction = jedis.multi();
            // 事务操作
            RedisPool.set(STOCK_COUNT + 1, "50");
            RedisPool.set(STOCK_SALE + 1, "0");
            RedisPool.set(STOCK_VERSION + 1, "0");
            // 结束事务
            List<Object> list = transaction.exec();
        } catch (Exception e) {
            log.error("initRedis 获取 Jedis 实例失败：", e);
        } finally {
            RedisPool.jedisPoolClose(jedis);
        }
    }



}
