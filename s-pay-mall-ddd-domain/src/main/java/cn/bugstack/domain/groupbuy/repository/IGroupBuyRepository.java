package cn.bugstack.domain.groupbuy.repository;

import cn.bugstack.domain.groupbuy.model.aggregate.GroupBuyOrderAggregate;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyOrderEntity;

public interface IGroupBuyRepository {

    GroupBuyOrderEntity queryGroupBuyOrderByBizId(String userId, String bizId);

    Integer countUserGroupBuyOrders(String userId, Long activityId);

    GroupBuyOrderEntity lockGroupBuyOrder(GroupBuyOrderAggregate aggregate);

}
