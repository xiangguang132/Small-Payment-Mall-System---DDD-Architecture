package cn.bugstack.domain.payment.service;

import cn.bugstack.domain.payment.model.entity.AlipayNotifyProcessCommandEntity;
import cn.bugstack.domain.payment.model.entity.AlipayNotifyProcessFeedBackEntity;
import cn.bugstack.domain.payment.model.entity.AlipayNotifyTaskEntity;
import cn.bugstack.domain.payment.adapter.port.IAlipayPort;
import cn.bugstack.domain.payment.adapter.repository.IAlipayNotifyTaskRepository;
import cn.bugstack.domain.payment.service.rule.factory.AlipayNotifyProcessRuleFilterFactory;
import cn.bugstack.types.design.framework.link.multilink.chain.BusinessLinkedList;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@Service
public class AlipayNotifyTaskService implements IAlipayNotifyTaskService {

    private static final int MAX_RETRY_COUNT = 5;
    private static final String LOCK_KEY_PREFIX = "alipay_notify_task_lock:";

    @Resource
    private IAlipayNotifyTaskRepository alipayNotifyTaskRepository;
    @Resource
    private IAlipayPort alipayPort;
    @Resource(name = "alipayNotifyProcessRuleFilter")
    private BusinessLinkedList<
            AlipayNotifyProcessCommandEntity,
            AlipayNotifyProcessRuleFilterFactory.DynamicContext,
            AlipayNotifyProcessFeedBackEntity> alipayNotifyProcessRuleFilter;

    /**
     * 保存回调任务
     *   1. 按 out_trade_no + trade_no 查询任务。
     *   2. 已存在且 task_status = 1 时直接返回，不做重复处理。
     *   3. 不存在则插入 task_status = 0 的待处理任务。
     *   4. 数据库唯一键 (out_trade_no, trade_no) 冲突时按"已接收"处理。
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
     * 消费与处理回调任务
     * 流程：参数校验 → 加锁 → 规则链处理 → catch异常更新重试 → finally释放锁
     * 规则链节点：TaskExists → TaskStatus → DirectOrder → GroupBuyOrder
     */
    @Override
    public void processTask(String outTradeNo) {
        if (outTradeNo == null) {
            log.info("支付通知任务不能为空 outTradeNo:{}", outTradeNo);
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "支付通知任务不能为空");
        }

        if (!alipayPort.tryLock(outTradeNo)) {
            log.warn("获取支付宝通知处理锁失败 outTradeNo:{}", outTradeNo);
            return;
        }

        try {
            alipayNotifyProcessRuleFilter.apply(
                    AlipayNotifyProcessCommandEntity.builder()
                            .outTradeNo(outTradeNo)
                            .build(),
                    AlipayNotifyProcessRuleFilterFactory.DynamicContext.builder()
                            .build()
            );
        } catch (Exception e) {
            log.error("支付宝通知任务处理失败 outTradeNo:{}", outTradeNo, e);
            AlipayNotifyTaskEntity task = alipayNotifyTaskRepository.queryByOutTradeNo(outTradeNo);
            int retryCount = task == null ? 0 : (task.getRetryCount() == null ? 0 : task.getRetryCount());
            if (retryCount >= MAX_RETRY_COUNT) {
                alipayNotifyTaskRepository.updatedAlipayNotifyTaskFailed(outTradeNo);
            } else {
                alipayNotifyTaskRepository.updatedAlipayNotifyTaskRetry(outTradeNo);
            }
            throw new AppException(ResponseCode.UN_ERROR, "支付宝通知任务处理失败");
        } finally {
            alipayPort.unlock(outTradeNo);
        }
    }

}
