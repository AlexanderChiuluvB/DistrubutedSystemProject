package DistributedSystem.miaosha.redis;

import com.alibaba.druid.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisNoScriptException;

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
    private static Integer maxTotal = 300;
    private static Integer maxIdle = 100;
    private static Integer maxWait = 10000;
    private static Boolean testOnBorrow = true;
    private static final Long UNLOCK_SUCCESS_CODE = 1L;
    private static final String LOCK_SUCCESS_CODE = "ok";
    private static volatile String unlockSha1 = "";
    private static TokenBucket bucket= new TokenBucket();
    static {
        initCluster();
    }

    public static JedisCluster initCluster() {
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
        return new JedisCluster(nodes, 2000, 2000, 100, "123456",config);
    }

    public static JedisCluster getJedis() {
        return cluster;
    }

    public static void jedisPoolClose(JedisCluster jedis) throws Exception {
        if (jedis != null) {
            jedis.close();
        }
    }

    // 每5ms，令牌桶中令牌增加一个，可以根据服务器处理能力进行调整
    @Scheduled(fixedRate = 1)
    private static void incrTokenBucket(){
        bucket.incrToken();
    }

    public static boolean acquireToken(){
        return bucket.decrToken();
    }

    private static final String DISTRIBUTE_LOCK_SCRIPT_UNLOCK_VAL = "if" +
            " redis.call('get', KEYS[1]) == ARGV[1]" +
            " then" +
            " return redis.call('del', KEYS[1])" +
            " else" +
            " return 0" +
            " end";


    public static boolean tryLock(String lockKey, String lockVal, long expiryTime, long loopTryTime){
        Long endTime = System.currentTimeMillis() + loopTryTime;
        while (System.currentTimeMillis() < endTime){
            if (tryLock(lockKey, lockVal, expiryTime)){
                return true;
            }
        }
        return false;
    }

    public static boolean tryLock(String lockKey, String lockVal, long expiryTime){
        //相比一般的分布式锁，这里把setNx和setExpiry操作合并到一起，jedis保证原子性，避免连个命令之间出现宕机等问题
        //这里也可以我们使用lua脚本实现
        cluster = initCluster();
        String result = cluster.set(lockKey, lockVal, "NX", "PX", expiryTime);
        return LOCK_SUCCESS_CODE.equalsIgnoreCase(result);
    }


    /**
     * 释放分布式锁，释放失败最可能是业务执行时间长于lockKey过期时间，应当结合业务场景调整过期时间
     * @param lockKey 锁key
     * @param lockVal 锁值
     * @return 是否释放成功
     */
    public static boolean tryUnLock(JedisCluster cluster, String lockKey, String lockVal){
        List<String> keys = new ArrayList<>();
        keys.add(lockKey);
        List<String> argv = new ArrayList<>();
        argv.add(lockVal);
        try {
            Object result = cluster.evalsha(unlockSha1, keys, argv);
            return UNLOCK_SUCCESS_CODE.equals(result);
        }catch (JedisNoScriptException e){
            //没有脚本缓存时，重新发送缓存
            storeScript(lockKey);
            Object result = cluster.evalsha(unlockSha1, keys, argv);
            return UNLOCK_SUCCESS_CODE.equals(result);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 由于使用redis集群，因此每个节点都需要各自缓存一份脚本数据
     * @param slotKey 用来定位对应的slot的slotKey
     */
    private static void storeScript(String slotKey){
        if (StringUtils.isEmpty(unlockSha1) || !cluster.scriptExists(unlockSha1, slotKey)){
            //redis支持脚本缓存，返回哈希码，后续可以继续用来调用脚本
            unlockSha1 = cluster.scriptLoad(DISTRIBUTE_LOCK_SCRIPT_UNLOCK_VAL, slotKey);
        }
    }

    public static String set(String key, String value) throws Exception {

        JedisCluster jedis = null;
        String result = null;

        try {
            jedis = getJedis();
            result = jedis.set(key, value);
        } catch (Exception e) {
            System.out.printf("set key{%s} value{%s} error %s" , key , value , e);
            e.printStackTrace();
        } finally {
            jedisPoolClose(jedis);
        }
        return result;
    }

    public static String get(String key) throws Exception {
        JedisCluster jedis = null;
        String result = null;

        try {
            jedis = RedisPool.getJedis();
            result = jedis.get(key);
        } catch (Exception e) {
            System.out.println("get key:{} error" + key + e);
        } finally {
            RedisPool.jedisPoolClose(jedis);
        }
        return result;
    }

    /**
     * 删除 key - value 值
     *
     * @param key
     */
    public static Long del(String key) throws Exception {
        JedisCluster jedis = null;
        Long result = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.del(key);
        } catch (Exception e) {
            System.out.println("del key:{} error" + key + e);
        } finally {
            RedisPool.jedisPoolClose(jedis);
        }
        return result;
    }

    /**
     * key - value 自增
     */
    public static Long incr(String key) throws Exception {
        JedisCluster jedis = null;
        Long result = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.incr(key);
        } catch (Exception e) {
            System.out.println("listGet key:{} error" + key + e);
        } finally {
            RedisPool.jedisPoolClose(jedis);
        }
        return result;
    }

    /**
     * key - value 自减
     */
    public static Long decr(String key) throws Exception {
        JedisCluster jedis = null;
        Long result = null;
        try {
            jedis = RedisPool.getJedis();
            result = jedis.decr(key);
        } catch (Exception e) {
            System.out.println("listGet key:{} error" + key + e);
        } finally {
            RedisPool.jedisPoolClose(jedis);
        }
        return result;
    }


}
