package DistributedSystem.miaosha.kafka;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;
import java.util.Properties;

public class kafkaUtil {

    private static final String kafkaPath = "C:\\Users\\AlexanderChiu\\Documents\\DistrubutedSystemProject\\demo\\src\\main\\resources\\kafka-properties.json";

    private static JsonObject getKafkaConfig(String name) throws FileNotFoundException {
        JsonParser parser = new JsonParser();
        JsonElement parse = parser.parse(new FileReader(kafkaPath));
        JsonObject jsonObject = parse.getAsJsonObject().getAsJsonObject(name);
        System.out.println(jsonObject);
        return jsonObject;
    }

    public static Properties getProperties(String name) throws FileNotFoundException {
        JsonObject config = kafkaUtil.getKafkaConfig(name);
        Properties properties = new Properties();
        for (Map.Entry<String, JsonElement> entry : config.entrySet()) {
            properties.put(entry.getKey(), entry.getValue().getAsString());
        }
        return properties;
    }

}