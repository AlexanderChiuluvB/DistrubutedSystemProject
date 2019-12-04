package DistributedSystem.miaosha.dao;

import DistributedSystem.miaosha.pojo.Stock;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;


@Mapper
@Repository
public interface StockMapper {

    /**
     * 初始化 DB
     */
    @Update("UPDATE stock SET count = #{count, jdbcType = INTEGER}, sale = 0, version = 0 " +
            "WHERE id = #{id, jdbcType = INTEGER}")
    int initDBBefore(@Param("id") int id, @Param("count")int count);

    @Insert(("INSERT INTO stock (id, name, count, sale, version) VALUES " +
            "(#{id, jdbcType = INTEGER}, #{name, jdbcType = INTEGER}, #{count, jdbcType = INTEGER}, 0, 0)"))
    int createStock(@Param("id") int id, @Param("count") int count, @Param("name") String name);

    @Select("SELECT * FROM stock WHERE id = #{id, jdbcType = INTEGER}")
    Stock selectByPrimaryKey(@Param("id") int id);

    @Update("UPDATE stock SET count = #{count, jdbcType = INTEGER}, name = #{name, jdbcType = VARCHAR}, " +
            "sale = #{sale,jdbcType = INTEGER},version = #{version,jdbcType = INTEGER} " +
            "WHERE id = #{id, jdbcType = INTEGER}")
    int updateByPrimaryKeySelective(Stock stock);
    /**
     * 乐观锁 version
     */
    @Update("UPDATE stock SET count = count - 1, sale = sale + 1, version = version + 1 WHERE " +
            "id = #{id, jdbcType = INTEGER} AND version = #{version, jdbcType = INTEGER}")
    int updateByOptimistic(Stock stock);

}
