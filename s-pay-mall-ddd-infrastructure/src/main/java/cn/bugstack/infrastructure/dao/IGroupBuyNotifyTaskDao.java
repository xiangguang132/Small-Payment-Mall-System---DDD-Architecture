package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.groupbuy.GroupBuyNotifyTask;

import java.util.List;

/**
 * 拼团成功回调任务
 */
public interface IGroupBuyNotifyTaskDao {

    /**
     * 创建拼团回调任务
     * @param task
     */
    void insert(GroupBuyNotifyTask task);

    /**
     * 查询未执行的回调任务-列表
     * @return
     */
    List<GroupBuyNotifyTask> queryUnExecutedNotifyTaskList();

    /**
     * 更新拼团回调任务状态
     * @param task
     * @return
     */
    int updateNotifyTaskStatusSuccess(GroupBuyNotifyTask task);          // where team_id + notify_status=0
    int updateNotifyTaskStatusRetry(GroupBuyNotifyTask task);            // notify_count+1, status=2
    int updateNotifyTaskStatusError(GroupBuyNotifyTask task);            // status=3

}
