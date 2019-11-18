package kafka;

import DistributedSystem.miaosha.kafka.kafkaConsumer;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;


public class kafkaConsumerTest {
    private static Properties kafkaProps = new Properties();

    static {
        kafkaProps.put("bootstrap.servers", "3.15.141.41:9092");
        kafkaProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaProps.put("group.id", "ConsumerGroup");
    }

    private KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(kafkaProps);

    @Test
    public void consumerTest() throws FileNotFoundException {
        ArrayList<String> topics = new ArrayList<>();
        topics.add("mykafka");
        kafkaConsumer.commit(topics);
    }

    @Test
    public void consumerSubscribeTest() {
        consumer.subscribe(Collections.singletonList("mykafka"));
        //支持正则表达式
        //consumer.subscribe(Collections.singletonList("test.*"));
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf("topic = %s, partitions = %s," +
                                    "offset = %d, customer = %s, country = %s\n",
                            record.topic(), record.partition(), record.offset(),
                            record.key(), record.value());
                }
            }
        } finally {
            consumer.close();
        }
    }

    @Test
    public void consumerSyncCommitTest() {
        consumer.subscribe(Collections.singletonList("mykafka"));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("topic = %s, partition = %s, offset = %d," +
                                "customer = %s, country = %s\n", record.topic(), record.partition(),
                        record.offset(), record.key(), record.value());
                try {
                    //处理完当前批次的消息，再轮询更多消息之前，调用
                    //commitSync来提交当前批次最新的偏移量
                    consumer.commitSync();
                } catch (CommitFailedException e) {
                    System.out.println(e);
                }
            }
        }
    }

    @Test
    public void consumerAsyncCommitTest() {
        consumer.subscribe(Collections.singletonList("mykafka"));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("topic = %s, partition = %s, offset = %d," +
                                "customer = %s, country = %s\n", record.topic(), record.partition(),
                        record.offset(), record.key(), record.value());
                try {
                    //异步提交一个缺点就是在成功提交或者碰到无法恢复的错误的时候不会一直重试
                    // 可以通过传入一个回调函数解决这个问题
                    consumer.commitAsync(new OffsetCommitCallback() {
                        @Override
                        public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception e) {
                            System.out.println(offsets);
                            if (e != null) {
                                System.out.println("Commit fail for offsets {}" + offsets + e);
                            }
                        }
                    });
                } catch (CommitFailedException e) {
                    System.out.println(e);
                }
            }
        }
    }

}