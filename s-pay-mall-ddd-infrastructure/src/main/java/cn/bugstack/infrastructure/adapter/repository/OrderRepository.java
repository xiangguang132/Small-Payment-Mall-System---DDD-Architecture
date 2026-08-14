package cn.bugstack.infrastructure.adapter.repository;

import cn.bugstack.domain.order.adapter.repository.IOrderRepository;
import cn.bugstack.domain.order.event.PaySuccessMessageEvent;
import cn.bugstack.domain.order.model.aggregate.CreateOrderAggregate;
import cn.bugstack.domain.order.model.entity.OrderEntity;
import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.order.model.entity.ProductEntity;
import cn.bugstack.domain.order.model.entity.ShopCartEntity;
import cn.bugstack.domain.order.model.valobj.OrderStatusVO;
import cn.bugstack.infrastructure.dao.IOrderDao;
import cn.bugstack.infrastructure.dao.po.pay.PayOrder;
import cn.bugstack.types.enums.OrderTypeEnum;
import cn.bugstack.types.event.BaseEvent;
import com.alibaba.fastjson2.JSON;
import com.google.common.eventbus.EventBus;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 该部分着重于实现业务，但是不调用数据库
 * 将修改数据的操作进行下沉
 * 下沉到数据库 dao层
 */

@Repository
public class OrderRepository implements IOrderRepository {

    @Resource
    private IOrderDao orderDao;
    @Resource
    private EventBus eventBus;
    @Resource
    private PaySuccessMessageEvent paySuccessMessageEvent;

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
        order.setOrderType(OrderTypeEnum.DIRECT.getCode());
        order.setStatus(orderEntity.getOrderStatus().getCode());

        orderDao.insert(order);

        // todo 存入缓存；缓存key聚合到对象中提供
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

    @Override
    public void changeOrderPaySuccess(String orderId, Date outTradeTime) {
        PayOrder order = new PayOrder();
        order.setOrderId(orderId);
        order.setStatus(OrderStatusVO.PAY_SUCCESS.getCode());
        order.setOutTradeTime(outTradeTime);
        orderDao.changeOrderPaySuccess(order);

        // todo 发送 mq 消息
        BaseEvent.EventMessage<PaySuccessMessageEvent.PaySuccessMessage> eventMessage = paySuccessMessageEvent.buildEventMessage(PaySuccessMessageEvent.PaySuccessMessage.builder().tradeNo(orderId).build());
        PaySuccessMessageEvent.PaySuccessMessage paySuccessMessage = eventMessage.getData();

        eventBus.post(JSON.toJSONString(paySuccessMessage));
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
    public boolean changeOrderPayClose(String orderId) {
        return orderDao.changeOrderClose(orderId);
    }

}
