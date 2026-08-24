package cn.bugstack.trigger.job;

import cn.bugstack.domain.timeout.ITimeoutOrderTaskProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 通用-处理超时订单任务
 * 只保留调度-只负责触发
 */
@Slf4j
@Component
public class TimeoutOrderJob {

    @Resource
    private List<ITimeoutOrderTaskProvider> timeoutOrderTaskProviders;

    @Scheduled(cron = "0 */1 * * * ?")
    public void exec() {
        for (ITimeoutOrderTaskProvider provider : timeoutOrderTaskProviders) {
            List<String> outTradeNoList = provider.queryTimeoutOutTradeNoList();
            for (String outTradeNo : outTradeNoList) {
                try {
                    boolean success = provider.handle(outTradeNo);
                    if (!success) {
                        log.info("超时任务：处理失败，跳过 outTradeNo={}", outTradeNo);
                        continue;
                    }
                    log.info("超时任务：处理成功 outTradeNo={}", outTradeNo);
                } catch (Exception e) {
                    log.error("超时任务：处理异常 outTradeNo={}", outTradeNo, e);
                }
            }
        }
    }

}
