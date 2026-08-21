package cn.bugstack.domain.order.service;

import cn.bugstack.domain.order.adapter.port.IProductPort;
import cn.bugstack.domain.order.adapter.repository.IOrderLockRepository;
import cn.bugstack.domain.order.model.entity.OrderLockEntity;
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
        return null;
    }

    @Override
    public OrderLockEntity queryLocalStatus(String lockId) {
        return null;
    }
}
