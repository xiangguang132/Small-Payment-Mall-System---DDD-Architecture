package cn.bugstack.domain.groupbuy.service.refund.filter;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyOrderEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderBehaviorEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderCommandEntity;
import cn.bugstack.domain.groupbuy.model.valobj.GroupBuyOrderStatusEnumVO;
import cn.bugstack.domain.groupbuy.service.refund.business.IGroupBuyRefundOrderStrategy;
import cn.bugstack.domain.groupbuy.service.refund.factory.GroupBuyRefundOrderRuleFilterFactory;
import cn.bugstack.types.design.framework.link.multilink.handler.ILogicHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@Service
public class RefundOrderNodeFilter implements ILogicHandler<GroupBuyRefundOrderCommandEntity, GroupBuyRefundOrderRuleFilterFactory.DynamicContext, GroupBuyRefundOrderBehaviorEntity> {

    @Resource
    private Map<String, IGroupBuyRefundOrderStrategy> refundGroupBuyOrderStrategyMap;

    @Override
    public GroupBuyRefundOrderBehaviorEntity apply(GroupBuyRefundOrderCommandEntity requestParameter, GroupBuyRefundOrderRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        // 获取上下文 订单数据
        GroupBuyOrderEntity groupBuyOrderEntity = dynamicContext.getGroupBuyOrderEntity();
        GroupBuyOrderStatusEnumVO groupBuyOrderStatusEnumVO = groupBuyOrderEntity.getStatus();
        // 获取执行策略 - 解析并路由
        // 执行退单操作
        // 返回退单实体
        return null;
    }
}
