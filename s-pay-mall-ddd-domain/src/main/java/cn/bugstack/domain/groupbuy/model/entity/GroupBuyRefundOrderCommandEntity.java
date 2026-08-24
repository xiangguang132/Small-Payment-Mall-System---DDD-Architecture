package cn.bugstack.domain.groupbuy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 退单-命令实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyRefundOrderCommandEntity {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 拼团活动ID
     */
    private Long activityId;

    /**
     * 拼团团队ID
     */
    private String teamId;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 外部交易单号
     */
    private String outTradeNo;

    /**
     * 退单类型或原因编码，后续可用于路由策略
     */
    private String refundType;
}
