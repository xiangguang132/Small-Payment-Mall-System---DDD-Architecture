package cn.bugstack.api.response.groupbuy;

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
public class GroupBuyOrderDetailResponse {

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

    /** 0已锁定、1已支付、2已完成、3已取消/退单 */
    private Integer status;

    private String outTradeNo;

    /** 拼团有效期开始（来自 group_buy_team） */
    private LocalDateTime validStartTime;

    /** 拼团有效期结束（来自 group_buy_team），拼团中订单用于前端倒计时 */
    private LocalDateTime validEndTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
