package cn.bugstack.domain.groupbuy.service.user;

import cn.bugstack.domain.groupbuy.model.entity.UserNotifyEntity;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyOrderRepository;
import cn.bugstack.domain.groupbuy.repository.IUserNotifyRepository;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserNotifyTaskService implements IUserNotifyTaskService {

    @Resource
    private IGroupBuyOrderRepository groupBuyOrderRepository;
    @Resource
    private IUserNotifyRepository userNotifyRepository;

    @Override
    public void writeTeamSuccessNotify(String message) {
        // 解析消息，提取 teamId
        JSONObject param = JSONObject.parseObject(message);
        String teamId = param.getString("teamId");

        // 查询团内所有成员
        List<String> userIdList = groupBuyOrderRepository.queryUserIdListByTeamId(teamId);
        if (CollectionUtils.isEmpty(userIdList)) {
            log.warn("成团通知-团内无成员 teamId:{}", teamId);
            return;
        }

        // 构建站内信实体列表
        List<UserNotifyEntity> notifyList = userIdList.stream()
                .map(userId -> UserNotifyEntity.builder()
                        .userId(userId)
                        .teamId(teamId)
                        .notifyType("TEAM_SUCCESS")
                        .title("拼团成功提醒")
                        .content("您参与的拼团已成团")
                        .isRead(0)
                        .build())
                .collect(Collectors.toList());

        // 批量写入站内信（依赖唯一键 uk_user_team_type 防 MQ 重复消费）
        userNotifyRepository.insertList(notifyList);
        log.info("成团通知-站内信写入完成 teamId:{} 用户数:{}", teamId, notifyList.size());
    }

}
