package service.impl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pojo.Stock;
import service.dao.OrderService;

@Slf4j
@Transactional(rollbackFor = Exception.class)
@Service(value = "OrderService")
public class OrderServiceImpl implements OrderService {

    //@Autowired
    //private StockServiceImpl stockService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.template.default-topic}")
    private String kafkaTopic;

    private Gson gson = new GsonBuilder().create();

    @Override
    public int createOrderAndSendToDB(Stock stock) throws Exception {
        //TODO 乐观锁更新库存和Redis
        //updateRedis(stock);
        int result = createOrder(stock);
        if (result==1) {
            System.out.println("Kafka 消费成功");
        } else {
            System.out.println("Kafka 消费失败");
        }
        return result;
    }

    @Override
    public void checkRedisAndSendToKafka(int sid) {

        //TODO Redis校检库存
        //Stock stock = checkStockWithRedis(sid);
        //下单请求发送到Kafka,序列化类
       // kafkaTemplate.send(kafkaTopic, gson.toJson(stock));
    }

    private int createOrder(Stock stock) {
        //TODO
        return 0;
    }

}
