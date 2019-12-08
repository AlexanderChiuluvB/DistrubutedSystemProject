package kafka;

import DistributedSystem.miaosha.kafka.kafkaConsumer;
import DistributedSystem.miaosha.kafka.kafkaUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
class kafkaTask implements Runnable {

    private KafkaConsumer<String, String> consumer;
    private final String topic = "mykafka";

    public kafkaTask(int partitionIdx) throws IOException {
        P//roperties props = kafkaUtil.getProperties("consumer");
        this.consumer = new KafkaConsumer<String, String>(props);
        TopicPartition topicPartition = new TopicPartition(topic, partitionIdx);
        this.consumer.assign(Arrays.asList(topicPartition));
    }

    @Override
    public void run() {
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                try {
                    System.out.printf("thread = %s, offset = %d, key = %s, partition = %s, " +
                                    "value = %s \n", Thread.currentThread().getName(),
                            record.offset(), record.key(), record.partition(), record.value());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}


public class kafkaMultiThreadTest {
    private final String topic = "mykafka";
    private final int threadNum = 5;
    private ExecutorService pool = new ThreadPoolExecutor(threadNum, threadNum, 0,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(1024));

    @Test
    public void testMultiThread() {

        try {
            for (int i = 0; i < threadNum; i++) {
                kafkaTask task = new kafkaTask(i);
                pool.execute(task);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Test
    public void testKafkaMultiThreadConsumer() {
        try {
            kafkaConsumer consumer = new kafkaConsumer(5);
            consumer.execute();
        } catch (Exception e){
            e.printStackTrace();
        }
    }


}
**/