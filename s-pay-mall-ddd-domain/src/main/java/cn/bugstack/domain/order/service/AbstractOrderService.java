package cn.bugstack.domain.order.service;

import cn.bugstack.domain.order.adapter.port.IProductPort;
import cn.bugstack.domain.order.adapter.repository.IOrderLockRepository;
import cn.bugstack.domain.order.adapter.repository.IOrderRepository;
import cn.bugstack.domain.order.model.aggregate.CreateOrderAggregate;
import cn.bugstack.domain.order.model.entity.*;
import cn.bugstack.domain.order.model.valobj.OrderStatusVO;
import cn.bugstack.types.enums.OrderTypeEnum;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import com.alipay.api.AlipayApiException;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
public abstract class AbstractOrderService implements IOrderService {

    protected final IOrderRepository orderRepository;
    protected final IOrderLockRepository orderLockRepository;
    protected final IProductPort productPort;

    public AbstractOrderService(IOrderRepository orderRepository, IOrderLockRepository orderLockRepository, IProductPort productPort) {
        this.orderRepository = orderRepository;
        this.orderLockRepository = orderLockRepository;
        this.productPort = productPort;
    }

    @Override
    public PayOrderEntity createOrder(String lockId) throws Exception {
        // 1. 根据 lockId 查询锁单记录
        OrderLockEntity orderLockEntity = orderLockRepository.queryLockByLockId(lockId);
        if (orderLockEntity == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "锁单的订单不存在");
        }
        if (!orderLockEntity.getLockStatus().equals("LOCKED")) {
            throw new AppException(ResponseCode.UN_ERROR, "锁单状态异常，当前状态是：{}" + orderLockEntity.getLockStatus());
        }
        if (orderLockEntity.isExpired()) {
            orderLockRepository.updateLockStatus(lockId, "EXPIRED");
            throw new AppException(ResponseCode.UN_ERROR, "锁单已过期，请重新锁单");
        }
        // 2. 基于锁单快照构建订单
        OrderEntity orderEntity = CreateOrderAggregate.buildOrderEntity(
                orderLockEntity.getProductId(), orderLockEntity.getProductName()
        );
        CreateOrderAggregate orderAggregate = CreateOrderAggregate.builder()
                .userId(orderLockEntity.getUserId())
                .productEntity(ProductEntity.builder()
                        .productId(orderLockEntity.getProductId())
                        .productName(orderLockEntity.getProductName())
                        .price(orderLockEntity.getTotalAmount())
                        .build())
                .orderEntity(orderEntity)
                .build();
        // 3. 保存订单
        orderRepository.doSaveOrder(orderAggregate);
        // 4. 创建支付单
        PayOrderEntity payOrderEntity = this.doPrepayOrder(
                orderLockEntity.getUserId(),
                orderLockEntity.getProductId(),
                orderLockEntity.getProductName(),
                orderEntity.getOutTradeNo(),
                orderLockEntity.getTotalAmount()
        );
        // 5. 确认锁单
        orderLockRepository.updateLockStatus(lockId, "CONFIRMED");
        return payOrderEntity;
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    protected abstract void doSaveOrder(CreateOrderAggregate orderAggregate);

    protected abstract PayOrderEntity doPrepayOrder(String userId, String productId, String productName, String outTradeNo, BigDecimal totalAmount) throws AlipayApiException;

}
