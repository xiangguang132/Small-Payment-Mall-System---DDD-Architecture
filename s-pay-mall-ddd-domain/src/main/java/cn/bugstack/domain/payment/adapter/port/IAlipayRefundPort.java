package cn.bugstack.domain.payment.adapter.port;

import java.math.BigDecimal;

/**
 * 支付宝退款接口
 */
public interface IAlipayRefundPort {

    // 调用支付宝统一退款；返回支付宝退款成功与否
    boolean refund(String outTradeNo, String outRequestNo, BigDecimal refundAmount);

}
