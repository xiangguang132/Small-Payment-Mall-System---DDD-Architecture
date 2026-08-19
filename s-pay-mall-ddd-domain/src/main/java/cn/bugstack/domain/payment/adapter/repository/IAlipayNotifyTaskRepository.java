package cn.bugstack.domain.payment.adapter.repository;

import cn.bugstack.domain.payment.model.entity.AlipayNotifyTaskEntity;

public interface IAlipayNotifyTaskRepository {

    AlipayNotifyTaskEntity queryByOutTradeNoAndTradeNo(String outTradeNo, String tradeNo);

    void save(AlipayNotifyTaskEntity alipayNotifyTaskEntity);
}
