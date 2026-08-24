package cn.bugstack.domain.groupbuy.repository;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyActivityEntity;

public interface IGroupBuyActivityRepository {

    GroupBuyActivityEntity queryGroupBuyActivityByActivityId(Long activityId);

    GroupBuyActivityEntity queryGroupBuyActivityByProductId(Long productId);

    boolean withinTagCrowdRange(String tagId, String userId);

    boolean downgradeSwitch();

    boolean cutRange(String userId);
}