package cn.bugstack.infrastructure.dao.po.promotion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户优惠券 PO - 对齐 group_buy_coupon_user 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCoupon {

    private Long id;
    private String couponUserId;
    private String userId;
    private String couponId;
    private String sourceOrderNo;
    /** 状态：0未使用 1已使用 2已过期 3已失效 */
    private Integer status;
    private LocalDateTime expireTime;
    private LocalDateTime usedTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
