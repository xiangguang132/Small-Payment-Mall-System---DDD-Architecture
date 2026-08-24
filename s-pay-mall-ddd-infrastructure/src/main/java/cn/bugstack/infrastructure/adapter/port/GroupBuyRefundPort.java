package cn.bugstack.infrastructure.adapter.port;

import cn.bugstack.domain.groupbuy.adapter.IGroupBuyRefundPort;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyNotifyTaskEntity;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyRefundOrderBehaviorEntity;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyNotifyTaskRepository;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyOrderRepository;
import cn.bugstack.domain.groupbuy.service.task.IGroupBuyNotifyTaskService;
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
    private IGroupBuyNotifyTaskRepository groupBuyNotifyTaskRepository;
    @Resource
    private IGroupBuyNotifyTaskService groupBuyNotifyTaskService;

    @Override
    public void groupBuyRefundNotify(GroupBuyRefundOrderBehaviorEntity behaviorEntity) throws Exception {
        String outTradeNo = behaviorEntity.getOutTradeNo();
        String teamId = behaviorEntity.getTeamId();
        boolean success = behaviorEntity.isSuccess();

        log.info("拼团退单回调 userId:{} teamId:{} outTradeNo:{} success:{} message:{}",
                behaviorEntity.getUserId(), teamId, outTradeNo, success, behaviorEntity.getMessage());

        if (!success) {
            log.warn("拼团退单失败，跳过后续处理 outTradeNo:{} message:{}", outTradeNo, behaviorEntity.getMessage());
            return;
        }

        // 1. 已支付订单：调用支付宝退款
        if (behaviorEntity.getPayAmount() != null) {
            boolean alipayRefundSuccess = alipayRefundPort.refund(outTradeNo, null, behaviorEntity.getPayAmount());
            if (!alipayRefundSuccess) {
                log.error("拼团退单支付宝退款失败 outTradeNo:{}", outTradeNo);
                return;   // 退款失败，不落消息表，等待重试
            }
            log.info("拼团退单支付宝退款成功 outTradeNo:{}", outTradeNo);
        }

        // 2. 更新拼团订单状态为已退款（status=2）
        int orderUpdated = groupBuyOrderRepository.updateOrderStatus2Refund(outTradeNo);
        if (orderUpdated != 1) {
            log.warn("拼团退单更新订单状态失败（可能已处理） outTradeNo:{}", outTradeNo);
        }

        // 3. 落库本地消息表（notifyStatus=0，等待发 MQ；兜底由 GroupBuyNotifyJob 扫）
        String parameterJson = JSON.toJSONString(new HashMap<String, Object>() {{
            put("refundType", behaviorEntity.getRefundType());
            put("userId", behaviorEntity.getUserId());
            put("teamId", teamId);
            put("orderId", behaviorEntity.getOrderId());
            put("outTradeNo", outTradeNo);
            put("activityId", behaviorEntity.getActivityId());
        }});
        GroupBuyNotifyTaskEntity task = GroupBuyNotifyTaskEntity.builder()
                .teamId(teamId)
                .activityId(behaviorEntity.getActivityId())
                .notifyType("MQ")
                .notifyMQ("topic.team_refund")          // 必须用 topic.team_refund，和消费者绑定一致
                .notifyStatus(0)
                .notifyCount(0)
                .parameterJson(parameterJson)
                .build();
        int inserted = groupBuyNotifyTaskRepository.insertNotifyTask(task);
        if (inserted != 1) {
            log.warn("拼团退单写本地消息表失败 teamId:{}", teamId);
            return;
        }

        // 4. 立即发 MQ（失败也由 GroupBuyNotifyJob 兜底重发 status=0）
        groupBuyNotifyTaskService.execNotifyJob(task);
    }

}
