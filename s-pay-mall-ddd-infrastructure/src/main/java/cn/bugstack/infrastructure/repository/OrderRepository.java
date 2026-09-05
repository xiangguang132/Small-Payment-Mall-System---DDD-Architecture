package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.order.adapter.repository.IOrderRepository;
import cn.bugstack.domain.order.event.PaySuccessMessageEvent;
import cn.bugstack.domain.order.model.aggregate.CreateCartOrderAggregate;
import cn.bugstack.domain.order.model.aggregate.CreateOrderAggregate;
import cn.bugstack.domain.order.model.entity.OrderEntity;
import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.order.model.entity.PayOrderItemEntity;
import cn.bugstack.domain.order.model.entity.ProductEntity;
import cn.bugstack.domain.order.model.entity.ShopCartEntity;
import cn.bugstack.domain.order.model.valobj.OrderStatusVO;
import cn.bugstack.infrastructure.dao.IOrderDao;
import cn.bugstack.infrastructure.dao.IPayOrderItemDao;
import cn.bugstack.infrastructure.dao.po.payment.PayOrder;
import cn.bugstack.infrastructure.dao.po.payment.PayOrderItem;
import cn.bugstack.infrastructure.event.EventPublisher;
import cn.bugstack.types.enums.OrderTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 该部分着重于实现业务，但是不调用数据库
 * 将修改数据的操作进行下沉
 * 下沉到数据库 dao层
 */

@Slf4j
@Repository
public class OrderRepository implements IOrderRepository {

    @Resource
    private IOrderDao orderDao;
    @Resource
    private IPayOrderItemDao payOrderItemDao;
    @Resource
    private EventPublisher eventPublisher;
    @Value("${spring.rabbitmq.config.producer.topic_order_pay_success.routing_key}")
    private String routingKey;

    @Override
    public void doSaveOrder(CreateOrderAggregate orderAggregate) {
        String userId = orderAggregate.getUserId();
        ProductEntity  productEntity = orderAggregate.getProductEntity();
        OrderEntity orderEntity = orderAggregate.getOrderEntity();

        PayOrder order = new PayOrder();
        order.setUserId(userId);
        order.setProductId(productEntity.getProductId());
        order.setProductName(productEntity.getProductName());
        order.setOutTradeNo(orderEntity.getOutTradeNo());
        order.setOrderTime(orderEntity.getOrderTime());
        // 优先使用券后价，无券时取商品原价
        order.setTotalAmount(orderAggregate.getPayAmount() != null
                ? orderAggregate.getPayAmount() : productEntity.getPrice());
        order.setOriginalAmount(orderAggregate.getOriginalAmount());
        order.setOrderType(OrderTypeEnum.DIRECT.getCode());
        order.setCouponIds(orderAggregate.getCouponIds());
        order.setStatus(orderEntity.getOrderStatus().getCode());

        orderDao.insert(order);
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
                .outTradeNo(order.getOutTradeNo())
                .orderStatus(OrderStatusVO.valueOf(order.getStatus()))
                .orderTime(order.getOrderTime())
                .totalAmount(order.getTotalAmount())
                .payUrl(order.getPayUrl())
                .build();

    }

    @Override
    public PayOrderEntity queryPayOrderByOutTradeNo(String outTradeNo) {
        PayOrder order = orderDao.queryPayOrderByOutTradeNo(outTradeNo);
        return toPayOrderEntity(order);
    }

    /**
     * PO → Domain Entity 统一转换
     */
    private PayOrderEntity toPayOrderEntity(PayOrder order) {
        if (order == null) {
            return null;
        }
        return PayOrderEntity.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .productId(order.getProductId())
                .productName(order.getProductName())
                .outTradeNo(order.getOutTradeNo())
                .orderTime(toLocalDateTime(order.getOrderTime()))
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getStatus() == null ? null : OrderStatusVO.valueOf(order.getStatus()))
                .orderType(order.getOrderType() == null ? null : OrderTypeEnum.valueOf(order.getOrderType()))
                .payUrl(order.getPayUrl())
                .payTime(toLocalDateTime(order.getPayTime()))
                .outTradeTime(toLocalDateTime(order.getOutTradeTime()))
                .createTime(toLocalDateTime(order.getCreateTime()))
                .updateTime(toLocalDateTime(order.getUpdateTime()))
                .build();
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private Date toDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    @Override
    @Transactional
    public void saveGroupBuyPayOrder(PayOrderEntity payOrderEntity) {
        PayOrder order = new PayOrder();
        order.setUserId(payOrderEntity.getUserId());
        order.setProductId(payOrderEntity.getProductId());
        order.setProductName(payOrderEntity.getProductName());
        order.setOutTradeNo(payOrderEntity.getOutTradeNo());
        order.setOrderTime(toDate(payOrderEntity.getOrderTime()));
        order.setTotalAmount(payOrderEntity.getTotalAmount());
        order.setOrderType(OrderTypeEnum.GROUP_BUY.getCode());
        order.setStatus(OrderStatusVO.CREATE.getCode());

        orderDao.insert(order);
    }

    @Override
    public void updateOrderPayInfo(PayOrderEntity payOrderEntity) {
        PayOrder order = new PayOrder();
        order.setUserId(payOrderEntity.getUserId());
        order.setOutTradeNo(payOrderEntity.getOutTradeNo());
        order.setPayUrl(payOrderEntity.getPayUrl());
        order.setStatus(payOrderEntity.getOrderStatus().getCode());
        orderDao.updateOrderPayInfo(order);
    }

    @Override
    public void changeOrderPaySuccess(String outTradeNo, Date outTradeTime) {
        PayOrder order = new PayOrder();
        order.setOutTradeNo(outTradeNo);
        order.setStatus(OrderStatusVO.PAY_SUCCESS.getCode());
        order.setOutTradeTime(outTradeTime);
        orderDao.changeOrderPaySuccess(order);

        // 发送 MQ 消息，通知支付成功
        eventPublisher.publish(routingKey, outTradeNo);
        log.info("支付成功消息已发送 outTradeNo:{}", outTradeNo);
    }

    @Override
    public List<String> queryNoPayNotifyOrderList() {
        return orderDao.queryNoPayNotifyOrder();
    }

    @Override
    public List<String> queryTimeOutCloseOrderList() {
        return orderDao.queryTimeoutCloseOrderList();
    }

    @Override
    public boolean changeOrderPayClose(String outTradeNo) {
        return orderDao.changeOrderClose(outTradeNo);
    }

    @Override
    public boolean changeOrderRefunding(String outTradeNo) {
        return orderDao.changeOrderRefunding(outTradeNo);
    }

    @Override
    public boolean changeOrderRefundResult(String outTradeNo, String fromStatus, String toStatus) {
        return orderDao.changeOrderRefundResult(outTradeNo, fromStatus, toStatus);
    }

    @Override
    public List<PayOrderEntity> queryPageByStatusAndUserId(String status, String userId, Integer offset, Integer limit) {
        List<PayOrder> orderList = orderDao.queryPageByStatusAndUserId(status, userId, offset, limit);
        if (orderList == null || orderList.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        List<PayOrderEntity> result = new java.util.ArrayList<>(orderList.size());
        for (PayOrder order : orderList) {
            result.add(toPayOrderEntity(order));
        }
        // 批量装载订单明细：购物车结算订单（CART）头表无商品字段，明细从 pay_order_item 聚合返回
        fillOrderItems(result);
        return result;
    }

    /**
     * 按 out_trade_no 批量查询明细并分组填充到支付单实体
     * 单商品订单（DIRECT/GROUP_BUY）不下明细，items 保持为空，前端可回退展示头表 productName
     */
    private void fillOrderItems(List<PayOrderEntity> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        List<String> orderIds = orders.stream()
                .map(PayOrderEntity::getOutTradeNo)
                .collect(Collectors.toList());
        List<PayOrderItem> items = payOrderItemDao.queryByOrderIds(orderIds);
        if (items == null || items.isEmpty()) {
            return;
        }
        Map<String, List<PayOrderItemEntity>> groupByOrderId = items.stream()
                .map(item -> PayOrderItemEntity.builder()
                        .orderId(item.getOrderId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .totalAmount(item.getTotalAmount())
                        .build())
                .collect(Collectors.groupingBy(PayOrderItemEntity::getOrderId));
        for (PayOrderEntity order : orders) {
            order.setItems(groupByOrderId.get(order.getOutTradeNo()));
        }
    }

    @Override
    public long countByStatusAndUserId(String status, String userId) {
        return orderDao.countByStatusAndUserId(status, userId);
    }

    @Override
    @Transactional
    public void saveCartOrder(CreateCartOrderAggregate aggregate) {
        OrderEntity orderEntity = aggregate.getOrderEntity();

        // 1. 插入 pay_order 头表（productId/productName 为空，orderType=CART）
        PayOrder order = new PayOrder();
        order.setUserId(aggregate.getUserId());
        order.setProductId(null);
        order.setProductName(null);
        order.setOutTradeNo(orderEntity.getOutTradeNo());
        order.setOrderTime(orderEntity.getOrderTime());
        order.setTotalAmount(aggregate.getPayAmount());
        order.setOriginalAmount(aggregate.getOriginalAmount());
        order.setOrderType(OrderTypeEnum.CART.getCode());
        order.setCouponIds(aggregate.getCouponIds());
        order.setStatus(orderEntity.getOrderStatus().getCode());
        orderDao.insert(order);

        // 2. 批量插入明细
        List<PayOrderItem> items = aggregate.getItems().stream()
                .map(item -> PayOrderItem.builder()
                        .orderId(orderEntity.getOutTradeNo())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .totalAmount(item.getTotalAmount())
                        .build())
                .collect(Collectors.toList());
        payOrderItemDao.batchInsert(items);

        log.info("购物车订单保存完成 userId:{} outTradeNo:{} 明细条数:{}",
                aggregate.getUserId(), orderEntity.getOutTradeNo(), items.size());
    }

    @Override
    public PayOrderEntity queryUnpaidCartOrder(String userId) {
        PayOrder order = orderDao.queryUnpaidCartOrder(userId);
        return toPayOrderEntity(order);
    }
}

