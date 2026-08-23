package cn.bugstack.trigger.job;

import cn.bugstack.domain.groupbuy.service.task.IGroupBuyNotifyTaskService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@Component
public class GroupBuyNotifyJob {

    @Resource
    private IGroupBuyNotifyTaskService groupBuyNotifyTaskService;

    @Scheduled(cron = "0 0/1 * * * ?")   // 每分钟；量大可放宽
    public void exec() {
        try {
            Map<String, Integer> result = groupBuyNotifyTaskService.execNotifyJob();
            log.info("成团通知兜底任务完成 result:{}", JSON.toJSONString(result));
        } catch (Exception e) {
            log.error("成团通知兜底任务失败", e);
        }
    }

}
