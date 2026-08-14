package cn.bugstack.domain.groupbuy.repository;

import cn.bugstack.domain.groupbuy.model.aggregate.GroupBuyOrderAggregate;
import cn.bugstack.domain.groupbuy.model.aggregate.GroupBuyTeamSettlementAggregate;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyOrderEntity;

public interface IGroupBuyOrderRepository {

    GroupBuyOrderEntity queryGroupBuyOrderByOutTradeNo(String userId, String outTradeNo);

    Integer countUserGroupBuyOrders(String userId, Long activityId);

    GroupBuyOrderEntity lockGroupBuyOrder(GroupBuyOrderAggregate aggregate);

    boolean settlementGroupBuyOrder(GroupBuyTeamSettlementAggregate aggregate);

}
