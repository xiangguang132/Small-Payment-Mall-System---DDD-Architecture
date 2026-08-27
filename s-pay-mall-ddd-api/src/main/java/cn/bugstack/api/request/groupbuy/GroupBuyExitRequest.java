package cn.bugstack.api.request.groupbuy;

import lombok.Data;

@Data
public class GroupBuyExitRequest {

    // 外部交易单号；定位要退出的拼团订单
    private String outTradeNo;

}
