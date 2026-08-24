package cn.bugstack.domain.groupbuy.repository;

import cn.bugstack.domain.groupbuy.model.entity.UserNotifyEntity;

import java.util.List;

public interface IUserNotifyRepository {

    /**
     * 批量写入站内信（依赖唯一键 uk_user_team_type 防 MQ 重复消费）
     * @param list
     */
    void insertList(List<UserNotifyEntity> list);

}
