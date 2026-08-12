package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyActivityEntity;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyActivityRepository;
import cn.bugstack.infrastructure.dao.IGroupBuyActivityDao;
import cn.bugstack.infrastructure.dao.po.GroupBuyActivity;
import cn.bugstack.infrastructure.redis.IRedisService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBitSet;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class GroupBuyActivityRepository implements IGroupBuyActivityRepository {

    @Resource
    private IGroupBuyActivityDao groupBuyActivityDao;
    @Resource
    private IRedisService redisService;

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
}
