package dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Update;
import pojo.StockOrder;

/*
与数据库链接的接口
 */
@Mapper()
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
