package kafka;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Properties;

public class kafkaProducer {

    public static void sendMessage(Map<String, String> topicMsg) throws FileNotFoundException {
        Properties properties = kafkaUtil.getProperties("producer");
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);
        for (Map.Entry<String, String> entry : topicMsg.entrySet()) {
            String topic = entry.getKey();
            String msg = entry.getValue();
            ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, msg);

            //异步回调
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    if (e != null) {
                        e.printStackTrace();
                    } else {
                        System.out.println("The offset we sent is " + recordMetadata);
                    }
                }
            });
        }
        producer.flush();
        producer.close();

    }


}
