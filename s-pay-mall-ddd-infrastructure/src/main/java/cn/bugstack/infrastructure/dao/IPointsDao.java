package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.promotion.Points;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IPointsDao {

    Points queryPointsByUserId(@Param("userId") String userId);

}
