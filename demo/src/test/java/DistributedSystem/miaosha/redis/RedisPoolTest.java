package DistributedSystem.miaosha.redis;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import redis.clients.jedis.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class RedisPoolTest {

    private static JedisCluster cluster;

    private static Integer maxTotal = 300;

    private static Integer maxIdle = 100;

    private static Integer maxWait = 10000;

    private static Boolean testOnBorrow = true;

    @Value("${spring.redis.ip}")
    private static String redisIP;

    @Value("${spring.redis.port}}")
    private static Integer redisPort;


    @Test
    public void testCluster() throws IOException, InterruptedException {
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
        try {
            cluster.set("alex","zhao");
            String res = cluster.get("alex");
            System.out.println(res);
            cluster.close();
        } catch (Exception e) {
            e.printStackTrace();
            cluster.close();
        }
    }

}