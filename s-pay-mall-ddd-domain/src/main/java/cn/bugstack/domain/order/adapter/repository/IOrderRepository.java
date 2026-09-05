package cn.bugstack.domain.order.adapter.repository;

import cn.bugstack.domain.order.model.aggregate.CreateCartOrderAggregate;
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
     * 查询超时 15 分钟需要处理的订单列表（含未支付和已支付未关单）
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

    /**
     * 分页查询用户支付订单（支持按状态筛选）
     * @param status 订单状态（null=不筛选）
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 每页条数
     * @return 订单列表
     */
    List<PayOrderEntity> queryPageByStatusAndUserId(String status, String userId, Integer offset, Integer limit);

    /**
     * 统计用户支付订单数量（支持按状态筛选）
     * @param status 订单状态（null=不筛选）
     * @param userId 用户ID
     * @return 数量
     */
    long countByStatusAndUserId(String status, String userId);

    /**
     * 保存购物车结算订单（pay_order 头表 + pay_order_item 明细，单事务）
     * @param aggregate 购物车结算聚合
     */
    void saveCartOrder(CreateCartOrderAggregate aggregate);

    /**
     * 查询用户未支付的购物车订单（结算幂等守卫）
     * @param userId 用户ID
     * @return 最近的 CART 型未支付订单；无则返回 null
     */
    PayOrderEntity queryUnpaidCartOrder(String userId);
}





