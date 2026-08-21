package cn.bugstack.api.request.trade;

import lombok.Data;

@Data
public class ConfirmOrderRequest {

    // 锁单号
    private String lockId;

    // 产品编号
    private String productId;

}
