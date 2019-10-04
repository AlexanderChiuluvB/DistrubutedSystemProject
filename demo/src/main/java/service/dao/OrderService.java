package service.dao;

import org.springframework.context.annotation.Bean;
import pojo.Stock;


public interface OrderService {

    /**
     * 清空订单表
     */
    int delOrderDB();

    /**
     *
     * @param stock
     */
    int createOrderAndSendToDB(Stock stock) throws Exception;

    /**
     * 检查redis库存然后发送下单消息给kafka
     *
     * @param sid
     * @return
     */
    void checkRedisAndSendToKafka(int sid) throws Exception;




}