package cn.bugstack.domain.groupbuy.service.order;

import cn.bugstack.domain.groupbuy.model.aggregate.GroupBuyOrderAggregate;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyOrderEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRuleCommandEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRuleFilterFeedBackEntity;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyOrderRepository;
import cn.bugstack.domain.groupbuy.service.rule.factory.GroupBuyRuleFilterFactory;
import cn.bugstack.types.design.framework.link.multilink.chain.BusinessLinkedList;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class GroupBuyOrderService implements IGroupBuyOrderService {

    @Resource
    private IGroupBuyOrderRepository groupBuyRepository;
    @Resource(name = "groupBuyRuleFilter")
    private BusinessLinkedList<GroupBuyRuleCommandEntity,
                GroupBuyRuleFilterFactory.DynamicContext,
                GroupBuyRuleFilterFeedBackEntity> groupBuyRuleFilter;


    @Override
    public GroupBuyOrderEntity lockGroupBuyOrder(GroupBuyOrderAggregate aggregate) {
        if (aggregate == null || aggregate.getTrialResult() == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "拼团锁单参数不能为空");
        }

        GroupBuyOrderEntity existing = groupBuyRepository.queryGroupBuyOrderByOutTradeNo(
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

    @Override
    public List<GroupBuyOrderEntity> queryPageByStatusAndUserId(Integer status, String userId, Integer pageNo, Integer pageSize) {
        int safePageNo = pageNo == null || pageNo <= 0 ? 1 : pageNo;
        int safePageSize = pageSize == null || pageSize <= 0 ? 10 : Math.min(pageSize, 100);
        int offset = (safePageNo - 1) * safePageSize;
        return groupBuyRepository.queryPageByStatusAndUserId(status, userId, offset, safePageSize);
    }

    @Override
    public long countByStatusAndUserId(Integer status, String userId) {
        return groupBuyRepository.countByStatusAndUserId(status, userId);
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
