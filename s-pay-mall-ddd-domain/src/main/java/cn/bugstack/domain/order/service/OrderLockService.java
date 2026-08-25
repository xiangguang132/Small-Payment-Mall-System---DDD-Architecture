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
    public OrderLockEntity lockOrder() {
        // 构建锁单聚合体（锁单只生成锁单号与状态，不涉及商品与用户）
        LockOrderAggregate aggregate = LockOrderAggregate.build();
        OrderLockEntity lockEntity = aggregate.getOrderLockEntity();

        // 持久化
        orderLockRepository.saveLock(lockEntity);

        log.info("锁单成功 lockId:{}", lockEntity.getLockId());
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
