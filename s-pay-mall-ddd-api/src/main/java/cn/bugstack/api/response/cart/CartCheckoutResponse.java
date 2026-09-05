package cn.bugstack.api.response.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartCheckoutResponse {

    /** 商户订单号 */
    private String outTradeNo;

    /** 支付宝支付表单（跳转支付用） */
    private String payUrl;

    /** 实付金额（券后） */
    private BigDecimal payAmount;

    /** 聚合原价（券抵扣前） */
    private BigDecimal originalAmount;

}