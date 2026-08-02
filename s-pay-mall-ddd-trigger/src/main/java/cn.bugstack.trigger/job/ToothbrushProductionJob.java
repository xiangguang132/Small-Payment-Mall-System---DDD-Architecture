package cn.bugstack.trigger.job;

import cn.bugstack.domain.materialstockallocation.service.IMaterialStockAllocationService;
import cn.bugstack.domain.warehousestock.service.IStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class ToothbrushProductionJob {

    // 硬编码：生产 100 个牙刷
    private static final Long TOOTHBRUSH_PRODUCT_ID = 1L;
    private static final Long FINISHED_PRODUCT_WAREHOUSE_ID = 1L;
    private static final Integer PRODUCTION_QTY = 100;

    // 硬编码：原料
    private static final Long PLASTIC_MATERIAL_ID = 2L;
    private static final Integer PLASTIC_QTY = 10;

    private static final Long WOOD_MATERIAL_ID = 3L;
    private static final Integer WOOD_QTY = 10;

    @Value("${production.toothbrush.job.enabled:false}")
    private boolean enabled;

    @Value("${production.toothbrush.job.run-once:true}")
    private boolean runOnce;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean executed = new AtomicBoolean(false);

    @Resource
    private IMaterialStockAllocationService materialStockAllocationService;

    @Resource
    private IStockService stockService;

    @Scheduled(cron = "${production.toothbrush.job.cron:0 0/1 * * * ?}")
    public void exec() {
        log.info("开始执行定时任务");
        if (!enabled) {
            return;
        }

        if (runOnce && !executed.compareAndSet(false, true)) {
            log.info("牙刷生产任务已尝试执行，本次跳过");
            return;
        }

        if (!running.compareAndSet(false, true)) {
            log.info("牙刷生产任务正在执行，本次跳过");
            return;
        }

        List<String> lockedAllocationNos = new ArrayList<>();

        try {
            String plasticAllocationNo = materialStockAllocationService.create(
                    PLASTIC_MATERIAL_ID,
                    PLASTIC_QTY,
                    "生产牙刷-塑料备料"
            );

            String woodAllocationNo = materialStockAllocationService.create(
                    WOOD_MATERIAL_ID,
                    WOOD_QTY,
                    "生产牙刷-木头备料"
            );

            materialStockAllocationService.lock(plasticAllocationNo);
            lockedAllocationNos.add(plasticAllocationNo);

            materialStockAllocationService.lock(woodAllocationNo);
            lockedAllocationNos.add(woodAllocationNo);

            materialStockAllocationService.autoOutbound(plasticAllocationNo);
            materialStockAllocationService.autoOutbound(woodAllocationNo);

            stockService.inbound(
                    FINISHED_PRODUCT_WAREHOUSE_ID,
                    TOOTHBRUSH_PRODUCT_ID,
                    PRODUCTION_QTY
            );

            log.info("牙刷生产任务完成 productId:{} quantity:{}", TOOTHBRUSH_PRODUCT_ID, PRODUCTION_QTY);

        } catch (Exception e) {
            log.warn("牙刷生产任务失败，开始释放已锁定原料 reason:{}", e.getMessage());
            releaseLockedAllocations(lockedAllocationNos);
        } finally {
            running.set(false);
        }
    }

    private void releaseLockedAllocations(List<String> allocationNos) {
        for (String allocationNo : allocationNos) {
            try {
                materialStockAllocationService.release(allocationNo);
                log.info("牙刷生产任务释放原料成功 allocationNo:{}", allocationNo);
            } catch (Exception e) {
                log.error("牙刷生产任务释放原料失败 allocationNo:{}", allocationNo, e);
            }
        }
    }
}
