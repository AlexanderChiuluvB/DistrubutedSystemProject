package kafka;

import DistributedSystem.miaosha.kafka.kafkaUtil;
import org.junit.Test;

import java.io.FileNotFoundException;


public class kafkaUtilTest {
    @Test
    public void  test() throws FileNotFoundException {
        kafkaUtil.getProperties("consumer");
    }

}