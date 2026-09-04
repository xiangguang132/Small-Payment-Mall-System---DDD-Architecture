package cn.bugstack.domain.groupbuy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyOrderEntity {

    private Long id;

    private String orderId;

    private String userId;

    private String teamId;

    private Long activityId;

    private Long productId;

    private String productName;

    private Integer quantity;

    private String source;

    private String channel;

    private BigDecimal originalAmount;

    private BigDecimal deductionAmount;

    private BigDecimal payAmount;

    private Integer status;

    private String outTradeNo;

    /** 使用的优惠券ID列表(JSON数组) */
    private String couponIds;

    /** 拼团有效期开始（联表 group_buy_team） */
    private LocalDateTime validStartTime;

    /** 拼团有效期结束（联表 group_buy_team） */
    private LocalDateTime validEndTime;

    /** 团队状态（联表 group_buy_team）：0拼团中 1已成团 */
    private Integer teamStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
