package kafka;

import org.junit.Test;

import java.io.FileNotFoundException;

import static org.junit.Assert.*;

public class kafkaUtilTest {
    @Test
    public void  test() throws FileNotFoundException {
        kafkaUtil.getProperties("consumer");
    }

}