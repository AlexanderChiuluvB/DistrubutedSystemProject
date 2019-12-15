package DistributedSystem.miaosha.redis;

import DistributedSystem.miaosha.pojo.Stock;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import redis.clients.jedis.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

    private static RedissonClient cluster;
    private static ConcurrentHashMap<Integer,Integer>serverStocks = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Integer,Integer>serverBufferStocks=new ConcurrentHashMap<>();
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
        try {
            initCluster();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initCluster() throws Exception {
        Config config=new Config();
        config.useClusterServers()
                .addNodeAddress("redis://172.101.8.2:8001")
                .addNodeAddress("redis://172.101.8.3:8002")
                .addNodeAddress("redis://172.101.8.4:8003")
                .addNodeAddress("redis://172.101.8.5:8004")
                .addNodeAddress("redis://172.101.8.6:8005")
                .addNodeAddress("redis://172.101.8.7:8006")
                .setPassword("123456")
                .setScanInterval(10000)
                .setMasterConnectionPoolSize(100)
                .setSlaveConnectionPoolSize(100);

        cluster = Redisson.create(config);
    }

    public static void addStockEntry(int sid, int stockNum){
        serverStocks.put(sid,(int) (stockNum/7));
    }


    public static RedissonClient getJedis() {
        return cluster;
    }

    // 拿到令牌的订单先更新本地库存，变量线程安全，无需额外同步
    public static Integer localDecrStock(Integer sid){
        try {
            Integer stock=serverStocks.get(sid);
            if(stock>0){
                serverStocks.put(sid,stock-1);
                return 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    //本地更新库存后，申请Redis的库存
    public static boolean redisDecrStock(Integer sid, Stock s) throws Exception {
        RLock lock=cluster.getLock("STOCK_"+sid);
        lock.lock();
        long stock= get(StockWithRedis.STOCK_COUNT+sid);
        if(stock<1){
            lock.unlock();
            return false;
        }
        long sale=get(StockWithRedis.STOCK_SALE+sid);
        decr(StockWithRedis.STOCK_COUNT+sid);
        incr(StockWithRedis.STOCK_SALE+sid);
        lock.unlock();
        s.setCount((int)(stock-1));
        s.setId(sid);
        s.setSale((int)(sale+1));
        System.out.println("Now in Redis, STOCK ="+(stock-1)+" SALE="+(sale+1));
        return true;
    }

    // 每1ms，令牌桶中令牌增加一个，可以根据服务器处理能力进行调整
    @Scheduled(fixedRate = 1)
    private static void incrTokenBucket(){
        bucket.incrToken();
    }

    public static boolean acquireToken(){
        return bucket.decrToken();
    }

    public static void set(String key, long value) throws Exception {
        try {
            RAtomicLong keyObject = cluster.getAtomicLong(key);
            keyObject.set(value);
        } catch (Exception e) {
            System.out.printf("set key{%s} value{%s} error %s" , key , value , e);
            e.printStackTrace();
        }
    }

    public static long get(String key) throws Exception {
        long result=0;

        try {
            RAtomicLong keyObject = cluster.getAtomicLong(key);
            result=keyObject.get();
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
    public static boolean del(String key) throws Exception {
        boolean result = false;
        try {
            RBucket<String> keyObject = cluster.getBucket(key);
            result=keyObject.delete();
        } catch (Exception e) {
            System.out.println("del key:{} error" + key + e);
        }
        return result;
    }

    /**
     * key - value 自增
     */
    public static long incr(String key) throws Exception {
        long result=0;
        try {
            RAtomicLong keyObject = cluster.getAtomicLong(key);
            result=keyObject.incrementAndGet();
        } catch (Exception e) {
            System.out.println("listGet key:{} error" + key + e);
        }
        return result;
    }

    /**
     * key - value 自减
     */
    public static long decr(String key) throws Exception {
        long result = 0;
        try {
            RAtomicLong keyObject = cluster.getAtomicLong(key);
            result=keyObject.decrementAndGet();
        } catch (Exception e) {
            System.out.println("listGet key:{} error" + key + e);
        }
        return result;
    }


}
