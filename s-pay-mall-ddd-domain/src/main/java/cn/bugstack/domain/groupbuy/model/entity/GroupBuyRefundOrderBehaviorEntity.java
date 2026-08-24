package cn.bugstack.domain.groupbuy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
     * 退单是否成功
     */
    private boolean success;

    /**
     * 退单结果描述
     */
    private String message;
}
