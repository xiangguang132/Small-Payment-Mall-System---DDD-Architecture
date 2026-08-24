package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.groupbuy.GroupBuyOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IGroupBuyOrderDao {

    void insert(GroupBuyOrder order);

    GroupBuyOrder queryGroupBuyOrderByOutTradeNo(@Param("userId") String userId, @Param("outTradeNo") String outTradeNo);

    int queryOrderCountByUserIdAndActivityId(@Param("userId") String userId, @Param("activityId") Long activityId);

    int updateOrderStatus2Complete(@Param("userId") String userId, @Param("outTradeNo") String outTradeNo);

    List<String> queryCompleteOutTradeNoListByTeamId(String teamId);

    List<String> queryUserIdListByTeamId(@Param("teamId") String teamId);
}
