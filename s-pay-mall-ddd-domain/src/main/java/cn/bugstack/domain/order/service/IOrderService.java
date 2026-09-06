package cn.bugstack.domain.order.service;

import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.order.model.entity.PayOrderItemEntity;
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

    // 通过锁单id创建支付单实体（userId/productId 来自确认下单请求，锁单仅校验有效性）
    PayOrderEntity createOrder(String userId, String productId, String lockId) throws Exception;

    // 创建拼团支付单（GROUP_BUY 类型），金额为拼团实付价
    PayOrderEntity createGroupBuyPayOrder(String userId, String productId,
                                          String productName, String outTradeNo,
                                          BigDecimal totalAmount) throws AlipayApiException;

    /**
     * 创建购物车结算订单（CART 类型）：多商品聚合为一笔 pay_order + 明细，券在聚合金额上择优抵扣
     * @param userId 用户ID
     * @param items 结算明细（商品+数量，单价为结算时实时价）
     * @param originalAmount 聚合原价（券抵扣前，每项 price × quantity 之和）
     * @param couponIds 用户选择的优惠券ID列表（可空）
     * @return 支付单（含支付宝支付表单 payUrl）
     */
    PayOrderEntity createCartOrder(String userId, List<PayOrderItemEntity> items,
                                   BigDecimal originalAmount, List<String> couponIds) throws Exception;

    // 依据 out_trade_no 修改订单状态为支付成功，并回写外部交易时间
    void changeOrderPaySuccess(String outTradeNo, Date outTradeTime);

    // 查询有效期内，未接收到支付回调的订单
    List<String> queryNoPayNotifyOrderList();

    // 查询超出15分钟未支付的订单
    List<String> queryTimeOutCloseOrderList();

    /**
     * 超时处理订单：未支付直接关单，已支付退单退款
     * @param outTradeNo 商户订单号
     * @return 是否处理成功
     */
    boolean timeoutCloseOrder(String outTradeNo);

    // 修改订单状态为关单
    boolean changeOrderPayClose(String outTradeNo);

    boolean refundOrder(String userId, String outTradeNo);

    /**
     * 查询支付订单（归属校验：仅限本人订单）
     * @param userId 当前用户ID
     * @param outTradeNo 商户订单号
     * @return 订单实体；不存在或非本人订单返回 null
     */
    PayOrderEntity queryByUserIdAndOutTradeNo(String userId, String outTradeNo);

    /**
     * 关闭待支付订单（归属+状态校验：仅限本人的 CREATE/PAY_WAIT 订单）
     * @param userId 当前用户ID
     * @param outTradeNo 商户订单号
     * @return 是否关闭成功
     */
    boolean closeOrder(String userId, String outTradeNo);

    /**
     * 待支付订单再次拉起支付：校验归属和状态后，幂等重生成支付宝表单
     * @param userId 当前用户ID
     * @param outTradeNo 商户订单号
     * @return 支付单（含 payUrl）
     */
    PayOrderEntity repayOrder(String userId, String outTradeNo) throws AlipayApiException;

    /**
     * 分页查询用户支付订单（支持按状态筛选）
     * @param status 订单状态（null=全部）
     * @param userId 用户ID
     * @param pageNo 页码
     * @param pageSize 每页条数
     * @return 订单列表
     */
    List<PayOrderEntity> queryPageByStatusAndUserId(String status, String userId, Integer pageNo, Integer pageSize);

    /**
     * 统计用户支付订单数量（支持按状态筛选）
     * @param status 订单状态（null=全部）
     * @param userId 用户ID
     * @return 数量
     */
    long countByStatusAndUserId(String status, String userId);

    /**
     * 直购试算：根据商品原价和用户选择的优惠券，预览折后实付价
     * @param originalPrice 商品原价
     * @param couponIds 用户选择的优惠券ID列表（可空）
     * @return 实付价（取最优券后的最低价）
     */
    BigDecimal previewOrderDiscount(BigDecimal originalPrice, List<String> couponIds);
}
