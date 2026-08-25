package cn.bugstack.domain.order.service;

import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.order.model.entity.ShopCartEntity;
import com.alipay.api.AlipayApiException;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单
 * 该部分定义订单相关业务接口
 * 将实现下沉交给 OrderService
 * 下沉到 OrderService 实现层
 */
public interface IOrderService {

    // 通过锁单id创建支付单实体（userId 来自登录态，订单归属以创单请求为准）
    PayOrderEntity createOrder(String userId, String lockId) throws Exception;

    // 创建拼团支付单（GROUP_BUY 类型），金额为拼团实付价
    PayOrderEntity createGroupBuyPayOrder(String userId, String productId,
                                          String productName, String outTradeNo,
                                          BigDecimal totalAmount) throws AlipayApiException;

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
