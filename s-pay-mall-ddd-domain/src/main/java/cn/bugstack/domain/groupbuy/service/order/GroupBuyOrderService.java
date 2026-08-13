package cn.bugstack.domain.groupbuy.service.order;

import cn.bugstack.domain.groupbuy.model.aggregate.GroupBuyOrderAggregate;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyOrderEntity;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyRepository;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class GroupBuyOrderService implements IGroupBuyOrderService {

    @Resource
    private IGroupBuyRepository groupBuyRepository;

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

        Integer takeLimitCount = aggregate.getTrialResult().getTakeLimitCount();
        if (takeLimitCount != null) {
            Integer count = groupBuyRepository.countUserGroupBuyOrders(
                    aggregate.getUserId(),
                    aggregate.getTrialResult().getActivityId()
            );
            if (count >= takeLimitCount) {
                throw new AppException(ResponseCode.E0103);
            }
        }

        return groupBuyRepository.lockGroupBuyOrder(aggregate);
    }
}
