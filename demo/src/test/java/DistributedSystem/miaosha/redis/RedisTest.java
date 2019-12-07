package DistributedSystem.miaosha.redis;


import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import redis.clients.jedis.Jedis;

import java.util.Collections;

public class RedisTest {
    private static final Long RELEASE_SUCCESS=1L;
    private static final String LOCK_SUCCESS="OK";
    private static final String SET_IF_NOT_EXIST="NX";
    private static final String SET_WITH_EXPIRE_TIME="PX";

    public static void main(String[] args){
        Integer i=0;
        test(i);
        System.out.println(i);
//        Config config=new Config();
//        config.useClusterServers()
//                .setScanInterval(2000) // 集群状态扫描间隔时间，单位是毫秒
//                //可以用"rediss://"来启用SSL连接
//                .addNodeAddress("redis://172.101.8.2:8001")
//                .addNodeAddress("redis://172.101.8.3:8002")
//                .addNodeAddress("redis://172.101.8.4:8003")
//                .addNodeAddress("redis://172.101.8.5:8004")
//                .addNodeAddress("redis://172.101.8.6:8005")
//                .addNodeAddress("redis://172.101.8.7:8006");
//
//        RedissonClient redisson = Redisson.create(config);
//
//        Jedis jedis = new Jedis("localhost",6379);
//        jedis.set("Test","1");
//        boolean result=tryGetDistributedLock(jedis,"TestLock","123",500);
//        System.out.println(result);
//        result=releaseDistributedLock(jedis,"TestLock","123");
//        System.out.println(result);
    }

    public static void test(Integer i){
        i=1;
    }

    public static boolean tryGetDistributedLock(Jedis jedis, String lockKey, String requestId, int expireTime) {
        String result = jedis.set(lockKey, requestId, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);
        return LOCK_SUCCESS.equals(result);
    }

    public static boolean releaseDistributedLock(Jedis jedis, String lockKey, String requestId) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(requestId));
        return RELEASE_SUCCESS.equals(result);
    }
}
