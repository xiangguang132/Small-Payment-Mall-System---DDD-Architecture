package cn.bugstack.api.request.trade;

import lombok.Data;

import java.util.List;

/**
 * 直购试算请求：选券后预览折后价
 */
@Data
public class DirectOrderTrialRequest {

    /** 商品ID */
    private Long productId;

    /** 选择的优惠券ID列表（可空，空=不使用券） */
    private List<String> couponIds;

}
