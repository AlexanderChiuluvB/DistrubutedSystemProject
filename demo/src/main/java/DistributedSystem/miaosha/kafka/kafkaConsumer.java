package DistributedSystem.miaosha.kafka;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class kafkaConsumer {

    private final static int minBatchSize = 50;

    public static void commit(List<String> topics) throws FileNotFoundException {

        Properties properties = kafkaUtil.getProperties("consumer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);
        consumer.subscribe(topics);
        List<ConsumerRecord<String, String>> buffer = new ArrayList<>();
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(1000);
            for (ConsumerRecord<String, String> record : records) {
                buffer.add(record);
            }
            if (buffer.size() > minBatchSize) {
                //do something
                printBuffer(buffer);
                consumer.commitAsync(new OffsetCommitCallback() {
                    @Override
                    public void onComplete(Map<TopicPartition, OffsetAndMetadata> map, Exception e) {
                        if (e != null) {
                            e.printStackTrace();
                        } else {
                            for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : map.entrySet()) {
                                System.out.println(entry.getKey());
                                System.out.println(entry.getValue());
                            }
                        }
                    }
                });
                buffer.clear();
            }
        }
    }

    private static void printBuffer(List<ConsumerRecord<String, String>> buffer) {
        buffer.stream().map(ConsumerRecord::value).forEach(System.out::println);
    }


}