package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.groupbuy.model.entity.UserNotifyEntity;
import cn.bugstack.domain.groupbuy.repository.IUserNotifyRepository;
import cn.bugstack.infrastructure.dao.IUserNotifyDao;
import cn.bugstack.infrastructure.dao.po.groupbuy.UserNotifyTask;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class UserNotifyRepository implements IUserNotifyRepository {

    @Resource
    private IUserNotifyDao userNotifyDao;

    @Override
    public void insertList(List<UserNotifyEntity> list) {
        userNotifyDao.insertList(list.stream()
                .map(this::toPo)
                .collect(Collectors.toList()));
    }

    private UserNotifyTask toPo(UserNotifyEntity entity) {
        return UserNotifyTask.builder()
                .userId(entity.getUserId())
                .teamId(entity.getTeamId())
                .activityId(entity.getActivityId())
                .notifyType(entity.getNotifyType())
                .title(entity.getTitle())
                .content(entity.getContent())
                .isRead(entity.getIsRead())
                .build();
    }

}
