package cn.bugstack.domain.timeout;

import java.util.List;

/**
 * 通用定时任务接口
 */
public interface ITimeoutOrderTaskProvider {

    String taskName();

    List<String> queryTimeoutOutTradeNoList();

    boolean handle(String outTradeNo);
}
