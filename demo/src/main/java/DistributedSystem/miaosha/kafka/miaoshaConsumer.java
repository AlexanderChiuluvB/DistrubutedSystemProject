package DistributedSystem.miaosha.kafka;

import DistributedSystem.miaosha.pojo.Stock;
import DistributedSystem.miaosha.service.api.OrderService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/*
@Slf4j
@Component
@EnableKafka
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
        return factory;
    }

    @Bean
    public Map<String, Object> consumerConfigs() {

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group");
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.101.8.2:9092,172.101.8.3:9092,172.101.8.4:9092");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 5); //设置每次接收Message的数量
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 5);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 5);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return props;
    }

    @Bean
    public KafkaProperties.Listener listener() {
        return new KafkaProperties.Listener();
    }

*/
/*
    @KafkaListener(containerFactory = "batchFactory",group = "test-consumer-group",topicPartitions = { @TopicPartition(topic = "mykafka", partitions = { "0" })})
    public void listen0(List<ConsumerRecord<String, String>> records) throws Exception {

        for (ConsumerRecord<?, ?> record : records) {
            Optional<?> kafkaMessage = Optional.ofNullable(record.value());
            // 获取消息
            //序列化 object -> String
            String message = (String) kafkaMessage.get();
            //System.out.println(message);

            //TODO 反序列化
            //Class object = gson.fromJson((String)message, Class.class)
            Stock stock = gson.fromJson((String) message, Stock.class);

            orderService.createOrderAndSendToDB(stock);
        }
    }




    @KafkaListener(containerFactory = "batchFactory",group = "test-consumer-group",topicPartitions = { @TopicPartition(topic = "mykafka", partitions = { "1" })})
    public void listen1(List<ConsumerRecord<String, String>> records) throws Exception {

        for (ConsumerRecord<?, ?> record : records) {
            Optional<?> kafkaMessage = Optional.ofNullable(record.value());
            // 获取消息
            //序列化 object -> String
            String message = (String) kafkaMessage.get();
            //System.out.println(message);

            //TODO 反序列化
            //Class object = gson.fromJson((String)message, Class.class)
            Stock stock = gson.fromJson((String) message, Stock.class);

            orderService.createOrderAndSendToDB(stock);
        }
    }


    @KafkaListener(containerFactory = "batchFactory",group = "test-consumer-group",topicPartitions = { @TopicPartition(topic = "mykafka", partitions = { "2" })})
    public void listen2(List<ConsumerRecord<String, String>> records) throws Exception {

        for (ConsumerRecord<?, ?> record : records) {
            Optional<?> kafkaMessage = Optional.ofNullable(record.value());
            // 获取消息
            //序列化 object -> String
            String message = (String) kafkaMessage.get();
            //System.out.println(message);

            //TODO 反序列化
            //Class object = gson.fromJson((String)message, Class.class)
            Stock stock = gson.fromJson((String) message, Stock.class);

            orderService.createOrderAndSendToDB(stock);
        }
    }


    @KafkaListener(containerFactory = "batchFactory",group = "test-consumer-group",topicPartitions = { @TopicPartition(topic = "mykafka", partitions = { "3" })})
    public void listen3(List<ConsumerRecord<String, String>> records) throws Exception {

        for (ConsumerRecord<?, ?> record : records) {
            Optional<?> kafkaMessage = Optional.ofNullable(record.value());
            // 获取消息
            //序列化 object -> String
            String message = (String) kafkaMessage.get();
            //System.out.println(message);

            //TODO 反序列化
            //Class object = gson.fromJson((String)message, Class.class)
            Stock stock = gson.fromJson((String) message, Stock.class);

            orderService.createOrderAndSendToDB(stock);
        }
    }


    @KafkaListener(containerFactory = "batchFactory",group = "test-consumer-group",topicPartitions = { @TopicPartition(topic = "mykafka", partitions = { "4" })})
    public void listen4(List<ConsumerRecord<String, String>> records) throws Exception {

        for (ConsumerRecord<?, ?> record : records) {
            Optional<?> kafkaMessage = Optional.ofNullable(record.value());
            // 获取消息
            //序列化 object -> String
            String message = (String) kafkaMessage.get();
            //System.out.println(message);

            //TODO 反序列化
            //Class object = gson.fromJson((String)message, Class.class)
            Stock stock = gson.fromJson((String) message, Stock.class);

            orderService.createOrderAndSendToDB(stock);
        }
    }

    @KafkaListener(containerFactory = "batchFactory",group = "test-consumer-group",topicPartitions = { @TopicPartition(topic = "mykafka", partitions = { "5" })})
    public void listen5(List<ConsumerRecord<String, String>> records) throws Exception {

        for (ConsumerRecord<?, ?> record : records) {
            Optional<?> kafkaMessage = Optional.ofNullable(record.value());
            // 获取消息
            //序列化 object -> String
            String message = (String) kafkaMessage.get();
            //System.out.println(message);

            //TODO 反序列化
            //Class object = gson.fromJson((String)message, Class.class)
            Stock stock = gson.fromJson((String) message, Stock.class);

            orderService.createOrderAndSendToDB(stock);
        }
    }
}

*/
