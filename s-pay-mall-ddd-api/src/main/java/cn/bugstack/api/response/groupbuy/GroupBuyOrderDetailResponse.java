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

    /** 订单状态：0已锁定(待付款)、1已支付、2已退款 */
    private Integer status;

    /** 团队状态（来自 group_buy_team）：0拼团中、1已成团；可能为 null */
    private Integer teamStatus;

    /** 展示态（组合 status 与 teamStatus）：10待付款、20拼团中、30已成团、40已退款 */
    private Integer displayStatus;

    private String outTradeNo;

    /** 拼团有效期开始（来自 group_buy_team） */
    private LocalDateTime validStartTime;

    /** 拼团有效期结束（来自 group_buy_team），拼团中订单用于前端倒计时 */
    private LocalDateTime validEndTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
