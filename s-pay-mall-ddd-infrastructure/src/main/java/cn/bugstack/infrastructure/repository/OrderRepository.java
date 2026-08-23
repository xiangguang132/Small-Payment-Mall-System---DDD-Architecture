package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.order.adapter.repository.IOrderRepository;
import cn.bugstack.domain.order.event.PaySuccessMessageEvent;
import cn.bugstack.domain.order.model.aggregate.CreateOrderAggregate;
import cn.bugstack.domain.order.model.entity.OrderEntity;
import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.order.model.entity.ProductEntity;
import cn.bugstack.domain.order.model.entity.ShopCartEntity;
import cn.bugstack.domain.order.model.valobj.OrderStatusVO;
import cn.bugstack.infrastructure.dao.IOrderDao;
import cn.bugstack.infrastructure.dao.po.payment.PayOrder;
import cn.bugstack.infrastructure.event.EventPublisher;
import cn.bugstack.types.enums.OrderTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

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
        order.setTotalAmount(productEntity.getPrice());
        order.setOrderType(OrderTypeEnum.DIRECT.getCode());
        order.setStatus(orderEntity.getOrderStatus().getCode());

        orderDao.insert(order);

        // todo 存入缓存；缓存key聚合到对象中提供
//        redisService.setValue(PayOrder.cacheKey(userId, orderEntity.getOutTradeNo()), order);

    }

    @Override
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
        if (order == null) {
            return null;
        }
        return PayOrderEntity.builder()
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
}
