package DistributedSystem.miaosha.kafka;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Properties;

public class kafkaProducer {

    public static void sendMessage(Map<String, String> topicMsg) throws Exception {
        //Properties properties = kafkaUtil.getProperties("producer");
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "172.101.8.2:9092,172.101.8.3:9092,172.101.8.4:9092,172.101.8.5:9092,172.101.8.6:9092");
        properties.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
        properties.put("compression.type","gzip");
        properties.put("max.request.size", "10485760");
        properties.put("batch.size","1600");
        properties.put("buffer.memory","536870912");
        properties.put("max.block.ms","500");
        properties.put("acks","0");

        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);
        for (Map.Entry<String, String> entry : topicMsg.entrySet()) {
            String topic = entry.getKey();
            String msg = entry.getValue();
            ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, msg);
            //异步回调
            /*
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
            */
            producer.send(record);
        }
        producer.flush();
        producer.close();

    }


}