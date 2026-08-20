package cn.bugstack.domain.payment.adapter.repository;

import cn.bugstack.domain.payment.model.entity.AlipayNotifyTaskEntity;

import java.util.List;

public interface IAlipayNotifyTaskRepository {

    AlipayNotifyTaskEntity queryByOutTradeNoAndTradeNo(String outTradeNo, String tradeNo);

    void save(AlipayNotifyTaskEntity alipayNotifyTaskEntity);

    AlipayNotifyTaskEntity queryByOutTradeNo(String outTradeNo);

    void updatedAlipayNotifyTaskFailed(String outTradeNo);

    void updatedAlipayNotifyTaskSuccess(String outTradeNo);

    void updatedAlipayNotifyTaskRetry(String outTradeNo);

    List<String> queryRetryOutTradeNoList();
}
