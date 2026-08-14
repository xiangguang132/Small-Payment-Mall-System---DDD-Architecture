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

}
