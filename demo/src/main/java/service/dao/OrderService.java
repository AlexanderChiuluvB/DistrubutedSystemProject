package service.dao;

import pojo.Stock;

public interface OrderService {

    /**
     * Kafka 消费消息
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