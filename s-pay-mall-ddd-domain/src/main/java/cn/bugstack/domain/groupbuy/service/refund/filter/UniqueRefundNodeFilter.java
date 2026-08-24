package cn.bugstack.domain.groupbuy.service.refund.filter;

import cn.bugstack.domain.groupbuy.model.entity.*;
import cn.bugstack.domain.groupbuy.model.valobj.GroupBuyOrderStatusEnumVO;
import cn.bugstack.domain.groupbuy.service.refund.factory.GroupBuyRefundOrderRuleFilterFactory;
import cn.bugstack.types.design.framework.link.multilink.handler.ILogicHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 幂等----重复退单检查过滤器
 * 实现幂等性控制防止重复退单
 */
@Slf4j
@Service
public class UniqueRefundNodeFilter implements ILogicHandler<GroupBuyRefundOrderCommandEntity, GroupBuyRefundOrderRuleFilterFactory.DynamicContext, GroupBuyRefundOrderBehaviorEntity> {

    @Override
    public GroupBuyRefundOrderBehaviorEntity apply(GroupBuyRefundOrderCommandEntity requestParameter, GroupBuyRefundOrderRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        log.info("逆向流程-退单操作，重复退单检查 userId:{} outTradeNo:{}", requestParameter.getUserId(), requestParameter.getOutTradeNo() );
        // 获取上下文数据
        // 订单、拼团配置和拼团活动
        GroupBuyOrderEntity groupBuyOrderEntity = dynamicContext.getGroupBuyOrderEntity();
        GroupBuyOrderStatusEnumVO statusEnumVO = GroupBuyOrderStatusEnumVO.valueOf(groupBuyOrderEntity.getStatus());
        // 幂等-如果单子已经是已取消/退单态，直接返回这个实体
        if (GroupBuyOrderStatusEnumVO.CANCELED.equals(statusEnumVO)) {
            log.info("逆向流程-退单操作，重复退单命中 userId:{} outTradeNo:{} status:{}", requestParameter.getUserId(), requestParameter.getOutTradeNo(), groupBuyOrderEntity.getStatus());
            return GroupBuyRefundOrderBehaviorEntity.builder()
                    .userId(groupBuyOrderEntity.getUserId())
                    .orderId(groupBuyOrderEntity.getOrderId())
                    .teamId(groupBuyOrderEntity.getTeamId())
                    .activityId(groupBuyOrderEntity.getActivityId())
                    .success(true)
                    .message("退单操作(幂等-重复退单)校验完成")
                    .build();
        }

        dynamicContext.setUniquePass(true);
        return null;
    }
}
