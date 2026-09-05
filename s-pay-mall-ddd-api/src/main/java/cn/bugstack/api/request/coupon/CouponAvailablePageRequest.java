package cn.bugstack.api.request.coupon;

import cn.bugstack.api.request.page.PageRequest;
import lombok.Data;

/**
 * 查询我的可用券-分页请求
 * （下单/结算选券弹窗按页加载，默认 5 张一页由前端传入）
 */
@Data
public class CouponAvailablePageRequest extends PageRequest {

    /** 优惠券类型筛选：FULL_REDUCTION / DISCOUNT / FIXED，null=全部 */
    private String couponType;
}