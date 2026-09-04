package cn.bugstack.infrastructure.dao.po.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 锁单持久化对象 —— 映射 order_lock 表
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderLock {

    private Long id;
    private String lockId;
    private String orderId;
    private String lockStatus;
    private Date lockTime;
    /** 使用的优惠券ID列表(JSON数组) */
    private String couponIds;
    private Date createTime;
    private Date updateTime;

}
