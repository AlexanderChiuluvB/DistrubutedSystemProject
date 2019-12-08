package DistributedSystem.miaosha.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonManager {
    //@Value("${spring.redis.cluster.nodes}")
    private static String cluster = "172.101.8.7:8006,172.101.8.6:8005,172.101.8.5:8004,172.101.8.4:8003,172.101.8.3:8002,172.101.8.2:8001";
    //@Value("${spring.redis.password}")
    private static String password = "123456";

    @Bean
    public static RedissonClient getRedisson(){
        String[] nodes = cluster.split(",");
        //redisson版本是3.5，集群的ip前面要加上“redis://”，不然会报错，3.2版本可不加
        for(int i=0;i<nodes.length;i++){
            nodes[i] = "redis://"+nodes[i];
        }
        RedissonClient redisson = null;
        Config config = new Config();
        config.useClusterServers() //这是用的集群server
                .setScanInterval(2000) //设置集群状态扫描时间
                .addNodeAddress(nodes)
                .setPassword(password);
        redisson = Redisson.create(config);

        //可通过打印redisson.getConfig().toJSON().toString()来检测是否配置成功
        return redisson;
    }

    public static void main(String[] args){}
}
