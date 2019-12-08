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
     * 重置缓存 缓存预热
     */
    public static int initRedisBefore(int id, int count) throws Exception {
       JedisCluster jedis = null;
        try {
            jedis = RedisPool.getJedis();
            jedis.set(STOCK_COUNT + id, String.valueOf(count));
            jedis.set(STOCK_SALE + id, "0");
            System.out.println("Redis STOCK COUNT: "+jedis.get(STOCK_COUNT+id));
            System.out.println("Redis SALE COUNT: "+jedis.get(STOCK_SALE+id));
            return 1;
        } catch (Exception e) {
            System.out.println("initRedis 获取 Jedis 实例失败："+ e);
            return 0;
        }
    }

    public static void initServerBefore(int id ,int count){
        RedisPool.addStockEntry(id,count);
    }



}
