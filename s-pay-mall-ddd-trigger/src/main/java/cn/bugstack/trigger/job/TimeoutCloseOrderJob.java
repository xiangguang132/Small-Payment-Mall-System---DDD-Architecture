package cn.bugstack.trigger.job;

import cn.bugstack.domain.order.service.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 超时关单
 * @create 2024-07-25 08:19
 */
@Slf4j
@Component()
public class TimeoutCloseOrderJob {

    @Resource
    private IOrderService orderService;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void exec() {
        try {
            List<String> outTradeNos = orderService.queryTimeOutCloseOrderList();
            if (null == outTradeNos || outTradeNos.isEmpty()) {
                log.info("定时任务，超时30分钟订单关闭，暂无超时未支付订单 outTradeNos is null");
                return;
            }
            for (String outTradeNo : outTradeNos) {
                boolean status = orderService.changeOrderPayClose(outTradeNo);
                log.info("定时任务，超时30分钟订单关闭 outTradeNo: {} status：{}", outTradeNo, status);
            }
        } catch (Exception e) {
            log.error("定时任务，超时15分钟订单关闭失败", e);
        }
    }

}
