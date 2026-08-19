package cn.bugstack.infrastructure.adapter.port;

import cn.bugstack.domain.payment.adapter.port.IAlipayPort;
import cn.bugstack.infrastructure.redis.IRedisService;
import org.redisson.api.RLock;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class AlipayPort implements IAlipayPort {

    private static final String LOCK_KEY_PREFIX = "alipay_notify_task_lock:";

    @Resource
    private IRedisService redisService;

    @Override
    public boolean tryLock(String outTradeNo) {
        RLock lock = redisService.getLock(LOCK_KEY_PREFIX + outTradeNo);
        try {
            return lock.tryLock(3,30, TimeUnit.SECONDS);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public void unlock(String outTradeNo) {
        RLock lock = redisService.getLock(LOCK_KEY_PREFIX + outTradeNo);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
