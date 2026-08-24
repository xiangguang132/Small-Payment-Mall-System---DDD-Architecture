package cn.bugstack.trigger.job;

import cn.bugstack.domain.order.service.IOrderService;
import cn.bugstack.domain.timeout.ITimeoutOrderTaskProvider;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 普通订单-超时关单
 */
@Component
public class CloseOrderTimeoutTaskProvider implements ITimeoutOrderTaskProvider {

    @Resource
    private IOrderService orderService;

    @Override
    public List<String> queryTimeoutOutTradeNoList() {
        return orderService.queryTimeOutCloseOrderList();
    }

    @Override
    public boolean handle(String outTradeNo) {
        return orderService.changeOrderPayClose(outTradeNo);
    }

}
