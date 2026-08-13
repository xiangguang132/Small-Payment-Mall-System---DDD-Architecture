package cn.bugstack.domain.groupbuy.service.rule.filter;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyActivityEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyOrderEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRuleCommandEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRuleFilterFeedBackEntity;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyActivityRepository;
import cn.bugstack.domain.groupbuy.service.rule.factory.GroupBuyRuleFilterFactory;
import cn.bugstack.types.design.framework.link.multilink.handler.ILogicHandler;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 规则链
 * 规则过滤节点-可用性节点
 * 入参 动态上下文 出参
 */
@Service
public class ActivityUsabilityRuleFilter implements ILogicHandler<GroupBuyRuleCommandEntity, GroupBuyRuleFilterFactory.DynamicContext, GroupBuyRuleFilterFeedBackEntity> {

    private static final Integer EFFECTIVE_STATUS = 1;

    @Resource
    private IGroupBuyActivityRepository activityRepository;

    @Override
    public GroupBuyRuleFilterFeedBackEntity apply(GroupBuyRuleCommandEntity requestParameter, GroupBuyRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        // 判单活动存在性
        if (requestParameter.getActivityId() == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "拼团活动ID不能为空");
        }
        // 依据activityId获取活动详情
        GroupBuyActivityEntity activity = activityRepository.queryGroupBuyActivityByActivityId(requestParameter.getActivityId());
        if (activity == null || !EFFECTIVE_STATUS.equals(activity.getStatus())) {
            throw new AppException(ResponseCode.E0101);
        }

        // 判断是否在活动范围内
        LocalDateTime now = LocalDateTime.now();
        if ((activity.getStartTime() != null && now.isBefore(activity.getStartTime()))
                || (activity.getEndTime() != null && now.isAfter(activity.getEndTime()))) {
            throw new AppException(ResponseCode.E0102);
        }

        dynamicContext.setActivity(activity);

        // 将活动继续给下一个链点
        return next(requestParameter, dynamicContext);
    }
}
