package cn.bugstack.domain.payment.adapter.port;

public interface IAlipayPort {

    boolean tryLock(String outTradeNo);

    void unlock(String outTradeNo);

}
