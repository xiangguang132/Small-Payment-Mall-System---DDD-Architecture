package cn.bugstack.trigger.job;

import cn.bugstack.domain.production.service.IProductionOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class ProductionOrderJob {

    @Value("${production.order.job.enabled:false}")
    private boolean enabled;

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Resource
    private IProductionOrderService productionOrderService;

    @Scheduled(cron = "${production.order.job.cron:0 0/1 * * * ?}")
    public void exec() {
        if (!enabled) {
            log.info("生产需求单任务未开启，本次跳过");
            return;
        }

        if (!running.compareAndSet(false, true)) {
            log.info("生产需求单任务正在执行，本次跳过");
            return;
        }

        try {
            log.info("开始执行生产需求单任务");
            productionOrderService.executeCreatedOrders();
        } finally {
            running.set(false);
        }
    }
}
