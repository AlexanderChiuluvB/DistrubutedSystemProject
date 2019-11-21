package DistributedSystem.miaosha.kafka;

import DistributedSystem.miaosha.pojo.Stock;
import DistributedSystem.miaosha.service.api.OrderService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class miaoshaConsumer {

    private Gson gson = new GsonBuilder().create();

    @Autowired
    private OrderService orderService;

    @Bean
    KafkaListenerContainerFactory<?>batchFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(consumerConfigs()));
        factory.setBatchListener(true);
        factory.setConcurrency(6);
        factory.setAutoStartup(true);
        factory.getContainerProperties().setAckMode(AbstractMessageListenerContainer.AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

    @Bean
    public Map<String, Object> consumerConfigs() {

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group");
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.101.8.2:9092,172.101.8.3:9092,172.101.8.4:9092");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10); //设置每次接收Message的数量
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 10*60*1000);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,"false");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return props;
    }


    /**
     * 监听器从kafka获得消息,然后采用乐观锁机制来更新mysql数据库
     * @param records
     */
    @KafkaListener(topics = "mykafka",containerFactory = "batchFactory",group = "test-consumer-group")
    public void listen(List<ConsumerRecord<String, String>> records) throws Exception {

        for (ConsumerRecord<?, ?> record : records) {
            Optional<?> kafkaMessage = Optional.ofNullable(record.value());
            // 获取消息
            //序列化 object -> String
            String message = (String) kafkaMessage.get();
            //System.out.println(message);

            //TODO 反序列化
            //Class object = gson.fromJson((String)message, Class.class)
            Stock stock = gson.fromJson((String) message, Stock.class);

            //TODO 执行消费消息
            orderService.createOrderAndSendToDB(stock);
        }

    }
}
