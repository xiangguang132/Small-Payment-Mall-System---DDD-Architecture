package cn.bugstack.domain.groupbuy.repository;

import cn.bugstack.domain.groupbuy.model.aggregate.GroupBuyOrderAggregate;
import cn.bugstack.domain.groupbuy.model.aggregate.GroupBuyTeamSettlementAggregate;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyNotifyTaskEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyOrderEntity;

import java.util.List;

public interface IGroupBuyOrderRepository {

    /**
     * 依据 userId 和 outTradeNo 查询拼团订单
     * @param userId
     * @param outTradeNo
     * @return
     */
    GroupBuyOrderEntity queryGroupBuyOrderByOutTradeNo(String userId, String outTradeNo);

    /**
     * 统计该用户的拼团订单数量
     * @param userId
     * @param activityId
     * @return
     */
    Integer countUserGroupBuyOrders(String userId, Long activityId);

    /**
     * 拼团锁单
     * @param aggregate
     * @return
     */
    GroupBuyOrderEntity lockGroupBuyOrder(GroupBuyOrderAggregate aggregate);

    /**
     * 结算订单
     * @param aggregate
     * @return
     */
    GroupBuyNotifyTaskEntity settlementGroupBuyOrder(GroupBuyTeamSettlementAggregate aggregate);

    /**
     * 查询团内所有用户ID列表
     * @param teamId
     * @return
     */
    List<String> queryUserIdListByTeamId(String teamId);

    /**
     * 查询超时订单
     * @return
     */
    List<String> queryTimeOutRefundOrderList();

    /**
     * 将拼团订单状态更新为已退款（status=2）
     * @param outTradeNo 外部交易单号
     * @return 更新行数（0=未找到或状态不符，1=成功）
     */
    int updateOrderStatus2Refund(String outTradeNo);
}

