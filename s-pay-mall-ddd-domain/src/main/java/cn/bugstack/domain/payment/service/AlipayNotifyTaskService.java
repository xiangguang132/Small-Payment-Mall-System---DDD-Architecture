package cn.bugstack.domain.payment.service;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuySettlementCommandEntity;
import cn.bugstack.domain.groupbuy.service.settlement.IGroupBuySettlementService;
import cn.bugstack.domain.order.adapter.repository.IOrderRepository;
import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.order.service.IOrderService;
import cn.bugstack.domain.payment.adapter.port.IAlipayPort;
import cn.bugstack.domain.payment.model.entity.AlipayNotifyTaskEntity;
import cn.bugstack.domain.payment.adapter.repository.IAlipayNotifyTaskRepository;
import cn.bugstack.types.enums.OrderTypeEnum;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
public class AlipayNotifyTaskService implements IAlipayNotifyTaskService {

    private static final int MAX_RETRY_COUNT = 5;
    private static final String LOCK_KEY_PREFIX = "alipay_notify_task_lock:";

    @Resource
    private IAlipayNotifyTaskRepository alipayNotifyTaskRepository;
    @Resource
    private IOrderRepository orderRepository;
    @Resource
    private IGroupBuySettlementService groupBuySettlementService;
    @Resource
    private IOrderService orderService;
    @Resource
    private IAlipayPort alipayPort;

    /**
     * 保存回调任务
     *   1. 按 out_trade_no + trade_no 查询任务。
     *   2. 已存在且 task_status = 1 时直接返回，不做重复处理。
     *   3. 不存在则插入 task_status = 0 的待处理任务。
     *   4. 数据库唯一键 (out_trade_no, trade_no) 冲突时按“已接收”处理。
     *   5. 订单更新、拼团结算这些重业务逻辑放到任务处理服务里，不要在 payNotify 里做。
     * @param params
     */
    @Override
    public void saveNotifyTask(Map<String, String> params) {
        String outTradeNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");

        // 查询是否存在
        AlipayNotifyTaskEntity isExisting = alipayNotifyTaskRepository.queryByOutTradeNoAndTradeNo(outTradeNo, tradeNo);

        if (isExisting != null) {
            log.info("支付通知任务已存在 outTradeNo:{} tradeNo:{}", outTradeNo, tradeNo);
            return ;
        }

        // 不存在的话就是保存入库
        AlipayNotifyTaskEntity entity = AlipayNotifyTaskEntity.builder()
                .outTradeNo(outTradeNo)
                .tradeNo(tradeNo)
                .taskStatus(0)   // 默认 0=待处理
                .retryCount(0)
                .parameterJson(JSON.toJSONString(params))
                .build();
        alipayNotifyTaskRepository.save(entity);
    }

    /**
     * 消费 与 处理 回调任务
     * 整体的流程 ---
     * IAlipayNotifyTaskRepository 处理回调任务，但是没有订单类型
     * IOrderRepository 订单信息，可以获取订单状态
     * @param outTradeNo
     */
    /*
    也就是说流程其实是：校验参数合法-加锁-获取支付宝回调任务-判断幂等-查询订单（为了获取订单类型）-执行任务（依据订单类型）-更新任务状态-释放锁？


• 对，你总结的流程是对的。

  1. 校验参数合法
  2. 加锁
  3. 获取支付宝回调任务
  4. 判断幂等
  5. 查询订单，获取订单类型
  6. 依据订单类型执行业务
  7. 更新任务状态
  8. 释放锁

  其中第 7 步根据结果分三种：

  - 业务成功 -> updateTaskSuccess
  - 业务异常 -> updateTaskRetry，然后抛出异常让 MQ 重试
  - 重试次数达到上限 -> updateTaskDead

  第 8 步 finally 释放锁，无论成功还是异常都会执行。

  所以更准确一点是：

  校验参数
  -> 加锁
  -> 查任务
  -> 幂等判断
  -> 查订单
  -> 按订单类型执行
  -> 成功更新状态 / 失败更新重试状态
  -> 释放锁
     */
    @Override
    public void processTask(String outTradeNo) {
        if (outTradeNo == null) {
            log.info("支付通知任务不能为空 outTradeNo:{}", outTradeNo);
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "支付通知任务不能为空");
        }

        if(!alipayPort.tryLock(outTradeNo)) {
            log.warn("获取支付宝通知处理锁失败 outTradeNo:{}", outTradeNo);
            return;
        }

        try {
            AlipayNotifyTaskEntity alipayNotifyTaskEntity = alipayNotifyTaskRepository.queryByOutTradeNo(outTradeNo);
            // 判断
            if (alipayNotifyTaskEntity == null) {
                log.warn("支付宝通知任务不存在 outTradeNo:{}", outTradeNo);
            }
            if (Integer.valueOf(1).equals(alipayNotifyTaskEntity.getTaskStatus())) {
                log.info("支付宝通知任务已处理完成 outTradeNo:{}", outTradeNo);
            }
            if (alipayNotifyTaskEntity.getRetryCount() != null && alipayNotifyTaskEntity.getRetryCount() >= MAX_RETRY_COUNT) {
                alipayNotifyTaskRepository.updatedAlipayNotifyTaskFailed(outTradeNo);
            }
            PayOrderEntity payOrderEntity = orderRepository.queryPayOrderByOutTradeNo(outTradeNo);
            if (payOrderEntity == null) {
                throw new AppException(ResponseCode.UN_ERROR, "支付单不存在： outTradeNo" + outTradeNo);
            }
            JSONObject params = JSON.parseObject(alipayNotifyTaskEntity.getParameterJson());
            LocalDateTime payTime = parseAlipayTime(params == null ? null : params.getString("gmt_payment"));

            if (OrderTypeEnum.GROUP_BUY.equals(payOrderEntity.getOrderType())) {
                GroupBuySettlementCommandEntity groupBuySettlementCommandEntity = GroupBuySettlementCommandEntity.builder()
                        .userId(payOrderEntity.getUserId())
                        .outTradeNo(outTradeNo)
                        .payTime(payTime)
                        .source("ALIPAY")
                        .channel("ALIPAY")
                        .build();
                groupBuySettlementService.settlementGroupBuyOrder(groupBuySettlementCommandEntity);
            } else {
                Date outTradeTime = payTime == null
                        ? null
                        : Date.from(payTime.atZone(ZoneId.systemDefault()).toInstant());
                orderService.changeOrderPaySuccess(outTradeNo, outTradeTime);
                alipayNotifyTaskRepository.updatedAlipayNotifyTaskSuccess(outTradeNo);
                log.info("支付宝通知任务处理完成 outTradeNo:{}", outTradeNo);
            }
        } catch (Exception e) {
            log.error("支付宝通知任务处理失败 outTradeNo:{}", outTradeNo, e);
            alipayNotifyTaskRepository.updatedAlipayNotifyTaskRetry(outTradeNo);
            throw new AppException(ResponseCode.UN_ERROR, "支付宝通知任务处理失败");
        } finally {
            alipayPort.unlock(outTradeNo);
        }
    }

    private LocalDateTime parseAlipayTime(String alipayTime) {
        if (StringUtils.isBlank(alipayTime)) {
            return null;
        }
        try {
            return LocalDateTime.parse(alipayTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            log.warn("支付宝支付时间解析失败 alipayTime:{}", alipayTime, e);
            return null;
        }
    }

}
