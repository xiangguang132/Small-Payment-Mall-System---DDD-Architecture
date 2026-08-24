package cn.bugstack.domain.groupbuy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 退单-行为实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyRefundOrderBehaviorEntity {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 团队ID
     */
    private String teamId;

    /**
     * 拼团活动ID
     */
    private Long activityId;

    /**
     * 外部交易单号
     */
    private String outTradeNo;

    /**
     * 实付金额（已支付场景需要，用于调用支付宝退款）
     */
    private BigDecimal payAmount;

    /**
     * 退单是否成功
     */
    private boolean success;

    /**
     * 退单结果描述
     */
    private String message;

    /**
     * 退单策略名
     * 仅在需要继续路由策略时使用，避免和 message 语义混用
     */
    private String strategyName;
}
