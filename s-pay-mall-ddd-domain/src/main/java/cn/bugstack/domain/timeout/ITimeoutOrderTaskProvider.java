package cn.bugstack.domain.timeout;

import java.util.List;

/**
 * 通用超时订单任务接口
 */
public interface ITimeoutOrderTaskProvider {

    /**
     * 查询需要处理的超时订单号列表
     */
    List<String> queryTimeoutOutTradeNoList();

    /**
     * 处理单个超时订单，返回是否成功
     */
    boolean handle(String outTradeNo);

}
