package cn.bugstack.api.response.trade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 直购试算响应：展示券后价
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectOrderTrialResponse {

    /** 商品ID */
    private Long productId;

    /** 商品原价 */
    private BigDecimal originalPrice;

    /** 优惠减免金额（例如原价100、实付88时这里是12） */
    private BigDecimal deductionPrice;

    /** 最终实付金额（例如原价100、优惠12时这里是88） */
    private BigDecimal payPrice;

}
