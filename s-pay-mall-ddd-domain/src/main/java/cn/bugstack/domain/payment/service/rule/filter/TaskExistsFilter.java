package cn.bugstack.domain.payment.service.rule.filter;

import cn.bugstack.domain.order.adapter.repository.IOrderRepository;
import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.payment.model.entity.AlipayNotifyProcessCommandEntity;
import cn.bugstack.domain.payment.model.entity.AlipayNotifyProcessFeedBackEntity;
import cn.bugstack.domain.payment.model.entity.AlipayNotifyTaskEntity;
import cn.bugstack.domain.payment.adapter.repository.IAlipayNotifyTaskRepository;
import cn.bugstack.domain.payment.service.rule.factory.AlipayNotifyProcessRuleFilterFactory;
import cn.bugstack.types.design.framework.link.multilink.handler.ILogicHandler;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 过滤节点-任务存在性校验
 * 查询任务、订单、解析回调参数，放入 DynamicContext 供后续节点使用
 */
@Slf4j
@Service
public class TaskExistsFilter implements ILogicHandler<
        AlipayNotifyProcessCommandEntity,
        AlipayNotifyProcessRuleFilterFactory.DynamicContext,
        AlipayNotifyProcessFeedBackEntity> {

    @Resource
    private IAlipayNotifyTaskRepository alipayNotifyTaskRepository;

    @Resource
    private IOrderRepository orderRepository;

    @Override
    public AlipayNotifyProcessFeedBackEntity apply(
            AlipayNotifyProcessCommandEntity command,
            AlipayNotifyProcessRuleFilterFactory.DynamicContext dynamicContext) throws Exception {

        // 查询任务
        AlipayNotifyTaskEntity task = alipayNotifyTaskRepository.queryByOutTradeNo(command.getOutTradeNo());
        if (task == null) {
            throw new AppException(ResponseCode.UN_ERROR, "支付宝通知任务不存在 outTradeNo:" + command.getOutTradeNo());
        }

        // 查询支付单
        PayOrderEntity payOrder = orderRepository.queryPayOrderByOutTradeNo(command.getOutTradeNo());
        if (payOrder == null) {
            throw new AppException(ResponseCode.UN_ERROR, "支付单不存在 outTradeNo:" + command.getOutTradeNo());
        }

        // 解析回调参数
        JSONObject params = JSON.parseObject(task.getParameterJson());

        // 写入上下文
        dynamicContext.setTask(task);
        dynamicContext.setPayOrder(payOrder);
        dynamicContext.setParams(params);

        return next(command, dynamicContext);
    }

}
