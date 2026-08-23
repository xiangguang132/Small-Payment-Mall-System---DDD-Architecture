package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.groupbuy.UserNotifyTask;

import java.util.List;

/**
 * 拼团成功回调任务-站内信
 */
public interface IUserNotifyDao {

    /**
     * 创建拼团回调任务-站内信
     * @param list
     */
    void insertList(List<UserNotifyTask> list);

}
