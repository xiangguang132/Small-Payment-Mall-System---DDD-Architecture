package cn.bugstack.domain.groupbuy.repository;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyActivityEntity;

public interface IGroupBuyActivityRepository {

    GroupBuyActivityEntity queryGroupBuyActivityByActivityId(Long activityId);

}
