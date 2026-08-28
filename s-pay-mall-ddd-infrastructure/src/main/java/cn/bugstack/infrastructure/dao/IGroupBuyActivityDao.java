package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.groupbuy.GroupBuyActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IGroupBuyActivityDao {

    GroupBuyActivity queryGroupBuyActivityByActivityId(@Param("activityId") Long
                                                               activityId);
    GroupBuyActivity queryByProductId(@Param("productId") Long productId);
    List<GroupBuyActivity> queryGroupBuyActivityList();

    long countByTagId(@Param("tagId") String tagId);

    List<GroupBuyActivity> queryActivityPageByMarketPlan(@Param("marketPlan") String marketPlan,
                                                         @Param("offset") int offset,
                                                         @Param("limit") int limit);

    long countActivityPageByMarketPlan(@Param("marketPlan") String marketPlan);
}
