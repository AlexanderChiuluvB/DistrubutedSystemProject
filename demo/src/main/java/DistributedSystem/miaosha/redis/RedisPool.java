package DistributedSystem.miaosha.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class RedisPool {

    private static JedisCluster cluster;
    private static Integer maxTotal = 300;
    private static Integer maxIdle = 100;
    private static Integer maxWait = 10000;
    private static Boolean testOnBorrow = true;
    static {
        initCluster();
    }

    private static void initCluster() {
        Set<HostAndPort> nodes = new HashSet<>();
        nodes.add(new HostAndPort("172.101.8.7", 8005));
        nodes.add(new HostAndPort("172.101.8.6", 8004));
        nodes.add(new HostAndPort("172.101.8.4", 8003));
        nodes.add(new HostAndPort("172.101.8.8", 8006));
        nodes.add(new HostAndPort("172.101.8.3", 8002));
        nodes.add(new HostAndPort("172.101.8.2", 8001));
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setTestOnBorrow(testOnBorrow);
        config.setBlockWhenExhausted(true);
        config.setMaxWaitMillis(maxWait);
        cluster = new JedisCluster(nodes, 2000, 2000, 100, "123456", config);
    }

    public static JedisCluster getJedis() {
        return cluster;
    }

    public static void jedisPoolClose(JedisCluster jedis) throws Exception {
        if (jedis != null) {
            jedis.close();
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
