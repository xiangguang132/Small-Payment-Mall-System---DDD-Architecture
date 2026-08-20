package cn.bugstack.domain.payment.service.rule.filter;

import cn.bugstack.domain.payment.model.entity.AlipayNotifyProcessCommandEntity;
import cn.bugstack.domain.payment.model.entity.AlipayNotifyProcessFeedBackEntity;
import cn.bugstack.domain.payment.model.entity.AlipayNotifyTaskEntity;
import cn.bugstack.domain.payment.adapter.repository.IAlipayNotifyTaskRepository;
import cn.bugstack.domain.payment.service.rule.factory.AlipayNotifyProcessRuleFilterFactory;
import cn.bugstack.types.design.framework.link.multilink.handler.ILogicHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 过滤节点-任务状态校验
 * 已成功 → 直接返回 / 超过最大重试次数 → 标记失败并返回
 */
@Slf4j
@Service
public class TaskStatusFilter implements ILogicHandler<
        AlipayNotifyProcessCommandEntity,
        AlipayNotifyProcessRuleFilterFactory.DynamicContext,
        AlipayNotifyProcessFeedBackEntity> {

    @Value("${alipay.notify.task.max-retry-count:5}")
    private int maxRetryCount;

    @Resource
    private IAlipayNotifyTaskRepository alipayNotifyTaskRepository;

    @Override
    public AlipayNotifyProcessFeedBackEntity apply(
            AlipayNotifyProcessCommandEntity command,
            AlipayNotifyProcessRuleFilterFactory.DynamicContext dynamicContext) throws Exception {

        AlipayNotifyTaskEntity task = dynamicContext.getTask();

        // 任务已处理完成，直接返回
        if (Integer.valueOf(1).equals(task.getTaskStatus())) {
            log.info("支付宝通知任务已处理完成 outTradeNo:{}", command.getOutTradeNo());
            return AlipayNotifyProcessFeedBackEntity.builder()
                    .outTradeNo(command.getOutTradeNo())
                    .handled(true)
                    .build();
        }

        // 超过最大重试次数，标记失败
        if (task.getRetryCount() != null && task.getRetryCount() >= maxRetryCount) {
            alipayNotifyTaskRepository.updatedAlipayNotifyTaskFailed(command.getOutTradeNo());
            log.warn("支付宝通知任务重试次数已达上限 outTradeNo:{} retryCount:{}", command.getOutTradeNo(), task.getRetryCount());
            return AlipayNotifyProcessFeedBackEntity.builder()
                    .outTradeNo(command.getOutTradeNo())
                    .handled(true)
                    .build();
        }

        return next(command, dynamicContext);
    }

}
