package cn.bugstack.api.request.trade;

import lombok.Data;

@Data
public class ConfirmOrderRequest {

    // 锁单号
    private String lockId;

    // 产品编号
    private String productId;

    // 用户编号（登录态缺失时的兜底，可空）
    private String userId;

}
