package cn.bugstack.domain.groupbuy.service.order;

import cn.bugstack.domain.groupbuy.model.aggregate.GroupBuyOrderAggregate;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyOrderEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRuleCommandEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRuleFilterFeedBackEntity;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyRepository;
import cn.bugstack.domain.groupbuy.service.rule.factory.GroupBuyRuleFilterFactory;
import cn.bugstack.types.design.framework.link.multilink.chain.BusinessLinkedList;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class GroupBuyOrderService implements IGroupBuyOrderService {

    @Resource
    private IGroupBuyRepository groupBuyRepository;
    @Resource(name = "groupBuyRuleFilter")
    private BusinessLinkedList<GroupBuyRuleCommandEntity,
                GroupBuyRuleFilterFactory.DynamicContext,
                GroupBuyRuleFilterFeedBackEntity> groupBuyRuleFilter;


    @Override
    public GroupBuyOrderEntity lockGroupBuyOrder(GroupBuyOrderAggregate aggregate) {
        if (aggregate == null || aggregate.getTrialResult() == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "拼团锁单参数不能为空");
        }

        GroupBuyOrderEntity existing = groupBuyRepository.queryGroupBuyOrderByBizId(
                aggregate.getUserId(),
                aggregate.getOutTradeNo()
        );
        if (existing != null) {
            return existing;
        }

        GroupBuyRuleFilterFeedBackEntity filterFeedBackEntity = applyRule(
                GroupBuyRuleCommandEntity.builder()
                        .userId(aggregate.getUserId())
                        .activityId(aggregate.getTrialResult().getActivityId())
                        .outTradeNo(aggregate.getOutTradeNo())
                        .build()
        );

        if (filterFeedBackEntity == null) {
            throw new AppException(ResponseCode.UN_ERROR, "拼团活动交易规则过滤链未返回结果");
        }

        return groupBuyRepository.lockGroupBuyOrder(aggregate);
    }

    // applyRule 开始走规则链
    private GroupBuyRuleFilterFeedBackEntity applyRule(GroupBuyRuleCommandEntity command) {
        try {
            return groupBuyRuleFilter.apply(command, new GroupBuyRuleFilterFactory.DynamicContext());
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ResponseCode.UN_ERROR, "拼团活动交易规则过滤失败", e);
        }
    }

}
