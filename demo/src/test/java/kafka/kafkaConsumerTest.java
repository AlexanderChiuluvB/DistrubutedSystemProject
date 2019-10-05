package kafka;

import DistributedSystem.miaosha.kafka.kafkaConsumer;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;


public class kafkaConsumerTest {

    @Test
    public void consumerTest() throws FileNotFoundException {
        ArrayList<String> topics = new ArrayList<>();
        topics.add("mykafka");
        kafkaConsumer.commit(topics);
    }

}