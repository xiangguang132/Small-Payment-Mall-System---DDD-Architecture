package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.payment.AlipayNotifyTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAlipayNotifyTaskDao {

    AlipayNotifyTask queryByOutTradeNoAndTradeNo(@Param("outTradeNo") String outTradeNo, @Param("tradeNo") String tradeNo);

    void insert(AlipayNotifyTask alipayNotifyTask);

    AlipayNotifyTask queryByOutTradeNo(String outTradeNo);

    void updatedAlipayNotifyTaskFailed(String outTradeNo);

    void updatedAlipayNotifyTaskSuccess(String outTradeNo);

    void updatedAlipayNotifyTaskRetry(String outTradeNo);

    List<String> queryRetryOutTradeNoList();
}
