package DistributedSystem.miaosha.service.api;


import DistributedSystem.miaosha.pojo.Stock;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.stereotype.Repository;

@Mapper
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