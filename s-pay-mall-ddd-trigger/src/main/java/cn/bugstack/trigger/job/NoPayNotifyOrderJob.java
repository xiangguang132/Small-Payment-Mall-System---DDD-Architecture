package cn.bugstack.trigger.job;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuySettlementCommandEntity;
import cn.bugstack.domain.groupbuy.service.settlement.IGroupBuySettlementService;
import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.order.adapter.repository.IOrderRepository;
import cn.bugstack.domain.order.service.IOrderService;
import cn.bugstack.types.enums.OrderTypeEnum;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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
    private IOrderRepository orderRepository;
    @Resource
    private IGroupBuySettlementService groupBuySettlementService;
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

                    // 补偿拼团订单：查询订单类型，如果是拼团订单则触发结算，更新 group_buy_order 状态
                    PayOrderEntity payOrder = orderRepository.queryPayOrderByOutTradeNo(outTradeNo);
                    if (payOrder != null && OrderTypeEnum.GROUP_BUY.equals(payOrder.getOrderType())) {
                        try {
                            GroupBuySettlementCommandEntity settlementCommand = GroupBuySettlementCommandEntity.builder()
                                    .userId(payOrder.getUserId())
                                    .outTradeNo(outTradeNo)
                                    .payTime(LocalDateTime.now())
                                    .source("COMPENSATION")
                                    .channel("ALIPAY")
                                    .build();
                            groupBuySettlementService.settlementGroupBuyOrder(settlementCommand);
                            log.info("补偿任务-拼团订单结算完成 outTradeNo:{}", outTradeNo);
                        } catch (Exception ex) {
                            log.error("补偿任务-拼团订单结算失败 outTradeNo:{}", outTradeNo, ex);
                        }
                    }
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
