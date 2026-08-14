package cn.bugstack.domain.payment.service;

import cn.bugstack.domain.payment.model.entity.AlipayNotifyTaskEntity;
import cn.bugstack.domain.payment.repository.IAlipayNotifyTaskRepository;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@Service
public class AlipayNotifyTaskService implements IAlipayNotifyTaskService {

    @Resource
    private IAlipayNotifyTaskRepository repository;

    /**
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
        AlipayNotifyTaskEntity isExisting = repository.queryByOutTradeNoAndTradeNo(outTradeNo, tradeNo);

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
        repository.save(entity);
    }
}
