package cn.bugstack.api.request.trade;

import lombok.Data;

import java.util.List;

@Data
public class LockOrderRequest {

    // 用户ID 【实际生产中会通过登录模块获取，不需要透传】
    private String userId;

    // 产品编号
    private String productId;

    // 优惠券ID列表
    private List<String> couponIds;

}
