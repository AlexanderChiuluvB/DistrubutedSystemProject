package DistributedSystem.miaosha.service.impl;

import DistributedSystem.miaosha.kafka.kafkaProducer;
import DistributedSystem.miaosha.pojo.Stock;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

public class BgThread implements Runnable{
    private Stock stock;
    private String kafkaTopic;
    private Gson gson;
    private static int id=0;


    public BgThread(Stock s,String k,Gson g){
        this.stock=s;
        this.kafkaTopic=k;
        this.gson=g;
    }

    public void run(){
        try {
            System.out.println("To Kafka :"+(++id));
            kafkaProducer.sendMessage(Collections.singletonMap(kafkaTopic, gson.toJson(stock)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
