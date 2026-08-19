# RabbitMQ 支付宝异步回调规则链演进

> 本文是后续演进方案，当前不落地。先完成直连版 `processTask`，等普通订单、拼团订单、发货、库存、会员等业务节点开始变多时，再按本文抽取规则链。

## 1. 演进目标

把 `AlipayNotifyTaskService.processTask` 里的任务状态校验、订单类型分叉、业务处理抽成可扩展规则链。

```text
processTask
  -> 获取分布式锁
  -> 构建 command / dynamicContext
  -> alipayNotifyProcessRuleFilter.apply(command, context)
  -> finally 释放分布式锁
```

锁和异常重试留在 `processTask` 外层，不进入规则链。

## 2. 复用现有模式

项目里已经有现成模板：

- `BusinessLinkedList`：业务链，按顺序执行节点
- `ILogicHandler`：规则节点接口
- `LinkArmory`：链装配工具
- `GroupBuySettlementRuleFilterFactory`：工厂模式示例

规则链语义：

- 节点返回 `null`，继续执行下一个节点
- 节点返回非 `null`，规则链停止

## 3. 建议包结构

```text
domain/payment/model/entity/AlipayNotifyProcessCommandEntity.java
domain/payment/model/entity/AlipayNotifyProcessFeedBackEntity.java
domain/payment/service/rule/factory/AlipayNotifyProcessRuleFilterFactory.java
domain/payment/service/rule/filter/TaskExistsRuleFilter.java
domain/payment/service/rule/filter/TaskStatusRuleFilter.java
domain/payment/service/rule/filter/DirectOrderRuleFilter.java
domain/payment/service/rule/filter/GroupBuyOrderRuleFilter.java
```

## 4. Command / Feedback / Context

```java
package cn.bugstack.domain.payment.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlipayNotifyProcessCommandEntity {

    private String outTradeNo;
}
```

```java
package cn.bugstack.domain.payment.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlipayNotifyProcessFeedBackEntity {

    private String outTradeNo;
    private boolean handled;
}
```

```java
package cn.bugstack.domain.payment.service.rule.factory;

import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.payment.model.entity.AlipayNotifyTaskEntity;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlipayNotifyProcessRuleFilterFactory {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DynamicContext {

        private AlipayNotifyTaskEntity task;
        private PayOrderEntity payOrder;
        private JSONObject params;
    }
}
```

## 5. 规则链装配

```java
package cn.bugstack.domain.payment.service.rule.factory;

import cn.bugstack.domain.payment.model.entity.AlipayNotifyProcessCommandEntity;
import cn.bugstack.domain.payment.model.entity.AlipayNotifyProcessFeedBackEntity;
import cn.bugstack.domain.payment.service.rule.filter.DirectOrderRuleFilter;
import cn.bugstack.domain.payment.service.rule.filter.GroupBuyOrderRuleFilter;
import cn.bugstack.domain.payment.service.rule.filter.TaskExistsRuleFilter;
import cn.bugstack.domain.payment.service.rule.filter.TaskStatusRuleFilter;
import cn.bugstack.types.design.framework.link.multilink.LinkArmory;
import cn.bugstack.types.design.framework.link.multilink.chain.BusinessLinkedList;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class AlipayNotifyProcessRuleFilterFactory {

    @Bean("alipayNotifyProcessRuleFilter")
    public BusinessLinkedList<
            AlipayNotifyProcessCommandEntity,
            AlipayNotifyProcessRuleFilterFactory.DynamicContext,
            AlipayNotifyProcessFeedBackEntity> alipayNotifyProcessRuleFilter(
            TaskExistsRuleFilter taskExistsRuleFilter,
            TaskStatusRuleFilter taskStatusRuleFilter,
            DirectOrderRuleFilter directOrderRuleFilter,
            GroupBuyOrderRuleFilter groupBuyOrderRuleFilter) {
        return new LinkArmory<>(
                "支付宝通知处理规则链",
                taskExistsRuleFilter,
                taskStatusRuleFilter,
                directOrderRuleFilter,
                groupBuyOrderRuleFilter
        ).getLogicLink();
    }
}
```

## 6. AlipayNotifyTaskService 改造

```java
@Resource(name = "alipayNotifyProcessRuleFilter")
private BusinessLinkedList<
        AlipayNotifyProcessCommandEntity,
        AlipayNotifyProcessRuleFilterFactory.DynamicContext,
        AlipayNotifyProcessFeedBackEntity> alipayNotifyProcessRuleFilter;

@Override
public void processTask(String outTradeNo) {
    if (StringUtils.isBlank(outTradeNo)) {
        throw new IllegalArgumentException("outTradeNo不能为空");
    }

    if (!alipayNotifyPort.tryLock(outTradeNo)) {
        log.warn("获取支付宝通知处理锁失败 outTradeNo:{}", outTradeNo);
        return;
    }

    try {
        AlipayNotifyProcessRuleFilterFactory.DynamicContext context =
                AlipayNotifyProcessRuleFilterFactory.DynamicContext.builder()
                        .task(repository.queryByOutTradeNo(outTradeNo))
                        .build();

        alipayNotifyProcessRuleFilter.apply(
                AlipayNotifyProcessCommandEntity.builder()
                        .outTradeNo(outTradeNo)
                        .build(),
                context);
    } catch (Exception e) {
        log.error("支付宝通知任务处理失败 outTradeNo:{}", outTradeNo, e);
        repository.updateTaskRetry(outTradeNo);
        throw new RuntimeException(e);
    } finally {
        alipayNotifyPort.unlock(outTradeNo);
    }
}
```

状态更新放在规则链节点里：

- 任务已成功：不更新，直接返回
- 超过最大重试次数：`updateTaskDead`
- 业务处理成功：`updateTaskSuccess`
- 业务处理异常：由 `processTask` 外层 `updateTaskRetry`

## 7. 规则节点示例

### 7.1 TaskExistsRuleFilter

```java
@Override
public AlipayNotifyProcessFeedBackEntity apply(
        AlipayNotifyProcessCommandEntity command,
        AlipayNotifyProcessRuleFilterFactory.DynamicContext context) {
    if (context.getTask() == null) {
        throw new AppException(ResponseCode.UN_ERROR, "支付宝通知任务不存在");
    }
    return next(command, context);
}
```

### 7.2 TaskStatusRuleFilter

```java
@Override
public AlipayNotifyProcessFeedBackEntity apply(
        AlipayNotifyProcessCommandEntity command,
        AlipayNotifyProcessRuleFilterFactory.DynamicContext context) throws Exception {
    AlipayNotifyTaskEntity task = context.getTask();
    if (Integer.valueOf(1).equals(task.getTaskStatus())) {
        return AlipayNotifyProcessFeedBackEntity.builder()
                .outTradeNo(command.getOutTradeNo())
                .handled(true)
                .build();
    }
    if (task.getRetryCount() != null && task.getRetryCount() >= MAX_RETRY_COUNT) {
        repository.updateTaskDead(command.getOutTradeNo());
        return AlipayNotifyProcessFeedBackEntity.builder()
                .outTradeNo(command.getOutTradeNo())
                .handled(true)
                .build();
    }
    return next(command, context);
}
```

### 7.3 DirectOrderRuleFilter

```java
@Override
public AlipayNotifyProcessFeedBackEntity apply(
        AlipayNotifyProcessCommandEntity command,
        AlipayNotifyProcessRuleFilterFactory.DynamicContext context) throws Exception {
    PayOrderEntity payOrder = context.getPayOrder();
    if (OrderTypeEnum.DIRECT.equals(payOrder.getOrderType())) {
        orderService.changeOrderPaySuccess(command.getOutTradeNo(), outTradeTime);
        repository.updateTaskSuccess(command.getOutTradeNo());
        return AlipayNotifyProcessFeedBackEntity.builder()
                .outTradeNo(command.getOutTradeNo())
                .handled(true)
                .build();
    }
    return next(command, context);
}
```

### 7.4 GroupBuyOrderRuleFilter

```java
@Override
public AlipayNotifyProcessFeedBackEntity apply(
        AlipayNotifyProcessCommandEntity command,
        AlipayNotifyProcessRuleFilterFactory.DynamicContext context) throws Exception {
    PayOrderEntity payOrder = context.getPayOrder();
    if (OrderTypeEnum.GROUP_BUY.equals(payOrder.getOrderType())) {
        groupBuySettlementService.settlementGroupBuyOrder(command);
        repository.updateTaskSuccess(command.getOutTradeNo());
        return AlipayNotifyProcessFeedBackEntity.builder()
                .outTradeNo(command.getOutTradeNo())
                .handled(true)
                .build();
    }
    throw new AppException(ResponseCode.UN_ERROR, "不支持的订单类型");
}
```

## 8. 建议迁移顺序

1. 先完成直连版 `processTask` 和任务状态机。
2. 保持 `IAlipayNotifyPort` 不变，锁继续放在 `processTask` 外层。
3. 当业务分支超过两个，或需要增加“库存扣减”“发货”“会员开通”等节点时，再抽取规则链。
4. 先抽 command、feedback、factory 和 context，再逐节点迁移。
5. 每个节点尽量只做一件事，状态更新和业务处理不混在同一个方法里。

## 9. 注意事项

- 规则链适合“节点多、顺序稳定、需要扩展”的场景。
- 如果只有普通订单和拼团订单两个分支，策略模式或简单 `if/else` 更轻。
- 不要为了规则链把 `processTask` 的可读性牺牲掉。
- 异常必须继续抛给 MQ，否则无法触发重试。
