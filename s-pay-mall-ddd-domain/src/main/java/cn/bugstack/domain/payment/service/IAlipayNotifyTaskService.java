package cn.bugstack.domain.payment.service;

import java.util.Map;

public interface IAlipayNotifyTaskService {

    /**
     * 创建回调任务
     * @param params
     */
    void saveNotifyTask(Map<String, String> params);

    /**
     * 消费 与 处理 回调任务
     * @param outTradeNo
     */
    void processTask(String outTradeNo);
}
