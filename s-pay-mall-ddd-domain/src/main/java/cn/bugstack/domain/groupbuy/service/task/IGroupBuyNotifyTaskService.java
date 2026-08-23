package cn.bugstack.domain.groupbuy.service.task;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyNotifyTaskEntity;

import java.util.Map;

public interface IGroupBuyNotifyTaskService {

    /**
     * 扫未执行任务并回调；返回 成功/失败/重试 计数
     * @return
     * @throws Exception
     */
    Map<String, Integer> execNotifyJob() throws Exception;

    /**
     * 立即执行-指定单条任务（结算后即时投递用）
     * @param notifyTask
     * @return
     * @throws Exception
     */
    Map<String, Integer> execNotifyJob(GroupBuyNotifyTaskEntity notifyTask) throws Exception;

}
