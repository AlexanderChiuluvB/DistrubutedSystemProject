package DistributedSystem.miaosha.service.impl;

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


import java.util.Date;

@Slf4j
@Transactional(rollbackFor = Exception.class)
@Service(value = "OrderService")
public class OrderServiceImpl implements OrderService {

    @Autowired
    private StockServiceImpl stockService;

    @Autowired
    private StockOrderMapper stockOrderMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.template.default-topic}")
    private String kafkaTopic;

    private Gson gson = new GsonBuilder().create();

    @Override
    public int delOrderDB() {
        return stockOrderMapper.clearDB();
    }

    /**
     * 秒杀的请求
     * @param sid stock id
     */
    @Override
    public void checkRedisAndSendToKafka(int sid) {
        //首先检查Redis(内存缓存)的库存
        Stock stock = checkStockWithRedis(sid);
        //下单请求发送到Kafka,序列化类
        kafkaTemplate.send(kafkaTopic, gson.toJson(stock));
        log.info("消息发送至Kafka成功");
    }

    @Override
    public int createOrderAndSendToDB(Stock stock) throws Exception {
        //TODO 乐观锁更新库存和Redis
        updateMysqlAndRedis(stock);
        int result = createOrder(stock);
        if (result == 1) {
            System.out.println("Kafka 消费成功");
        } else {
            System.out.println("Kafka 消费失败");
        }
        return result;
    }


    /**
     * 创建持久化到数据库的订单
     */
    private int createOrder(Stock stock) {

        StockOrder order = new StockOrder();
        order.setId(stock.getId());
        order.setCreateTime(new Date());
        order.setName(stock.getName());
        int result = stockOrderMapper.insertToDB(order);
        if (result == 0) {
            throw new RuntimeException("创建订单失败");
        }
        return result;
    }

    private void updateMysqlAndRedis(Stock stock) {
        int result = stockService.updateStockInMysql(stock);
        if (result == 0) {
            throw new RuntimeException("并发更新mysql失败");
        }
        StockWithRedis.updateStockWithRedis(stock);
    }

    private Stock checkStockWithRedis(int sid) {

        Integer count = Integer.parseInt(RedisPool.get(StockWithRedis.STOCK_COUNT + sid));
        Integer version = Integer.parseInt(RedisPool.get(StockWithRedis.STOCK_VERSION + sid));
        Integer sale = Integer.parseInt(RedisPool.get(StockWithRedis.STOCK_SALE + sid));
        if (count < 1) {
            log.info("库存不足");
            throw new RuntimeException("库存不足 Redis currentCount: " + sale);
        }
        Stock stock = new Stock();
        stock.setId(sid);
        stock.setCount(count);
        stock.setSale(sale);
        stock.setVersion(version);
        // 此处应该是热更新，但是在数据库中只有一个商品，所以直接赋值
        stock.setName("mobile phone");
        return stock;
    }


}
