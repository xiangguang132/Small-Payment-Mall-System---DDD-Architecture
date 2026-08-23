package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.order.adapter.repository.IOrderLockRepository;
import cn.bugstack.domain.order.model.entity.OrderLockEntity;
import cn.bugstack.infrastructure.dao.IOrderLockDao;
import cn.bugstack.infrastructure.dao.po.payment.OrderLock;
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
                .orderId(lockEntity.getOrderId())
                .lockStatus(lockEntity.getLockStatus())
                .lockTime(toDate(lockEntity.getLockTime()))
                .build();
        orderLockDao.insert(orderLock);
    }

    @Override
    public OrderLockEntity queryLockByLockId(String lockId) {
        return toEntity(orderLockDao.queryByLockId(lockId));
    }

    @Override
    public void updateOrderId(String lockId, String orderId) {
        orderLockDao.updateOrderId(lockId, orderId);
    }

    @Override
    public void updateLockStatus(String lockId, String status) {
        orderLockDao.updateLockStatus(lockId, status);
    }

    // ---- PO ↔ Entity 转换 ----

    private OrderLockEntity toEntity(OrderLock orderLock) {
        if (orderLock == null) return null;
        return OrderLockEntity.builder()
                .id(orderLock.getId())
                .lockId(orderLock.getLockId())
                .userId(orderLock.getUserId())
                .productId(orderLock.getProductId())
                .orderId(orderLock.getOrderId())
                .lockStatus(orderLock.getLockStatus())
                .lockTime(toLocalDateTime(orderLock.getLockTime()))
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
