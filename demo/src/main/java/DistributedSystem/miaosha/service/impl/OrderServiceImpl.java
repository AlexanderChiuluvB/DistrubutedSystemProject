package DistributedSystem.miaosha.service.impl;

import DistributedSystem.miaosha.kafka.kafkaProducer;
import DistributedSystem.miaosha.redis.RedisPool;
import DistributedSystem.miaosha.redis.StockWithRedis;
import DistributedSystem.miaosha.service.api.OrderService;
import DistributedSystem.miaosha.dao.StockOrderMapper;
import DistributedSystem.miaosha.pojo.Stock;
import DistributedSystem.miaosha.pojo.StockOrder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.JedisCluster;


import java.util.Collections;
import java.util.Date;
import java.util.concurrent.Semaphore;

@Slf4j
@Transactional(rollbackFor = Exception.class)
@Service(value = "OrderService")
public class OrderServiceImpl implements OrderService {
    private Integer id =0;

    @Autowired
    private StockServiceImpl stockService;

    @Autowired
    private StockOrderMapper stockOrderMapper;

    //@Autowired
    //private KafkaTemplate<String, String> kafkaTemplate;


   // @Autowired
   // private miaoshaConsumer consumer;


    @Value("mykafka")
    private String kafkaTopic;

    private Semaphore semaphore = new Semaphore(1);

    private Gson gson = new GsonBuilder().create();

    @Override
    public int delOrderDB() {
        return stockOrderMapper.clearDB();
    }

    @Override
    public boolean acquireTokenFromRedisBucket(Integer sid) {
        return RedisPool.acquireToken();
    }

    /**
     * 秒杀的请求
     *
     * @param sid stock id
     */
    @Override
    public boolean checkRedisAndSendToKafka(Integer sid) throws Exception {
        //首先检查Redis(内存缓存)的库存
        Stock stock = checkStockWithRedis(sid);
        //下单请求发送到Kafka,序列化类
        //kafkaTemplate.send(kafkaTopic, gson.toJson(stock));
        //System.out.println(++this.id);
        if (stock != null) {
            Thread bgthread=new Thread(new BgThread(stock,kafkaTopic,gson));
            bgthread.start();
            return true;
        }
        return false;

    }

    //TODO 本地减库存线程不安全
    private Stock checkStockWithRedis(Integer sid) throws Exception {
       // ver1. semaphore
        try {
            semaphore.acquire();
            Integer localResult=RedisPool.localDecrStock(sid);
            if(localResult==-1){
                System.out.println("本服务器内库存不足，秒杀失败\n");
                return null;
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            semaphore.release();
        }

        // ver2. synchronized

        Stock stock = new Stock();
        boolean redisResult=RedisPool.redisDecrStock(sid,stock);
        if(!redisResult){
            //RedisPool.localDecrStockRecover(sid,localResult);
            System.out.println("商品"+stock.getName()+"已无Redis库存，秒杀失败");
            return null;
        }
        stock.setName(stockService.getStockById(sid).getName());
        System.out.println(++this.id);
        return stock;
    }


    @Override
    public int createOrderAndSendToDB(Stock stock) throws Exception {
        System.out.println("马上更新Mysql");
        boolean updateResult = updateMysql(stock);
        int createOrderResult = -1;

        if (updateResult) {
            createOrderResult = createOrder(stock);
        }
        else return createOrderResult;

        if (createOrderResult == 1) {
            System.out.printf("商品 %s has sold %d, remain %d\n", stock.getName(), stock.getSale(), stock.getCount());
        }
        else return -1;
        return createOrderResult;
    }

    /**
     * 创建持久化到数据库的订单
     */
    private int createOrder(Stock stock) {

        StockOrder order = new StockOrder();
        order.setCreateTime(new Date());
        order.setName(stock.getName());
        order.setSid(stock.getId());
        int result = stockOrderMapper.insertToDB(order);
        if (result == 0) {
            System.out.println("创建订单失败");
            return -1;
        }
        return result;
    }

    private boolean updateMysql(Stock stock) throws Exception {
        int result = stockService.updateStockInMysql(stock);
        if (result == 0) {
            System.out.println("更新mysql失败");
            return false;
        }
        return true;
    }
}
