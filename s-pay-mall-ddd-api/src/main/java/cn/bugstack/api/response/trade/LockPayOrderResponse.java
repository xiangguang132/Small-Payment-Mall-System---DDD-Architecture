package cn.bugstack.api.response.trade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockPayOrderResponse {

    // 商户订单号/支付宝 out_trade_no
    private String outTradeNo;

    // 支付宝预支付表单/payUrl
    private String payUrl;

}
