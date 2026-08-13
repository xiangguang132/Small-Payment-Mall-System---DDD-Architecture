package cn.bugstack.domain.groupbuy.service.rule.filter;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyActivityEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRuleCommandEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRuleFilterFeedBackEntity;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyRepository;
import cn.bugstack.domain.groupbuy.service.rule.factory.GroupBuyRuleFilterFactory;
import cn.bugstack.types.design.framework.link.multilink.handler.ILogicHandler;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 规则链
 * 规则过滤节点-用户参与活动限制节点
 * 入参 动态上下文 出参
 */
@Service
public class UserTakeLimitRuleFilter implements ILogicHandler<GroupBuyRuleCommandEntity, GroupBuyRuleFilterFactory.DynamicContext, GroupBuyRuleFilterFeedBackEntity> {

    @Resource
    private IGroupBuyRepository groupBuyRepository;

    @Override
    public GroupBuyRuleFilterFeedBackEntity apply(GroupBuyRuleCommandEntity requestParameter, GroupBuyRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        GroupBuyActivityEntity activity = dynamicContext.getActivity();
        Integer takeLimitCount = activity == null ? null : activity.getTakeLimitCount();
        Integer count = groupBuyRepository.countUserGroupBuyOrders(
                requestParameter.getUserId(),
                requestParameter.getActivityId());
        int current = count == null ? 0 : count;

        if (takeLimitCount != null && current >= takeLimitCount) {
            throw new AppException(ResponseCode.E0103);
        }

        return GroupBuyRuleFilterFeedBackEntity.builder()
                .userTakeOrderCount(current)
                .build();
    }
}
