package cn.bugstack.api.request.trade;

import lombok.Data;

@Data
public class RefundOrderRequest {

    // 用户ID 【实际生产中会通过登录模块获取，不需要透传】
    private String userId;

    // 外部交易单号
    private String outTradeNo;

}
