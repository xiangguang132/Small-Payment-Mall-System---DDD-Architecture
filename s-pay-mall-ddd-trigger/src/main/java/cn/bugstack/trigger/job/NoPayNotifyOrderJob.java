package cn.bugstack.trigger.job;

import cn.bugstack.domain.order.service.IOrderService;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 检测未接收到或未正确处理的支付回调通知
 * @create 2024-07-25 07:17
 */
@Slf4j
@Component()
public class NoPayNotifyOrderJob {

    @Resource
    private IOrderService orderService;
    @Resource
    private AlipayClient alipayClient;

    @Scheduled(cron = "0/3 * * * * ?")
    public void exec() {
        try {
            List<String> orderIds = orderService.queryNoPayNotifyOrderList();
            if (null == orderIds || orderIds.isEmpty()) return;

            for (String orderId : orderIds) {
                AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
                AlipayTradeQueryModel bizModel = new AlipayTradeQueryModel();
                bizModel.setOutTradeNo(orderId);
                request.setBizModel(bizModel);


                AlipayTradeQueryResponse alipayTradeQueryResponse = alipayClient.execute(request);
                String code = alipayTradeQueryResponse.getCode();
                String tradeStatus = alipayTradeQueryResponse.getTradeStatus();
                // 支付宝查询成功且明确已支付，才更新本地订单状态
                if ("10000".equals(code) && "TRADE_SUCCESS".equals(tradeStatus)) {
                    orderService.changeOrderPaySuccess(orderId, alipayTradeQueryResponse.getSendPayDate());
                } else {
                    log.info("检测未接收到或未正确处理的支付回调通知，订单未支付成功 orderId:{} code:{} tradeStatus:{}", orderId, code, tradeStatus);
                }

            }
        } catch (Exception e) {
            log.error("检测未接收到或未正确处理的支付回调通知失败", e);
        }
    }

}
