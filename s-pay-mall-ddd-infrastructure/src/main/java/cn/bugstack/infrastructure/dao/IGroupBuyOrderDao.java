package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.groupbuy.GroupBuyOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IGroupBuyOrderDao {

    void insert(GroupBuyOrder order);

    GroupBuyOrder queryGroupBuyOrderByOutTradeNo(@Param("userId") String userId, @Param("outTradeNo") String outTradeNo);

    GroupBuyOrder queryUserActiveOrder(@Param("userId") String userId, @Param("activityId") Long activityId);

    int queryOrderCountByUserIdAndActivityId(@Param("userId") String userId, @Param("activityId") Long activityId);

    int updateOrderStatus2Complete(@Param("userId") String userId, @Param("outTradeNo") String outTradeNo);

    List<String> queryCompleteOutTradeNoListByTeamId(String teamId);

    List<String> queryUserIdListByTeamId(@Param("teamId") String teamId);

    List<String> queryTimeOutRefundOrderList();

    int updateOrderStatus2Refund(@Param("outTradeNo") String outTradeNo);

    long countByStatusAndUserId(@Param("status") Integer status, @Param("userId") String userId);

    List<GroupBuyOrder> queryPageByStatusAndUserId(@Param("status") Integer status,
                                                   @Param("userId") String userId,
                                                   @Param("offset") Integer offset,
                                                   @Param("limit") Integer limit);
}
