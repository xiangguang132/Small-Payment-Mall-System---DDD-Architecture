package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.payment.PayOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IOrderDao {

    void insert(PayOrder order);

    PayOrder queryUnPayOrder(PayOrder order);

    void updateOrderPayInfo(PayOrder order);

    void changeOrderPaySuccess(PayOrder order);

    PayOrder queryPayOrderByOutTradeNo(@Param("outTradeNo") String outTradeNo);

    List<String> queryNoPayNotifyOrder();

    List<String> queryTimeoutCloseOrderList();

    boolean changeOrderClose(@Param("outTradeNo") String outTradeNo);

    /**
     * 乐观锁式置为退款中：只有 status 命中 PAY_SUCCESS/DEAL_DONE 才改成功（防重复发起退款）
     * @return true=本次请求抢到退款处理权
     */
    boolean changeOrderRefunding(@Param("outTradeNo") String outTradeNo);

    /**
     * 修改状态-退款成功
     * @return 是否更新成功
     */
    boolean changeOrderRefundResult(@Param("outTradeNo") String outTradeNo,
                                    @Param("fromStatus") String fromStatus,
                                    @Param("toStatus") String toStatus);

    /**
     * 分页查询用户支付订单（支持按状态筛选）
     * @param status 订单状态（null=不筛选）
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 每页条数
     * @return 订单列表
     */
    List<PayOrder> queryPageByStatusAndUserId(@Param("status") String status,
                                              @Param("userId") String userId,
                                              @Param("offset") Integer offset,
                                              @Param("limit") Integer limit);

    /**
     * 统计用户支付订单数量（支持按状态筛选）
     * @param status 订单状态（null=不筛选）
     * @param userId 用户ID
     * @return 数量
     */
    long countByStatusAndUserId(@Param("status") String status,
                                @Param("userId") String userId);
}
