package cn.bugstack.domain.groupbuy.service.trial.thread;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyActivityEntity;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyActivityRepository;

import javax.annotation.Resource;
import java.util.concurrent.Callable;

public class QueryGroupBuyActivityVOThreadTask implements Callable<GroupBuyActivityEntity> {

    private final Long activityId;

    @Resource
    private IGroupBuyActivityRepository repository;

    public QueryGroupBuyActivityVOThreadTask(Long activityId, IGroupBuyActivityRepository repository) {
        this.activityId = activityId;
        this.repository = repository;
    }

    @Override
    public GroupBuyActivityEntity call() throws Exception {
        Long availableActivityId = activityId;

        // 查询 活动配置
        if (availableActivityId == null || activityId == null) {
            return null;
        }
        return repository.queryGroupBuyActivityByActivityId(availableActivityId);

    }
}
