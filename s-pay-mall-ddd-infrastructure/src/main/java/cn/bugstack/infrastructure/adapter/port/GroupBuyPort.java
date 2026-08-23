package cn.bugstack.infrastructure.adapter.port;

import cn.bugstack.domain.groupbuy.adapter.IGroupBuyPort;
import cn.bugstack.domain.groupbuy.model.entity.GroupBuyNotifyTaskEntity;
import cn.bugstack.infrastructure.event.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class GroupBuyPort implements IGroupBuyPort {

    @Resource
    private EventPublisher eventPublisher;

    @Override
    public String groupBuyNotify(GroupBuyNotifyTaskEntity notifyTask) throws Exception {
        // 只做站内信场景：消息体 = parameterJson，路由 = notifyMQ（topic.team_success）
        eventPublisher.publish(notifyTask.getNotifyMQ(), notifyTask.getParameterJson());
        return "success";
    }
}
