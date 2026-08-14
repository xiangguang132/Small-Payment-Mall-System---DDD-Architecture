package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.payment.AlipayNotifyTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IAlipayNotifyTaskDao {

    AlipayNotifyTask queryByOutTradeNoAndTradeNo(@Param("outTradeNo") String outTradeNo, @Param("tradeNo") String tradeNo);

    void insert(AlipayNotifyTask alipayNotifyTask);
}
