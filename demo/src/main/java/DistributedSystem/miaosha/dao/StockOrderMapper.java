package DistributedSystem.miaosha.dao;

import DistributedSystem.miaosha.pojo.StockOrder;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;


/*
与数据库链接的接口
 */
@Mapper
@Repository
public interface StockOrderMapper {

    @Insert("INSERT INTO stock_order (id, sid, name, create_time) VALUES " +
            "(#{id, jdbcType = INTEGER}, #{sid, jdbcType = INTEGER}, #{name, jdbcType = VARCHAR}, #{createTime, jdbcType = TIMESTAMP})")
    int insertToDB(StockOrder order);

    /**
     * 清空订单表
     * 成功为 0，失败为 -1
     */
    @Update("TRUNCATE TABLE stock_order")
    int clearDB();
}
