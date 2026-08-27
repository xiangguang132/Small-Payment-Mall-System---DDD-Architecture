package cn.bugstack.api.request.groupbuy;

import lombok.Data;

@Data
public class GroupBuyRepayRequest {

    // 外部交易单号；定位本人待付款的拼团订单并重新拉起支付
    private String outTradeNo;

}
