package cn.bugstack.domain.order.service;

import cn.bugstack.domain.order.adapter.port.IProductPort;
import cn.bugstack.domain.order.adapter.repository.IOrderLockRepository;
import cn.bugstack.domain.order.model.aggregate.LockOrderAggregate;
import cn.bugstack.domain.order.model.entity.OrderLockEntity;
import cn.bugstack.domain.order.model.entity.ProductEntity;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderLockService implements IOrderLockService {

    private final IProductPort productPort;
    private final IOrderLockRepository orderLockRepository;

    public OrderLockService(IProductPort productPort, IOrderLockRepository orderLockRepository) {
        this.productPort = productPort;
        this.orderLockRepository = orderLockRepository;
    }

    @Override
    public OrderLockEntity lockOrder(String userId, String productId) throws Exception {
        // 1. 幂等：复用已有未过期锁单
        OrderLockEntity orderLockEntity = orderLockRepository.queryLockedByUserProduct(userId, productId);
        if (orderLockEntity != null && !orderLockEntity.isExpired()) {
            log.info("锁单复用已有锁单 lockId:{} userId:{}", orderLockEntity.getLockId(), userId);
            return orderLockEntity;
        }
        // 2. 查询商品信息
        ProductEntity productEntity = productPort.queryProductByProductId(productId);
        if (productEntity == null ) {
            throw new AppException(ResponseCode.NOT_FOUND, "商品不存在");
        }
        // 3. TODO: 校验库存
        // 4. TODO: 锁定库存
        // 5. 构建锁单聚合体
        LockOrderAggregate lockOrderAggregate = LockOrderAggregate.build(userId, productEntity);

        // 6. 持久化锁单记录
        OrderLockEntity orderLock = lockOrderAggregate.getOrderLockEntity();
        orderLockRepository.saveLock(orderLock);
        log.info("锁单成功 lockId:{} userId:{} productId:{}", orderLock.getLockId(), userId, productId);
        return orderLock;
    }

    @Override
    public OrderLockEntity queryLocalStatus(String lockId) {
        OrderLockEntity orderLockEntity = orderLockRepository.queryLockByLockId(lockId);

        // 如果 不为并且状态已经 过期了 -> 更新状态为 已过期
        if (orderLockEntity != null && orderLockEntity.isExpired()) {
            orderLockRepository.updateLockStatus(lockId, "EXPIRED");
            orderLockEntity.setLockStatus("EXPIRED");
        }

        // 返回锁单实体
        return orderLockEntity;
    }
}
