package cn.bugstack.infrastructure.adapter.port;

import cn.bugstack.domain.groupbuy.adapter.IGroupBuyRefundPort;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderBehaviorEntity;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyOrderRepository;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyTeamRepository;
import cn.bugstack.domain.payment.adapter.port.IAlipayRefundPort;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * 拼团退单回调端口
 * 负责退款后的全部收尾工作：Alipay 退款、状态更新、库存恢复、MQ 通知
 */
@Slf4j
@Component
public class GroupBuyRefundPort implements IGroupBuyRefundPort {

    @Resource
    private IAlipayRefundPort alipayRefundPort;

    @Resource
    private IGroupBuyOrderRepository groupBuyOrderRepository;

    @Resource
    private IGroupBuyTeamRepository groupBuyTeamRepository;

    @Override
    public void groupBuyRefundNotify(GroupBuyRefundOrderBehaviorEntity behaviorEntity) throws Exception {
        String outTradeNo = behaviorEntity.getOutTradeNo();
        String teamId = behaviorEntity.getTeamId();
        boolean success = behaviorEntity.isSuccess();

        log.info("拼团退单回调 userId:{} teamId:{} outTradeNo:{} success:{} message:{}",
                behaviorEntity.getUserId(),
                teamId,
                outTradeNo,
                success,
                behaviorEntity.getMessage());

        if (!success) {
            log.warn("拼团退单失败，跳过后续处理 outTradeNo:{} message:{}", outTradeNo, behaviorEntity.getMessage());
            return;
        }

        // 1. 已支付订单：调用支付宝退款
        if (behaviorEntity.getPayAmount() != null) {
            boolean alipayRefundSuccess = alipayRefundPort.refund(outTradeNo, null, behaviorEntity.getPayAmount());
            if (!alipayRefundSuccess) {
                log.error("拼团退单支付宝退款失败 outTradeNo:{}", outTradeNo);
                // 支付宝退款失败，不继续更新状态，等待重试
                return;
            }
            log.info("拼团退单支付宝退款成功 outTradeNo:{}", outTradeNo);
        }

        // 2. 更新拼团订单状态为已退款（status=2）
        int orderUpdated = groupBuyOrderRepository.updateOrderStatus2Refund(outTradeNo);
        if (orderUpdated != 1) {
            log.warn("拼团退单更新订单状态失败（可能已处理） outTradeNo:{}", outTradeNo);
        }

        // 3. 恢复团队库存：锁定人数 -1
        int teamUpdated = groupBuyTeamRepository.updateSubtractLockCount(teamId);
        if (teamUpdated != 1) {
            log.warn("拼团退单恢复团队库存失败 teamId:{}", teamId);
        }

        // 4. 写入 notify_task，等待 MQ Job 发送退款成功通知
        String parameterJson = JSON.toJSONString(new HashMap<String, Object>() {{
            put("teamId", teamId);
            put("outTradeNo", outTradeNo);
            put("activityId", behaviorEntity.getActivityId());
        }});
        log.info("拼团退单写入MQ通知 outTradeNo:{} parameterJson:{}", outTradeNo, parameterJson);
        // TODO: 当 group_buy_notify_task 表和 GroupBuyNotifyJob 补全后，取消注释写入逻辑
        // groupBuyNotifyTaskDao.insert(GroupBuyNotifyTask.builder()
        //         .teamId(teamId)
        //         .activityId(behaviorEntity.getActivityId())
        //         .notifyMq("topic.order_refund_success")
        //         .notifyStatus(0)
        //         .notifyCount(0)
        //         .parameterJson(parameterJson)
        //         .build());
    }
}
