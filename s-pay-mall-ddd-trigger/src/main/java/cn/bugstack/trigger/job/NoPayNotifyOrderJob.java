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

    @Scheduled(cron = "0/30 * * * * ?")
    public void exec() {
        try {
            List<String> outTradeNos = orderService.queryNoPayNotifyOrderList();
            if (null == outTradeNos || outTradeNos.isEmpty()) return;

            for (String outTradeNo : outTradeNos) {
                AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
                AlipayTradeQueryModel bizModel = new AlipayTradeQueryModel();
                bizModel.setOutTradeNo(outTradeNo);
                request.setBizModel(bizModel);


                AlipayTradeQueryResponse alipayTradeQueryResponse = alipayClient.execute(request);
                String code = alipayTradeQueryResponse.getCode();
                String tradeStatus = alipayTradeQueryResponse.getTradeStatus();
                // 支付宝查询成功且明确已支付，才更新本地订单状态
                if ("10000".equals(code) && "TRADE_SUCCESS".equals(tradeStatus)) {
                    orderService.changeOrderPaySuccess(outTradeNo, alipayTradeQueryResponse.getSendPayDate());
                } else if ("ACQ.TRADE_NOT_EXIST".equals(alipayTradeQueryResponse.getSubCode())) {
                    // 用户尚未提交支付表单，支付宝侧还没有这笔交易，属正常情况，等超时关单即可，降为 debug 避免刷屏
                    log.debug("检测支付回调通知，支付宝交易不存在（用户未支付），等待超时关单 outTradeNo:{}", outTradeNo);
                } else {
                    log.info("检测未接收到或未正确处理的支付回调通知，订单未支付成功 outTradeNo:{} code:{} tradeStatus:{}", outTradeNo, code, tradeStatus);
                }

            }
        } catch (Exception e) {
            log.error("检测未接收到或未正确处理的支付回调通知失败", e);
        }
    }

}
