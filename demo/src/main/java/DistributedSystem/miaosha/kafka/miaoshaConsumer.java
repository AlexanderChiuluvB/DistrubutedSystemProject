package DistributedSystem.miaosha.kafka;

import DistributedSystem.miaosha.pojo.Stock;
import DistributedSystem.miaosha.service.api.OrderService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Slf4j
@Component
public class miaoshaConsumer {

    private Gson gson = new GsonBuilder().create();

    @Autowired
    private OrderService orderService;

    /**
     * 监听器从kafka获得消息,然后采用乐观锁机制来更新mysql数据库
     * @param record
     */
    @KafkaListener(topics = "mykafka")
    public void listen(ConsumerRecord<String, String> record) throws Exception {

        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        //序列化 object -> String
        String message = (String) kafkaMessage.get();
        //TODO 反序列化
        //Class object = gson.fromJson((String)message, Class.class)
        Stock stock = gson.fromJson((String) message, Stock.class);
        //TODO 执行消费消息
        orderService.createOrderAndSendToDB(stock);

    }


}
