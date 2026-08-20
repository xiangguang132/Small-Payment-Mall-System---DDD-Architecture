package cn.bugstack.domain.payment.service.rule.filter;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuySettlementCommandEntity;
import cn.bugstack.domain.groupbuy.service.settlement.IGroupBuySettlementService;
import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.payment.model.entity.AlipayNotifyProcessCommandEntity;
import cn.bugstack.domain.payment.model.entity.AlipayNotifyProcessFeedBackEntity;
import cn.bugstack.domain.payment.adapter.repository.IAlipayNotifyTaskRepository;
import cn.bugstack.domain.payment.service.rule.factory.AlipayNotifyProcessRuleFilterFactory;
import cn.bugstack.types.design.framework.link.multilink.handler.ILogicHandler;
import cn.bugstack.types.enums.OrderTypeEnum;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 过滤节点-拼团订单处理
 */
@Slf4j
@Service
public class GroupBuyOrderFilter implements ILogicHandler<
        AlipayNotifyProcessCommandEntity,
        AlipayNotifyProcessRuleFilterFactory.DynamicContext,
        AlipayNotifyProcessFeedBackEntity> {

    @Resource
    private IGroupBuySettlementService groupBuySettlementService;

    @Resource
    private IAlipayNotifyTaskRepository alipayNotifyTaskRepository;

    @Override
    public AlipayNotifyProcessFeedBackEntity apply(
            AlipayNotifyProcessCommandEntity command,
            AlipayNotifyProcessRuleFilterFactory.DynamicContext dynamicContext) throws Exception {

        PayOrderEntity payOrder = dynamicContext.getPayOrder();

        if (OrderTypeEnum.GROUP_BUY.equals(payOrder.getOrderType())) {
            LocalDateTime payTime = dynamicContext.getParams() == null
                    ? null
                    : parseAlipayTime(dynamicContext.getParams().getString("gmt_payment"));

            GroupBuySettlementCommandEntity settlementCommand = GroupBuySettlementCommandEntity.builder()
                    .userId(payOrder.getUserId())
                    .outTradeNo(command.getOutTradeNo())
                    .payTime(payTime)
                    .source("ALIPAY")
                    .channel("ALIPAY")
                    .build();

            groupBuySettlementService.settlementGroupBuyOrder(settlementCommand);
            alipayNotifyTaskRepository.updatedAlipayNotifyTaskSuccess(command.getOutTradeNo());
            log.info("支付宝通知任务-拼团订单处理完成 outTradeNo:{}", command.getOutTradeNo());

            return AlipayNotifyProcessFeedBackEntity.builder()
                    .outTradeNo(command.getOutTradeNo())
                    .handled(true)
                    .build();
        }

        throw new AppException(ResponseCode.UN_ERROR, "不支持的订单类型");
    }

    private LocalDateTime parseAlipayTime(String alipayTime) {
        if (alipayTime == null || alipayTime.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(alipayTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            log.warn("支付宝支付时间解析失败 alipayTime:{}", alipayTime, e);
            return null;
        }
    }

}
