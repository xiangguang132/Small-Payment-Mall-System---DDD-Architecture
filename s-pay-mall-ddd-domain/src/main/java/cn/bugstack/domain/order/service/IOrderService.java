package cn.bugstack.domain.order.service;

import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.order.model.entity.ShopCartEntity;

import java.util.Date;
import java.util.List;

/**
 * 订单
 * 该部分定义订单相关业务接口
 * 将实现下沉交给 OrderService
 * 下沉到 OrderService 实现层
 */
public interface IOrderService {

    // 通过购物车实体对象创建支付单实体
    PayOrderEntity createOrder(ShopCartEntity shopCartEntity) throws Exception;

    // 依据 out_trade_no 修改订单状态为支付成功，并回写外部交易时间
    void changeOrderPaySuccess(String outTradeNo, Date outTradeTime);

    // 查询有效期内，未接收到支付回调的订单
    List<String> queryNoPayNotifyOrderList();

    // 查询超出15分钟未支付的订单
    List<String> queryTimeOutCloseOrderList();

    // 修改订单状态为关单
    boolean changeOrderPayClose(String outTradeNo);

    boolean refundOrder(String userId, String outTradeNo);
}
