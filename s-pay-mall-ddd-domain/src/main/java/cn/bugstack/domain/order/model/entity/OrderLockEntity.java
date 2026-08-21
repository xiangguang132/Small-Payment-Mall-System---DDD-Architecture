package cn.bugstack.domain.order.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 锁单实体 —— 映射 order_lock 表
 * 仅记录锁单号、商品ID、关联订单ID、状态、时间
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderLockEntity {

    private Long id;
    private String lockId;
    private String userId;
    private String productId;
    private String orderId;
    private String lockStatus;  // LOCKED / CONFIRMED / EXPIRED
    private LocalDateTime lockTime;

    /**
     * 判断是否过期（默认15分钟）
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.lockTime.plusMinutes(15));
    }
}
