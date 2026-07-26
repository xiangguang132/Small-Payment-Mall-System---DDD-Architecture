package cn.bugstack.infrastructure.adapter.repository;

import cn.bugstack.domain.order.adapter.repository.IOrderRepository;
import cn.bugstack.domain.order.model.aggregate.CreateOrderAggregate;
import cn.bugstack.domain.order.model.entity.OrderEntity;
import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.order.model.entity.ProductEntity;
import cn.bugstack.domain.order.model.entity.ShopCartEntity;
import cn.bugstack.domain.order.model.valobj.OrderStatusVO;
import cn.bugstack.infrastructure.dao.IOrderDao;
import cn.bugstack.infrastructure.dao.po.PayOrder;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class OrderRepository implements IOrderRepository {

    @Resource
    private IOrderDao orderDao;

    @Override
    public void doSaveOrder(CreateOrderAggregate orderAggregate) {
        String userId = orderAggregate.getUserId();
        ProductEntity  productEntity = orderAggregate.getProductEntity();
        OrderEntity orderEntity = orderAggregate.getOrderEntity();

        PayOrder order = new PayOrder();
        order.setUserId(userId);
        order.setProductId(productEntity.getProductId());
        order.setProductName(productEntity.getProductName());
        order.setOrderId(orderEntity.getOrderId());
        order.setOrderTime(orderEntity.getOrderTime());
        order.setTotalAmount(productEntity.getPrice());
        order.setStatus(orderEntity.getOrderStatus().getCode());

        orderDao.insert(order);

        // 存入缓存；缓存key聚合到对象中提供
//        redisService.setValue(PayOrder.cacheKey(userId, orderEntity.getOrderId()), order);

    }

    @Override
    public OrderEntity queryUnPayOrder(ShopCartEntity shopCartEntity) {
        // 1. 封装参数
        PayOrder orderReq = new  PayOrder();
        orderReq.setUserId(shopCartEntity.getUserId());
        orderReq.setProductId(shopCartEntity.getProductId());

        // 2.查询到订单
        PayOrder order = orderDao.queryUnPayOrder(orderReq);
        if (order == null) {
            return null;
        }

        // 3. 返回结果
        return OrderEntity.builder()
                .productId(order.getProductId())
                .productName(order.getProductName())
                .orderId(order.getOrderId())
                .orderStatus(OrderStatusVO.valueOf(order.getStatus()))
                .orderTime(order.getOrderTime())
                .totalAmount(order.getTotalAmount())
                .payUrl(order.getPayUrl())
                .build();

    }

    @Override
    public void updateOrderPayInfo(PayOrderEntity payOrderEntity) {
        PayOrder order = new PayOrder();
        order.setUserId(payOrderEntity.getUserId());
        order.setOrderId(payOrderEntity.getOrderId());
        order.setPayUrl(payOrderEntity.getPayUrl());
        order.setStatus(payOrderEntity.getOrderStatus().getCode());
        orderDao.updateOrderPayInfo(order);
    }

}
