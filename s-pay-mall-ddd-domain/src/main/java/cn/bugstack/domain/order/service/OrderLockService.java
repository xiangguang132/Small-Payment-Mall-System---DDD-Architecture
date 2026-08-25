package cn.bugstack.domain.order.service;

import cn.bugstack.domain.order.adapter.repository.IOrderLockRepository;
import cn.bugstack.domain.order.model.aggregate.LockOrderAggregate;
import cn.bugstack.domain.order.model.entity.OrderLockEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderLockService implements IOrderLockService {

    private final IOrderLockRepository orderLockRepository;

    public OrderLockService(IOrderLockRepository orderLockRepository) {
        this.orderLockRepository = orderLockRepository;
    }

    @Override
    public OrderLockEntity lockOrder(String productId) {
        // 构建锁单聚合体（锁单只关心商品与订单，不关心归属用户）
        LockOrderAggregate aggregate = LockOrderAggregate.build(productId);
        OrderLockEntity lockEntity = aggregate.getOrderLockEntity();

        // 持久化
        orderLockRepository.saveLock(lockEntity);

        log.info("锁单成功 lockId:{} productId:{}", lockEntity.getLockId(), productId);
        return lockEntity;
    }

    @Override
    public OrderLockEntity queryLocalStatus(String lockId) {
        OrderLockEntity lockEntity = orderLockRepository.queryLockByLockId(lockId);
        if (lockEntity != null && lockEntity.isExpired()) {
            orderLockRepository.updateLockStatus(lockId, "EXPIRED");
            lockEntity.setLockStatus("EXPIRED");
        }
        return lockEntity;
    }
}
