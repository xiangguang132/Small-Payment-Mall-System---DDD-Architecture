package cn.bugstack.domain.payment.service.rule.filter;

import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.order.service.IOrderService;
import cn.bugstack.domain.payment.model.entity.AlipayNotifyProcessCommandEntity;
import cn.bugstack.domain.payment.model.entity.AlipayNotifyProcessFeedBackEntity;
import cn.bugstack.domain.payment.adapter.repository.IAlipayNotifyTaskRepository;
import cn.bugstack.domain.payment.service.rule.factory.AlipayNotifyProcessRuleFilterFactory;
import cn.bugstack.types.design.framework.link.multilink.handler.ILogicHandler;
import cn.bugstack.types.enums.OrderTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 过滤节点-普通订单处理
 */
@Slf4j
@Service
public class DirectOrderFilter implements ILogicHandler<
        AlipayNotifyProcessCommandEntity,
        AlipayNotifyProcessRuleFilterFactory.DynamicContext,
        AlipayNotifyProcessFeedBackEntity> {

    @Resource
    private IAlipayNotifyTaskRepository alipayNotifyTaskRepository;

    @Resource
    private IOrderService orderService;

    @Override
    public AlipayNotifyProcessFeedBackEntity apply(
            AlipayNotifyProcessCommandEntity command,
            AlipayNotifyProcessRuleFilterFactory.DynamicContext dynamicContext) throws Exception {

        PayOrderEntity payOrder = dynamicContext.getPayOrder();

        if (OrderTypeEnum.DIRECT.equals(payOrder.getOrderType())) {
            LocalDateTime payTime = dynamicContext.getParams() == null
                    ? null
                    : parseAlipayTime(dynamicContext.getParams().getString("gmt_payment"));
            Date outTradeTime = payTime == null
                    ? null
                    : Date.from(payTime.atZone(ZoneId.systemDefault()).toInstant());

            orderService.changeOrderPaySuccess(command.getOutTradeNo(), outTradeTime);
            alipayNotifyTaskRepository.updatedAlipayNotifyTaskSuccess(command.getOutTradeNo());
            log.info("支付宝通知任务-普通订单处理完成 outTradeNo:{}", command.getOutTradeNo());

            return AlipayNotifyProcessFeedBackEntity.builder()
                    .outTradeNo(command.getOutTradeNo())
                    .handled(true)
                    .build();
        }

        return next(command, dynamicContext);
    }

    private LocalDateTime parseAlipayTime(String alipayTime) {
        if (alipayTime == null || alipayTime.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(alipayTime, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            log.warn("支付宝支付时间解析失败 alipayTime:{}", alipayTime, e);
            return null;
        }
    }

}
