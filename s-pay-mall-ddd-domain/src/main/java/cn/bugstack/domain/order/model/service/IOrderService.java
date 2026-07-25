package cn.bugstack.domain.order.model.service;

import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.order.model.entity.ShopCartEntity;

/**
 * 订单
 */
public interface IOrderService {

    // 通过购物车实体对象创建支付单实体
    PayOrderEntity createOrder(ShopCartEntity shopCartEntity) throws Exception;
}
