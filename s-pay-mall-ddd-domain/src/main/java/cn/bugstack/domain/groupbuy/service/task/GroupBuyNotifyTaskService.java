package cn.bugstack.domain.groupbuy.service.task;

import cn.bugstack.domain.groupbuy.adapter.IGroupBuyPort;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyNotifyTaskEntity;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyNotifyTaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GroupBuyNotifyTaskService implements IGroupBuyNotifyTaskService {

    @Resource
    private IGroupBuyNotifyTaskRepository repository;
    @Resource
    private IGroupBuyPort groupBuyPort;

    @Override
    public Map<String, Integer> execNotifyJob() throws Exception {
        return exec(repository.queryUnExecutedNotifyTaskList());
    }

    @Override
    public Map<String, Integer> execNotifyJob(GroupBuyNotifyTaskEntity notifyTask) throws Exception {
        return exec(Collections.singletonList(notifyTask));
    }

    private Map<String, Integer> exec(List<GroupBuyNotifyTaskEntity> list) throws Exception {
        int success = 0, error = 0, retry = 0;
        for (GroupBuyNotifyTaskEntity task : list) {
            String response = groupBuyPort.groupBuyNotify(task);
            if ("success".equals(response)) {
                if (1 == repository.updateNotifyTaskStatusSuccess(task)) success++;
            } else if ("error".equals(response)) {
                if (task.getNotifyCount() != null && task.getNotifyCount() > 4) {
                    if (1 == repository.updateNotifyTaskStatusError(task)) error++;
                } else {
                    if (1 == repository.updateNotifyTaskStatusRetry(task)) retry++;
                }
            }
        }
        Map<String, Integer> result = new HashMap<>();
        result.put("waitCount", list.size());
        result.put("successCount", success);
        result.put("errorCount", error);
        result.put("retryCount", retry);
        return result;
    }
}
