package cn.bugstack.domain.order.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 锁单实体 —— 映射 order_lock 表
 * 仅记录锁单号、关联订单ID、状态、时间；商品与用户信息由确认下单请求提供
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderLockEntity {

    private Long id;
    private String lockId;
    private String orderId;
    private String lockStatus;  // LOCKED / CONFIRMED / EXPIRED
    private LocalDateTime lockTime;
    /** 使用的优惠券ID列表(JSON数组) */
    private String couponIds;

    /**
     * 判断是否过期（默认15分钟）
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.lockTime.plusMinutes(15));
    }
}
