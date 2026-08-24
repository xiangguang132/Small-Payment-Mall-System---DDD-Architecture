package cn.bugstack.domain.groupbuy.repository;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyNotifyTaskEntity;

import java.util.List;

public interface IGroupBuyNotifyTaskRepository {

    List<GroupBuyNotifyTaskEntity> queryUnExecutedNotifyTaskList();

    int updateNotifyTaskStatusSuccess(GroupBuyNotifyTaskEntity task);

    int updateNotifyTaskStatusError(GroupBuyNotifyTaskEntity task);

    int updateNotifyTaskStatusRetry(GroupBuyNotifyTaskEntity task);

    /**
     * 创建回调任务
     */
    int insertNotifyTask(GroupBuyNotifyTaskEntity task);
}
