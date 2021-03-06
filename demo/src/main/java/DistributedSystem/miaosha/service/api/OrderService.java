package DistributedSystem.miaosha.service.api;


import DistributedSystem.miaosha.pojo.Stock;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderService {

    /**
     * 清空订单表
     */
    int delOrderDB();

    /**
     * 令牌桶算法限流
     */
    boolean acquireTokenFromRedisBucket(Integer sid)throws Exception;

    /**
     * 检查redis库存然后发送下单消息给kafka
     *
     * @param sid
     * @return
     */
    boolean checkRedisAndSendToKafka(Integer sid) throws Exception;

    /**
     * kafka异步消费信息,更新数据库和Redis
     *
     * @param stock
     */
    int createOrderAndSendToDB(Stock stock) throws Exception;

}