package cn.bugstack.api.request.groupbuy;

import lombok.Data;

import java.util.List;

@Data
public class GroupBuyLockOrderRequest {

    // 用户ID 【实际生产中会通过登录模块获取，不需要透传】
    private String userId;

    // 拼团活动ID
    private Long activityId;

    // 商品ID
    private Long productId;

    // 团队ID；开团为空、参团传参
    private String teamId;

    // 商户订单号（幂等号）；同一笔锁单重试/重复点击时传同一个值以复用已创建订单，为空则服务端生成
    private String outTradeNo;

    // 选择的优惠券ID列表（锁单时传入，用于核销）
    private List<String> couponIds;

    // 来源
    private String source;

    // 渠道
    private String channel;

}