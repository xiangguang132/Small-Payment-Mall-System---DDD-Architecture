package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.groupbuy.UserNotifyTask;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 拼团成功回调任务-站内信
 */
@Mapper
public interface IUserNotifyDao {

    /**
     * 创建拼团回调任务-站内信
     * @param list
     */
    void insertList(List<UserNotifyTask> list);

}
