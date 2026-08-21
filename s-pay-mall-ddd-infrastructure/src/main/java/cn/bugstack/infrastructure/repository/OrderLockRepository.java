package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.order.adapter.repository.IOrderLockRepository;
import cn.bugstack.domain.order.model.entity.OrderLockEntity;
import cn.bugstack.infrastructure.dao.IOrderLockDao;
import cn.bugstack.infrastructure.dao.po.OrderLock;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Repository
public class OrderLockRepository implements IOrderLockRepository {

    @Resource
    private IOrderLockDao orderLockDao;

    @Override
    public void saveLock(OrderLockEntity lockEntity) {
        OrderLock orderLock = OrderLock.builder()
                .lockId(lockEntity.getLockId())
                .userId(lockEntity.getUserId())
                .productId(lockEntity.getProductId())
                .productName(lockEntity.getProductName())
                .totalAmount(lockEntity.getTotalAmount())
                .lockStatus(lockEntity.getLockStatus())
                .lockTime(toDate(lockEntity.getLockTime()))
                .expireTime(toDate(lockEntity.getExpireTime()))
                .build();
        orderLockDao.insert(orderLock);
    }

    @Override
    public OrderLockEntity queryLockByLockId(String lockId) {
        OrderLock orderLock = orderLockDao.queryByLockId(lockId);
        return toEntity(orderLock);
    }

    @Override
    public void updateLockStatus(String lockId, String status) {
        orderLockDao.updateLockStatus(lockId, status);
    }

    @Override
    public OrderLockEntity queryLockedByUserProduct(String userId, String productId) {
        OrderLock orderLock = orderLockDao.queryLockedByUserProduct(userId, productId);
        return toEntity(orderLock);
    }

    // ---- PO ↔ Entity 转换 ----

    private OrderLockEntity toEntity(OrderLock orderLock) {
        if (orderLock == null) {
            return null;
        }
        return OrderLockEntity.builder()
                .lockId(orderLock.getLockId())
                .userId(orderLock.getUserId())
                .productId(orderLock.getProductId())
                .productName(orderLock.getProductName())
                .totalAmount(orderLock.getTotalAmount())
                .lockStatus(orderLock.getLockStatus())
                .lockTime(toLocalDateTime(orderLock.getLockTime()))
                .expireTime(toLocalDateTime(orderLock.getExpireTime()))
                .build();
    }

    private Date toDate(LocalDateTime ldt) {
        if (ldt == null) return null;
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

}
