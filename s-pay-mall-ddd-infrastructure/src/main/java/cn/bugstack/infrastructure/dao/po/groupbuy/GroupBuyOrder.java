package cn.bugstack.infrastructure.dao.po.groupbuy;

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
public class GroupBuyOrder {

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

    /** 拼团有效期开始（来自 group_buy_team 联表） */
    private LocalDateTime validStartTime;

    /** 拼团有效期结束（来自 group_buy_team 联表） */
    private LocalDateTime validEndTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
