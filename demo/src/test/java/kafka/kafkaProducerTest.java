package kafka;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.HashMap;

public class kafkaProducerTest {

    @Test
    public void testKafkaProducer() throws FileNotFoundException {
        HashMap<String, String> mp = new HashMap<>();
        for(int i = 0; i < 500; i++) {
            mp.put("mykafka", "message-" + i);
            kafkaProducer.sendMessage(mp);
        }
    }

}