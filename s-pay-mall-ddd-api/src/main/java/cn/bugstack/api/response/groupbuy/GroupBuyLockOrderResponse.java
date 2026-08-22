package cn.bugstack.api.response.groupbuy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyLockOrderResponse {

    // 团队成员ID
    private String teamId;

    // 商户订单号/支付宝 out_trade_no，与 group_buy_order 一致
    private String outTradeNo;

    // 支付宝预支付表单/payUrl
    private String payUrl;

}