package cn.bugstack.domain.payment.adapter.port;

/**
 * 支付宝支付接口
 */
public interface IAlipayPort {

    boolean tryLock(String outTradeNo);

    void unlock(String outTradeNo);

}
