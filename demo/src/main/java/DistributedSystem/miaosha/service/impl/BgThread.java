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


    public BgThread(Stock s,String k,Gson g){
        this.stock=s;
        this.kafkaTopic=k;
        this.gson=g;
    }

    public void run(){
        try {
            kafkaProducer.sendMessage(Collections.singletonMap(kafkaTopic, gson.toJson(stock)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
