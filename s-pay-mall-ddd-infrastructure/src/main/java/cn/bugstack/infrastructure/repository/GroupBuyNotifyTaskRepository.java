package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyNotifyTaskEntity;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyNotifyTaskRepository;
import cn.bugstack.infrastructure.dao.IGroupBuyNotifyTaskDao;
import cn.bugstack.infrastructure.dao.po.groupbuy.GroupBuyNotifyTask;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class GroupBuyNotifyTaskRepository implements IGroupBuyNotifyTaskRepository {

    @Resource
    private IGroupBuyNotifyTaskDao groupBuyNotifyTaskDao;

    @Override
    public List<GroupBuyNotifyTaskEntity> queryUnExecutedNotifyTaskList() {
        return groupBuyNotifyTaskDao.queryUnExecutedNotifyTaskList().stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public int updateNotifyTaskStatusSuccess(GroupBuyNotifyTaskEntity task) {
        return groupBuyNotifyTaskDao.updateNotifyTaskStatusSuccess(toPo(task));
    }

    @Override
    public int updateNotifyTaskStatusError(GroupBuyNotifyTaskEntity task) {
        return groupBuyNotifyTaskDao.updateNotifyTaskStatusError(toPo(task));
    }

    @Override
    public int updateNotifyTaskStatusRetry(GroupBuyNotifyTaskEntity task) {
        return groupBuyNotifyTaskDao.updateNotifyTaskStatusRetry(toPo(task));
    }

    private GroupBuyNotifyTask toPo(GroupBuyNotifyTaskEntity entity) {
        return GroupBuyNotifyTask.builder()
                .teamId(entity.getTeamId())
                .activityId(entity.getActivityId())
                .notifyMq(entity.getNotifyMQ())
                .notifyStatus(entity.getNotifyStatus())
                .notifyCount(entity.getNotifyCount())
                .parameterJson(entity.getParameterJson())
                .build();
    }

    private GroupBuyNotifyTaskEntity toEntity(GroupBuyNotifyTask po) {
        return GroupBuyNotifyTaskEntity.builder()
                .activityId(po.getActivityId())
                .teamId(po.getTeamId())
                .notifyType(po.getNotifyMq() == null ? null : "MQ")
                .notifyMQ(po.getNotifyMq())
                .notifyCount(po.getNotifyCount())
                .notifyStatus(po.getNotifyStatus())
                .parameterJson(po.getParameterJson())
                .build();
    }
}
