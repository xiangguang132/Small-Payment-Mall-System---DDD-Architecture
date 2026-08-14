package cn.bugstack.domain.payment.service;

import java.util.Map;

public interface IAlipayNotifyTaskService {

    void saveNotifyTask(Map<String, String> params);
}
