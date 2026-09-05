package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyActivityEntity;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyActivityRepository;
import cn.bugstack.infrastructure.dao.IGroupBuyActivityDao;
import cn.bugstack.infrastructure.dao.po.groupbuy.GroupBuyActivity;
import cn.bugstack.infrastructure.dcc.DCCService;
import cn.bugstack.infrastructure.redis.IRedisService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBitSet;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class GroupBuyActivityRepository implements IGroupBuyActivityRepository {

    @Resource
    private IGroupBuyActivityDao groupBuyActivityDao;
    @Resource
    private IRedisService redisService;
    @Resource
    private DCCService dccService;

    @Override
    public GroupBuyActivityEntity queryGroupBuyActivityByActivityId(Long
                                                                            activityId) {
        GroupBuyActivity activity =
                groupBuyActivityDao.queryGroupBuyActivityByActivityId(activityId);
        if (activity == null) {
            return null;
        }

        return GroupBuyActivityEntity.builder()
                .id(activity.getId())
                .activityId(activity.getActivityId())
                .activityName(activity.getActivityName())
                .productId(activity.getProductId())
                .discountId(activity.getDiscountId())
                .groupType(activity.getGroupType())
                .takeLimitCount(activity.getTakeLimitCount())
                .targetCount(activity.getTargetCount())
                .validTime(activity.getValidTime())
                .status(activity.getStatus())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .tagId(activity.getTagId())
                .tagScope(activity.getTagScope())
                .createTime(activity.getCreateTime())
                .updateTime(activity.getUpdateTime())
                .build();
    }

    @Override
    public GroupBuyActivityEntity queryGroupBuyActivityByProductId(Long productId) {
        GroupBuyActivity activity = groupBuyActivityDao.queryByProductId(productId);
        if (activity == null) {
            return null;
        }
        return toEntity(activity);
    }

    @Override
    public List<GroupBuyActivityEntity> queryGroupBuyActivityByProductIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return groupBuyActivityDao.queryByProductIds(productIds)
                .stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    private GroupBuyActivityEntity toEntity(GroupBuyActivity activity) {
        return GroupBuyActivityEntity.builder()
                .id(activity.getId())
                .activityId(activity.getActivityId())
                .activityName(activity.getActivityName())
                .productId(activity.getProductId())
                .discountId(activity.getDiscountId())
                .groupType(activity.getGroupType())
                .takeLimitCount(activity.getTakeLimitCount())
                .targetCount(activity.getTargetCount())
                .validTime(activity.getValidTime())
                .status(activity.getStatus())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .tagId(activity.getTagId())
                .tagScope(activity.getTagScope())
                .createTime(activity.getCreateTime())
                .updateTime(activity.getUpdateTime())
                .build();
    }

    @Override
    public List<GroupBuyActivityEntity> queryActivityPageByMarketPlan(String marketPlan, int offset, int limit) {
        return groupBuyActivityDao.queryActivityPageByMarketPlan(marketPlan, offset, limit)
                .stream()
                .map(activity -> GroupBuyActivityEntity.builder()
                        .id(activity.getId())
                        .activityId(activity.getActivityId())
                        .activityName(activity.getActivityName())
                        .productId(activity.getProductId())
                        .discountId(activity.getDiscountId())
                        .groupType(activity.getGroupType())
                        .takeLimitCount(activity.getTakeLimitCount())
                        .targetCount(activity.getTargetCount())
                        .validTime(activity.getValidTime())
                        .status(activity.getStatus())
                        .startTime(activity.getStartTime())
                        .endTime(activity.getEndTime())
                        .tagId(activity.getTagId())
                        .tagScope(activity.getTagScope())
                        .createTime(activity.getCreateTime())
                        .updateTime(activity.getUpdateTime())
                        .discountName(activity.getDiscountName())
                        .marketPlan(activity.getMarketPlan())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public long countActivityPageByMarketPlan(String marketPlan) {
        return groupBuyActivityDao.countActivityPageByMarketPlan(marketPlan);
    }

    @Override
    public boolean withinTagCrowdRange(String tagId, String userId) {
        if (StringUtils.isBlank(tagId)) {
            return true;
        }
        RBitSet bitSet = redisService.getBitSet(tagId);
        if (!bitSet.isExists()) {
            return true;
        }
        return bitSet.get(redisService.getIndexFromUserId(userId));
    }

    @Override
    public boolean downgradeSwitch() {
        return dccService.isDowngradeSwitch();
    }

    @Override
    public boolean cutRange(String userId) {
        return dccService.isCutRange(userId);
    }
}
