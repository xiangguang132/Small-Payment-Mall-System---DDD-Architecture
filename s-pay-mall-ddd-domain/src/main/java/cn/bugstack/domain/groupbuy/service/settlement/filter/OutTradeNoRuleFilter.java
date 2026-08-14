package cn.bugstack.domain.groupbuy.service.settlement.filter;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyOrderEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuySettlementCommandEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuySettlementFeedBackEntity;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyOrderRepository;
import cn.bugstack.domain.groupbuy.service.settlement.factory.GroupBuySettlementRuleFilterFactory;
import cn.bugstack.types.design.framework.link.multilink.handler.ILogicHandler;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 过滤节点-外部订单
 */
@Service
public class OutTradeNoRuleFilter implements ILogicHandler<GroupBuySettlementCommandEntity, GroupBuySettlementRuleFilterFactory.DynamicContext, GroupBuySettlementFeedBackEntity> {

    @Resource
    private IGroupBuyOrderRepository groupBuyRepository;

    @Override
    public GroupBuySettlementFeedBackEntity apply(GroupBuySettlementCommandEntity requestParameter, GroupBuySettlementRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        // 获取订单并判空
        GroupBuyOrderEntity orderEntity = groupBuyRepository.queryGroupBuyOrderByOutTradeNo(
                requestParameter.getUserId(),
                requestParameter.getOutTradeNo()
        );
        if (orderEntity == null || orderEntity.getStatus() == null || orderEntity.getStatus() != 0) {
            throw new AppException(ResponseCode.E0004.getCode(), ResponseCode.E0004.getInfo());
        }

        dynamicContext.setGroupBuyOrderEntity(orderEntity);

        return next(requestParameter, dynamicContext);
    }
}
