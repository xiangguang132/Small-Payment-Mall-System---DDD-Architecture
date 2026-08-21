package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.order.adapter.repository.IOrderLockRepository;
import cn.bugstack.domain.order.model.entity.OrderLockEntity;
import org.springframework.stereotype.Repository;

@Repository
public class OrderLockRepository implements IOrderLockRepository {

    @Override
    public void saveLock(OrderLockEntity lockEntity) {

    }

    @Override
    public OrderLockEntity queryLockByLockId(String lockId) {
        return null;
    }

    @Override
    public void updateLockStatus(String lockId, String status) {

    }

    @Override
    public OrderLockEntity queryLockedByUserProduct(String userId, String productId) {
        return null;
    }
}
