package DistributedSystem.miaosha.redis;

import DistributedSystem.miaosha.pojo.Stock;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import redis.clients.jedis.*;

import java.util.*;

class TokenBucket{
    private Integer tokens=500;
    private static Integer maxTokens = 5000;

    public synchronized Integer getToken(){
        return tokens;
    }

    synchronized void incrToken(){
        if(tokens<maxTokens)
            ++tokens;
    }

    synchronized boolean decrToken(){
        if(tokens<=0)
            return false;
        --tokens;
        return true;
    }
}

@Component
@Slf4j
public class RedisPool {

    private static JedisCluster cluster;
    private static HashMap<Integer,Integer>serverStocks = new HashMap<>();
    private static HashMap<Integer,Integer>serverBufferStocks=new HashMap<>();
    private static final Long RELEASE_SUCCESS=1L;
    private static final String LOCK_SUCCESS="OK";
    private static final String SET_IF_NOT_EXIST="NX";
    private static final String SET_WITH_EXPIRE_TIME="PX";
    private static Integer maxTotal = 300;
    private static Integer maxIdle = 100;
    private static Integer maxWait = 10000;
    private static Boolean testOnBorrow = true;
    private static TokenBucket bucket= new TokenBucket();
    static {
        initCluster();
    }

    private static void initCluster() {
        Set<HostAndPort> nodes = new HashSet<>();
        nodes.add(new HostAndPort("172.101.8.7", 8006));
        nodes.add(new HostAndPort("172.101.8.6", 8005));
        nodes.add(new HostAndPort("172.101.8.5", 8004));
        nodes.add(new HostAndPort("172.101.8.4", 8003));
        nodes.add(new HostAndPort("172.101.8.3", 8002));
        nodes.add(new HostAndPort("172.101.8.2", 8001));
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setTestOnBorrow(testOnBorrow);
        config.setBlockWhenExhausted(true);
        config.setMaxWaitMillis(maxWait);
        cluster = new JedisCluster(nodes, 2000, 2000, 100, "123456",config);
    }

    public static void addStockEntry(int sid, int stock){
        serverStocks.put(sid,(int) (stock*0.14));
        serverBufferStocks.put(sid,(int)(stock*0.03));
        System.out.println("server local stocks :"+serverStocks.get(sid));
        System.out.println("server local buffer stocks :"+serverBufferStocks.get(sid));
    }


    public static JedisCluster getJedis() {
        return cluster;
    }

    // 拿到令牌的订单先更新本地库存，单线程操作，无需同步
    public static Integer localDecrStock(Integer sid){
        Integer stock=serverStocks.get(sid);
        if(stock>0){
            serverStocks.put(sid,stock-1);
            return 1;
        }
        stock=serverBufferStocks.get(sid);
        if(stock>0){
            serverBufferStocks.put(sid,stock-1);
            return 0;
        }
        return -1;
    }

    //本地更新库存后，申请Redis的库存
    public static boolean redisDecrStock(Integer sid, Stock s) throws Exception {

        boolean locked=false;
        String requestId=UUID.randomUUID().toString();
        while(!locked)
            locked = tryGetDistributedLock(sid+"_KEY", requestId, 50);
        Integer stock= Integer.parseInt(cluster.get(StockWithRedis.STOCK_COUNT+sid));
        if(stock<1){
            releaseDistributedLock(StockWithRedis.STOCK_COUNT + sid+"_KEY",requestId);
            return false;
        }
        Integer sale=Integer.parseInt(cluster.get(StockWithRedis.STOCK_SALE+sid));
        decr(StockWithRedis.STOCK_COUNT+sid);
        incr(StockWithRedis.STOCK_SALE+sid);
        releaseDistributedLock(sid+"_KEY",requestId);
        s.setCount(stock-1);
        s.setId(sid);
        s.setSale(sale+1);
        return true;
    }


    public static boolean tryGetDistributedLock(String lockKey, String requestId, int expireTime) {
        String result = cluster.set(lockKey, requestId, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);
        return LOCK_SUCCESS.equals(result);
    }

    public static boolean releaseDistributedLock(String lockKey, String requestId) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = cluster.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));
        return RELEASE_SUCCESS.equals(result);
    }

    // 本地先更新库存，如果Redis库存空了，本地库存要恢复
    public static void localDecrStockRecover(Integer sid,Integer localCode){
        if(localCode==1)
            serverStocks.put(sid,serverStocks.get(sid)+1);
        else
            serverBufferStocks.put(sid,serverBufferStocks.get(sid)+1);
    }

    // 每1ms，令牌桶中令牌增加一个，可以根据服务器处理能力进行调整
    @Scheduled(fixedRate = 1)
    private static void incrTokenBucket(){
        bucket.incrToken();
    }

    public static boolean acquireToken(){
        return bucket.decrToken();
    }

    public static String set(String key, String value) throws Exception {
        String result = null;

        try {
            result = cluster.set(key, value);
        } catch (Exception e) {
            System.out.printf("set key{%s} value{%s} error %s" , key , value , e);
            e.printStackTrace();
        }
        return result;
    }

    public static String get(String key) throws Exception {
        String result = null;

        try {
            cluster.get(key);
        } catch (Exception e) {
            System.out.println("get key:{} error " + key + e);
        }
        return result;
    }

    /**
     * 删除 key - value 值
     *
     * @param key
     */
    public static Long del(String key) throws Exception {
        Long result = null;
        try {
            result = cluster.del(key);
        } catch (Exception e) {
            System.out.println("del key:{} error" + key + e);
        }
        return result;
    }

    /**
     * key - value 自增
     */
    public static Long incr(String key) throws Exception {
        Long result = null;
        try {
            result = cluster.incr(key);
        } catch (Exception e) {
            System.out.println("listGet key:{} error" + key + e);
        }
        return result;
    }

    /**
     * key - value 自减
     */
    public static Long decr(String key) throws Exception {
        Long result = null;
        try {
            result = cluster.decr(key);
        } catch (Exception e) {
            System.out.println("listGet key:{} error" + key + e);
        }
        return result;
    }


}
