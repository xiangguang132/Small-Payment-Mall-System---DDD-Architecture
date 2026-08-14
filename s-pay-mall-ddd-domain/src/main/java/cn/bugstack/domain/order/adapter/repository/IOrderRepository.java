package cn.bugstack.domain.order.adapter.repository;

import cn.bugstack.domain.order.model.aggregate.CreateOrderAggregate;
import cn.bugstack.domain.order.model.entity.OrderEntity;
import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.order.model.entity.ShopCartEntity;

import java.util.Date;
import java.util.List;

/**
 * 该部分着重于对修改数据库操作的业务进行接口定义
 * 继续下沉
 * 下沉到 orderRepository 层
 * 让 orderRepository 类实现，这部分是 "接口"
 */

public interface IOrderRepository {
    void doSaveOrder(CreateOrderAggregate orderAggregate);

    OrderEntity queryUnPayOrder(ShopCartEntity shopCartEntity);

    /**
     * 更新订单支付信息
     * @param payOrderEntity
     */
    void updateOrderPayInfo(PayOrderEntity payOrderEntity);

    /**
     * 修改状态-success
     * @param orderId
     * @param outTradeTime 外部交易时间
     */
    void changeOrderPaySuccess(String orderId, Date outTradeTime);

    /**
     * 查询有效期内，未接收到支付回调的订单
     * @return
     */
    List<String> queryNoPayNotifyOrderList();

    /**
     * 查询超时15min的订单进行关单
     * @return
     */
    List<String> queryTimeOutCloseOrderList();

    /**
     * 修改状态为关单状态
     * @return
     */
    boolean changeOrderPayClose(String orderId);
}
