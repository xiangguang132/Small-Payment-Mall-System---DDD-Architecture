package cn.bugstack.domain.groupbuy.repository;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyActivityEntity;

import java.util.List;

public interface IGroupBuyActivityRepository {

    GroupBuyActivityEntity queryGroupBuyActivityByActivityId(Long activityId);

    GroupBuyActivityEntity queryGroupBuyActivityByProductId(Long productId);

    List<GroupBuyActivityEntity> queryActivityPageByMarketPlan(String marketPlan, int offset, int limit);

    long countActivityPageByMarketPlan(String marketPlan);

    boolean withinTagCrowdRange(String tagId, String userId);

    boolean downgradeSwitch();

    boolean cutRange(String userId);
}