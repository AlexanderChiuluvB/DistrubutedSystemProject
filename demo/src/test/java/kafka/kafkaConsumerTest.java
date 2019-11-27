package kafka;

import DistributedSystem.miaosha.kafka.kafkaConsumer;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.junit.Test;
import org.springframework.data.redis.listener.Topic;

import java.io.FileNotFoundException;
import java.util.*;


public class kafkaConsumerTest {
    private static Properties kafkaProps = new Properties();

    static {
        kafkaProps.put("bootstrap.servers", "172.101.8.2:9092,172.101.8.3:9092,172.101.8.4:9092");
        kafkaProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaProps.put("group.id", "ConsumerGroup");
    }

    private KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(kafkaProps);

    @Test
    public void consumerTest() throws FileNotFoundException {
        ArrayList<String> topics = new ArrayList<>();
        topics.add("mykafka");
       // kafkaConsumer.commit(topics);
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

    @Test
    public void consumerAsyncWithSyncCommitTest() {
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
                    consumer.commitAsync();
                } catch (CommitFailedException e) {
                    System.out.println(e);
                } finally {
                    consumer.commitSync();
                }
            }
        }
    }

    @Test
    public void consumerWithSpecificOffset() {
        consumer.subscribe(Collections.singletonList("mykafka"));
        Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
        int count = 0;
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(1000);
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("topic = %s, partition = %s, offset = %d," +
                                "customer = %s, country = %s\n", record.topic(), record.partition(),
                        record.offset(), record.key(), record.value());
                currentOffsets.put(new TopicPartition(record.topic(), record.partition()), new
                        OffsetAndMetadata(record.offset() + 1, "no metadata"));
                if (count % 100 == 0) {
                    consumer.commitAsync(currentOffsets, null);
                }
                count++;
            }
        }
    }

    @Test
    public void consumerWithRebalanceListener() {

        Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

        class HandleRebalance implements ConsumerRebalanceListener {

            //重新分配分区之后和消费者开始读取消息之前
            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {

            }

            // Rebalance开始之前和消费者停止读取消息之后
            // 如果要发生Rebalance，消费者可能会丢失对某个分区的所有权
            // 因此需要在丢失所有权之前提交偏移量，那么下一个接管分区的consumer就会知道继续从哪里
            // 读起
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                System.out.println("Lost partitions in rebalance." +
                        "Committing current offsets: " + currentOffsets);
                consumer.commitSync(currentOffsets);
            }
        }

        try {
            consumer.subscribe(Collections.singletonList("mykafka"), new HandleRebalance());

            while (true) {
                ConsumerRecords<String, String> consumerRecords = consumer.poll(100);
                for (ConsumerRecord<String, String> record : consumerRecords) {
                    System.out.printf("topic = %s, partition = %s, offset = %d," +
                                    " message = %s\n", record.topic(), record.partition(),
                            record.offset(), record.value());
                    currentOffsets.put(new TopicPartition(record.topic(), record.partition()), new
                            OffsetAndMetadata(record.offset() + 1, "no metadata"));
                }
                consumer.commitAsync(currentOffsets, null);
            }
        } catch (WakeupException e) {

        } catch (Exception e) {
            System.out.println("error is  " + e);
        } finally {
            try {
                consumer.commitSync(currentOffsets);
            } finally {
                consumer.close();
                System.out.println("Consumer closed");
            }
        }
    }
}