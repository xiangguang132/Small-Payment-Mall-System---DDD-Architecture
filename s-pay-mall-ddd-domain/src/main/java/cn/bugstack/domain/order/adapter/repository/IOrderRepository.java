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

    // 普通下单
    /**
     * 创建普通订单
     * @param orderAggregate
     */
    void doSaveOrder(CreateOrderAggregate orderAggregate);

    /**
     * 保存拼团支付单（GROUP_BUY 类型）
     * 与 doSaveOrder 区别：金额取自 payOrderEntity（拼团实付价），orderType 固定 GROUP_BUY
     * @param payOrderEntity 拼团支付单
     */
    void saveGroupBuyPayOrder(PayOrderEntity payOrderEntity);

    /**
     * 查询当前是否存在同等商品或者单子未支付的订单
     * ----------------------------------
     - 用户刚要下单时
     - 系统先查一下
     - 看这个购物车/用户/商品组合下
     - 有没有已经存在的“未支付订单”
     * @param shopCartEntity
     * @return
     */
    OrderEntity queryUnPayOrder(ShopCartEntity shopCartEntity);

    /**
     * 根据商户订单号查询支付单
     * @param outTradeNo 商户订单号/支付宝 out_trade_no
     * @return 支付单
     */
    PayOrderEntity queryPayOrderByOutTradeNo(String outTradeNo);

    /**
     * 更新订单支付信息
     * @param payOrderEntity
     */
    void updateOrderPayInfo(PayOrderEntity payOrderEntity);

    /**
     * 修改状态-success
     * @param outTradeNo
     * @param outTradeTime 外部交易时间
     */
    void changeOrderPaySuccess(String outTradeNo, Date outTradeTime);

    /**
     * 查询有效期内，未接收到支付回调的订单
     * 用于 NoPayNotifyOrderJob 这种补偿任务
     * 重点是“没收到通知”，不是“没支付”
     * @return
     */
    List<String> queryNoPayNotifyOrderList();

    /**
     * 查询超时 15 分钟需要关单的订单列表
     * @return
     */
    List<String> queryTimeOutCloseOrderList();

    /**
     * 修改状态为关单状态
     * @return
     */
    boolean changeOrderPayClose(String outTradeNo);

    /**
     * 原子占锁：置为退款中，抢到处理权返回 true。
     * @return true=本轮应执行支付宝退款；false=已有人在退/已退，直接返回
     */
    boolean changeOrderRefunding(String outTradeNo);

    /**
     * 退款成功
     * @return 是否更新成功
     */
    boolean changeOrderRefundResult(String outTradeNo, String fromStatus, String toStatus);
}





