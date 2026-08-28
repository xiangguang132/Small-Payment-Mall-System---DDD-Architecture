package cn.bugstack.api.request.coupon;

import cn.bugstack.api.request.page.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 领券中心-分页查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CouponPageRequest extends PageRequest {

    /** 优惠券状态筛选：null=全部启用，1=启用，0=停用 */
    private Integer status;

}
