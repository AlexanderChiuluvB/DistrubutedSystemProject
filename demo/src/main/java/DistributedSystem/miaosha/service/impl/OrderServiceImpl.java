package DistributedSystem.miaosha.service.impl;

import DistributedSystem.miaosha.kafka.kafkaProducer;
import DistributedSystem.miaosha.kafka.miaoshaConsumer;
import DistributedSystem.miaosha.redis.RedisPool;
import DistributedSystem.miaosha.redis.StockWithRedis;
import DistributedSystem.miaosha.service.api.OrderService;
import DistributedSystem.miaosha.dao.StockOrderMapper;
import DistributedSystem.miaosha.pojo.Stock;
import DistributedSystem.miaosha.pojo.StockOrder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.JedisCluster;


import java.util.Collections;
import java.util.Date;

import static DistributedSystem.miaosha.redis.RedisPool.initCluster;

@Slf4j
@Transactional(rollbackFor = Exception.class)
@Service(value = "OrderService")
public class OrderServiceImpl implements OrderService {

    private final String STOCK_LOCK_KEY = "stock_lock_key_";

    private final String STOCK_LOCK_VALUE = "stock_lock_value_";

    @Autowired
    private StockServiceImpl stockService;

    @Autowired
    private StockOrderMapper stockOrderMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("miaosha")
    private String kafkaTopic;

    @Autowired
    private static miaoshaConsumer listener;

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
    public void checkRedisAndSendToKafka(Integer sid) throws Exception {
        //首先检查Redis(内存缓存)的库存
        Stock stock = checkStockWithRedis(sid);
        //下单请求发送到Kafka,序列化类
        //kafkaTemplate.send(kafkaTopic, gson.toJson(stock));
        if (stock != null) {
            kafkaProducer.sendMessage(Collections.singletonMap(kafkaTopic, gson.toJson(stock)));
            System.out.println("消息发送至Kafka成功");
        }

    }

    private Stock checkStockWithRedis(Integer sid) {
        JedisCluster jedis = initCluster();
        boolean updateResult = false;

        //TODO 非线程安全,尝试加锁?
        Integer count;
        Integer version;
        Integer sale;
        count = Integer.parseInt(jedis.get(StockWithRedis.STOCK_COUNT + sid));
        version = Integer.parseInt(jedis.get(StockWithRedis.STOCK_VERSION + sid));
        sale = Integer.parseInt(jedis.get(StockWithRedis.STOCK_SALE + sid));

        if (count < 1) {
            System.out.println("库存不足，秒杀完成\n");
            return null;
        }
        try {
            updateResult = StockWithRedis.updateStockWithRedis(sid);
            if(!updateResult){
                System.out.println("更新Redis失败");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.printf("count %d, version %d\n", count, version);
        Stock stock = new Stock();
        stock.setId(sid);
        stock.setCount(count);
        stock.setSale(sale);
        stock.setVersion(version);
        stock.setName(stockService.getStockById(sid).getName());
        if (updateResult) {
            return stock;
        } else {
            return null;
        }
    }


    @Override
    public int createOrderAndSendToDB(Stock stock) throws Exception {
        boolean updateResult = updateMysqlAndRedis(stock);
        int createOrderResult = -1;
        if (updateResult) {
            createOrderResult = createOrder(stock);
        } else {
            return createOrderResult;
        }
        if (createOrderResult == 1) {
            System.out.printf("商品 %s has sold %d, remain %d\n", stock.getName(), stock.getSale(), stock.getCount());
            System.out.println("Kafka 消费成功");
        } else {
            //System.out.println("Kafka 消费失败");
            return -1;
        }
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

    private boolean updateMysqlAndRedis(Stock stock) throws Exception {
        //JedisCluster jedis = RedisPool.getJedis();
        //Integer version = Integer.parseInt(jedis.get(StockWithRedis.STOCK_VERSION + stock.getId()));
        // System.out.println(stock.getVersion());
        // TODO Kafka到达顺序不是有序的,因此弃用版本号机制更新
        // TODO 稍微改了下更新的语句
        int result = stockService.updateStockInMysql(stock);
        if (result==1) {
            System.out.println("更新成功");
        }
        //int result = stockService.updateStockInMysql(stock);
        // throw new RuntimeException("concurrent update mysql failed");
        //System.out.println("current version " + stock.getVersion());
        //System.out.println("并发更新mysql失败");
        //System.out.printf("current version saved in redis is %d\n", version);
        //System.out.printf("current version of the stock is %d\n", stock.getVersion());
        return result != 0;
        //return StockWithRedis.updateStockWithRedis(stock);
    }
}
