package cn.bugstack.api.request.groupbuy;

import lombok.Data;

@Data
public class GroupBuyTrialRequest {

    // 用户ID 【实际生产中会通过登录模块获取，不需要透传】
    private String userId;

    // 拼团活动ID
    private Long activityId;

    // 商品ID
    private Long productId;

}