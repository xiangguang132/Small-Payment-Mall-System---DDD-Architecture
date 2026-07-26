package cn.bugstack.domain.order.model.service;

import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.order.model.entity.ShopCartEntity;

import java.util.List;

/**
 * 订单
 * 该部分定义订单相关业务接口
 */
public interface IOrderService {

    // 通过购物车实体对象创建支付单实体
    PayOrderEntity createOrder(ShopCartEntity shopCartEntity) throws Exception;

    // 依据 orderid 修改订单状态位支付成功
    void changeOrderPaySuccess(String orderId);

    // 查询有效期内，未接收到支付回调的订单
    List<String> queryNoPayNotifyOrderList();

    // 查询超出15分钟未支付的订单
    List<String> queryTimeOutCloseOrderList();

    // 修改订单状态为关单
    boolean changeOrderPayClose(String orderId);
}
