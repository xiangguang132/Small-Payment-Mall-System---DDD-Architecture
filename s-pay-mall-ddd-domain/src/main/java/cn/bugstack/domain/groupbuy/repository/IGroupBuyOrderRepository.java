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
     * 查询用户在该活动下进行中（待付款 status=0）的拼团订单，业务防重用
     * @param userId 用户ID
     * @param activityId 活动ID
     * @return 进行中的订单；不存在返回 null
     */
    GroupBuyOrderEntity queryUserActiveOrder(String userId, Long activityId);

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

    /**
     * 分页查询拼团订单
     * @param status 活动状态（null不过滤）
     * @param userId  用户ID（null/空不过滤）
     * @param offset 偏移量
     * @param limit  每页条数
     * @return 订单列表
     */
    List<GroupBuyOrderEntity> queryPageByStatusAndUserId(Integer status, String userId, Integer offset, Integer limit);

    /**
     * 统计拼团订单数量
     * @param status 活动状态（null不过滤）
     * @param userId  用户ID（null/空不过滤）
     * @return 总数
     */
    long countByStatusAndUserId(Integer status, String userId);
}

