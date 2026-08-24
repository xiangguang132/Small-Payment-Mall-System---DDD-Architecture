package cn.bugstack.trigger.job;

import cn.bugstack.domain.timeout.ITimeoutOrderTaskProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class TimeoutOrderJob {

    @Resource
    private List<ITimeoutOrderTaskProvider> timeoutOrderTaskProviders;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void exec() {
        if (timeoutOrderTaskProviders == null || timeoutOrderTaskProviders.isEmpty()) {
            log.info("定时任务，暂无超时任务 provider");
            return ;
        }
        for (ITimeoutOrderTaskProvider provider : timeoutOrderTaskProviders) {
            try {
                List<String> outTradeNos = provider.queryTimeoutOutTradeNoList();
                if (outTradeNos == null || outTradeNos.isEmpty()) {
                    log.info("定时任务[{}]，暂无超时单据", provider.taskName());
                    continue;
                }

                for (String outTradeNo : outTradeNos) {
                    boolean status = provider.handle(outTradeNo);
                    log.info("定时任务[{}] outTradeNo:{} status:{}",
                            provider.taskName(), outTradeNo, status);
                }
            } catch (Exception e) {
                log.error("定时任务[{}]执行失败", provider.taskName(), e);
            }
        }
    }

}
