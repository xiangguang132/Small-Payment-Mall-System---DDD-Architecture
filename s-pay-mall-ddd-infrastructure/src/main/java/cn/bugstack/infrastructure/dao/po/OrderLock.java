package cn.bugstack.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    private String userId;
    private String productId;
    private String productName;
    private BigDecimal totalAmount;
    private String lockStatus;
    private Date lockTime;
    private Date expireTime;
    private Date createTime;
    private Date updateTime;

}
