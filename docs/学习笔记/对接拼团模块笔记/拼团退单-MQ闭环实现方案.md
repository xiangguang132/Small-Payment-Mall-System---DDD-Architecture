# 拼团退单 MQ 闭环实现方案（对标 group-buy-market）

> 整理日期：2026-08-24
> 对象项目：`D:\code\s-pay-mall-ddd-cc`（本地）
> 参考项目：`E:\group-buy-market`
> 目标：把退单从「纯同步收尾」改成「落库 → 本地消息表 `group_buy_notify_task` → MQ 异步（`topic.team_refund`）→ 消费端恢复团队锁单量」的异步最终一致性模型。

---

## 0. 当前现状

本地退单链路是**纯同步**的：`GroupBuyRefundPort.groupBuyRefundNotify` 一个方法内依次做完

1. 支付宝退款（已支付才走）
2. `updateOrderStatus2Refund` 订单状态置已退款
3. `updateSubtractLockCount` **同步**扣团队 lock_count
4. 写 `group_buy_notify_task` 发 MQ —— **被注释成 TODO**，没落库也没发

消费端 `RefundSuccessTopicListener` 绑定了 `topic_team_refund` 队列，但方法体只有一行日志，是空壳。

---

## 1. 目标链路

```
触发退单（超时 Job / HTTP 接口）
  → GroupBuyRefundOrderService.refundGroupBuyOrder
      → 退单责任链（Data → Unique → RefundOrder）选策略 strategyName
      → 策略.refundGroupBuyOrder
          → GroupBuyRefundPort.groupBuyRefundNotify         [① 同步部分]
              ② 支付宝退款（已支付）
              ③ 订单状态置已退款
              ④ 落库 group_buy_notify_task（notifyStatus=0, notifyMQ=topic.team_refund）
              ⑤ 异步 execNotifyJob(task) 发 MQ               [② 消息表补偿，兜底靠切面 Job]
          →   （原同步 updateSubtractLockCount 删除，移到消费端）
  → MQ topic.team_refund 消费 RefundSuccessTopicListener
      → GroupBuyRefundOrderService.restoreTeamLockStock      [③ 消费端恢复库存]
          → 按 refundType 取策略 → 策略.reverseStock
          → groupBuyTeamRepository.updateSubtractLockCount(teamId)   （已成团策略空实现）
```

这是参考项目的最终一致性模型（参考 `RefundSuccessTopicListener` 注释）：
1. DB 退单 + 本地消息表落库，即使 MQ 发送失败，`GroupBuyNotifyJob` 每分钟兜底扫 `notify_status in (0,2)` 补偿，保证消息一定会发出去。
2. MQ 消费后恢复锁单量，抛异常触发 MQ 重试。
3. `install` 幂等：本地 `updateSubtractLockCount` 自带 `lock_count > 0` 条件兜底；参考靠 Redis 分布式锁。

---

## 2. 本地已具备的基础（无需重写，直接复用）

| 组件 | 本地文件 | 状态 |
|---|---|---|
| RabbitMQ producer | `infrastructure/event/EventPublisher.java`（RabbitTemplate） | ✅ |
| topic 配置 `topic_team_refund`（routing_key=`topic.team_refund`，queue=`s_pay_mall_queue_team_refund`）| `application-dev.yml` / `application-test.yml` | ✅ |
| 本地消息表 DAO | `IGroupBuyNotifyTaskDao`（insert / queryUnExecuted / 状态更新）| ✅ |
| 本地消息表 PO | `dao/po/groupbuy/GroupBuyNotifyTask`（含 notifyMq）| ✅ |
| 本地消息表仓储 | `IGroupBuyNotifyTaskRepository` + `GroupBuyNotifyTaskRepository`（**暂无 insert**）| ⚠️ 缺 insert |
| 消息表 Service | `GroupBuyNotifyTaskService`：`execNotifyJob()` + `execNotifyJob(notifyTask)` | ✅ 两个重载都在 |
| 兜底 Job | `trigger/job/GroupBuyNotifyJob`（每分钟扫 `notify_status in (0,2)`）| ✅ |
| 发 MQ 端口 | `infrastructure/adapter/port/GroupBuyPort` → `eventPublisher.publish(notifyMQ, parameterJson)` | ✅ |
| 消费绑定 | `trigger/listener/RefundSuccessTopicListener`（已绑 `topic_team_refund`）| ⚠️ 空壳 |

> 结论：只缺「接线」——落库 insert、发 MQ 触发、消费端恢复、以及同步恢复的移除。全是小改动，不重写。

---

## 3. 关键决策点（先看懂再动手）

1. **双扣隐患** —— 现在 `GroupBuyRefundPort` 同步 `updateSubtractLockCount` 已经把库存恢复了；如果 MQ 消费端再恢复一次，就双扣。所以**恢复职责必须整体从 port 移到消费端**，port 里那行同步更新要删。
2. **topic 常量** —— `GroupBuyRefundPort` 注释里写的 `topic.order_refund_success` 是**错的**，和消费者绑定的 `topic.team_refund`（yml routing_key）对不上。必须用 `topic.team_refund`。
3. **已成团不恢复锁单量** —— 对标参考 `PaidTeam2RefundStrategy.reverseStock` 空实现：已成团队伍已结束，恢复没有意义，只恢复「已支付未成团」「未支付未成团」两种。
4. **落库位置** —— 参考在仓储 `paid2Refund` 里（和 DB 退款同事务）落 notify_task；本地沿用现有结构，就在 `GroupBuyRefundPort` 里落，与 DB 更新保持同一次调用即可。建议给 port 的 DB 段加 `@Transactional`，避免「退款已到账但消息表没落」。

---

## 4. 分步改动（按依赖顺序）

### 第 1 步：行为实体补 `refundType`（让 MQ 消息能带回策略类型）

`domain/groupbuy/model/entity/GroupBuyRefundOrderBehaviorEntity.java` 增加字段：

```java
/**
 * 退单类型（恢复库存时分派策略用，值 = 策略 bean 名）
 */
private String refundType;
```

### 第 2 步：策略基类把 refundType 透传给 port + 补 reverseStock 骨架

`domain/groupbuy/service/refund/business/AbstractGroupBuyRefundOrderStrategy.java`：
- 重载带 `refundType` 的 `sendRefundNotifyMessage`，构建行为实体时填上 `refundType`
- 新增 `doReverseStock(...)`（对标参考 `AbstractRefundOrderStrategy.doReverseStock`）

```java
@Slf4j
public abstract class AbstractGroupBuyRefundOrderStrategy implements IGroupBuyRefundOrderStrategy {

    @Resource
    protected IGroupBuyRefundPort groupBuyRefundPort;

    @Resource
    protected IGroupBuyTeamRepository groupBuyTeamRepository;

    // ...原有 3 个 sendRefundNotifyMessage 重载保留，追加带 refundType 的版本...

    /**
     * 统一退单后回调钩子（带退单类型，供 MQ 消息记录 type）。
     */
    protected void sendRefundNotifyMessage(GroupBuyRefundOrderEntity refundOrderEntity,
                                           String refundType, boolean success,
                                           String message, BigDecimal payAmount) {
        GroupBuyRefundOrderBehaviorEntity behaviorEntity = GroupBuyRefundOrderBehaviorEntity.builder()
                .userId(refundOrderEntity.getUserId())
                .teamId(refundOrderEntity.getTeamId())
                .activityId(refundOrderEntity.getActivityId())
                .orderId(refundOrderEntity.getOrderId())
                .outTradeNo(refundOrderEntity.getOutTradeNo())
                .payAmount(payAmount)
                .refundType(refundType)
                .success(success)
                .message(message)
                .build();
        sendRefundNotifyMessage(behaviorEntity);
    }

    /**
     * 消费端恢复锁单量（对标参考 doReverseStock；本地无 Redis，直接扣 DB team.lock_count）。
     */
    protected void doReverseStock(GroupBuyRefundRestoreEntity restoreEntity) {
        log.info("退单；恢复锁单量 activityId:{} teamId:{}",
                restoreEntity.getActivityId(), restoreEntity.getTeamId());
        int updated = groupBuyTeamRepository.updateSubtractLockCount(restoreEntity.getTeamId());
        if (updated != 1) {
            log.warn("退单；恢复锁单量失败或已恢复 teamId:{}", restoreEntity.getTeamId());
        }
    }
}
```

### 第 3 步：策略接口加 `reverseStock`，三个策略实现

`domain/groupbuy/service/refund/business/IGroupBuyRefundOrderStrategy.java` 增加：

```java
/**
 * 恢复退单占用的锁单量（由 MQ 消费端调用）
 */
void reverseStock(GroupBuyRefundRestoreEntity restoreEntity) throws Exception;
```

三个实现（对照参考的 `paid2Refund` / `unpaid2Refund` / `paidTeam2Refund`）：

```java
// paidRefundStrategy —— 已支付未成团：要恢复锁单量
@Service("paidRefundStrategy")
public class PaidRefundStrategy extends AbstractGroupBuyRefundOrderStrategy {
    @Override
    public void refundGroupBuyOrder(GroupBuyRefundOrderEntity e) {
        log.info("退单：已支付未成团 userId:{} teamId:{} outTradeNo:{}", e.getUserId(), e.getTeamId(), e.getOutTradeNo());
        sendRefundNotifyMessage(e, "paidRefundStrategy", true, "已支付未成团退单成功", e.getPayAmount());
    }
    @Override
    public void reverseStock(GroupBuyRefundRestoreEntity restoreEntity) {
        doReverseStock(restoreEntity);   // 未成团，要恢复锁单量
    }
}

// unpaidNotTeamRefundStrategy —— 未支付未成团：要恢复锁单量
@Service("unpaidNotTeamRefundStrategy")
public class UnpaidNotTeamRefundStrategy extends AbstractGroupBuyRefundOrderStrategy {
    @Override
    public void refundGroupBuyOrder(GroupBuyRefundOrderEntity e) {
        log.info("退单：未支付未成团 userId:{} teamId:{} outTradeNo:{}", e.getUserId(), e.getTeamId(), e.getOutTradeNo());
        sendRefundNotifyMessage(e, "unpaidNotTeamRefundStrategy", true, "未支付退单成功");   // payAmount 不传
    }
    @Override
    public void reverseStock(GroupBuyRefundRestoreEntity restoreEntity) {
        doReverseStock(restoreEntity);   // 锁单过，要恢复
    }
}

// paidTeamRefundStrategy —— 已支付已成团：不恢复锁单量（对标参考空实现）
@Service("paidTeamRefundStrategy")
public class PaidTeamRefundStrategy extends AbstractGroupBuyRefundOrderStrategy {
    @Override
    public void refundGroupBuyOrder(GroupBuyRefundOrderEntity e) {
        log.info("退单：已支付已成团 userId:{} teamId:{} outTradeNo:{}", e.getUserId(), e.getTeamId(), e.getOutTradeNo());
        sendRefundNotifyMessage(e, "paidTeamRefundStrategy", true, "已支付已成团退单成功", e.getPayAmount());
    }
    @Override
    public void reverseStock(GroupBuyRefundRestoreEntity restoreEntity) {
        log.info("退单；已支付已成团，队伍组队结束，不需要恢复锁单量 teamId:{}", restoreEntity.getTeamId());
    }
}
```

> 新增引用的 `GroupBuyRefundRestoreEntity`（对标参考 `TeamRefundSuccess`，MQ 消息体）放 `domain/groupbuy/model/entity/`：

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyRefundRestoreEntity {
    private String refundType;    // 策略 bean 名：paidRefundStrategy / unpaidNotTeamRefundStrategy / paidTeamRefundStrategy
    private String userId;
    private String teamId;
    private Long activityId;
    private String orderId;
    private String outTradeNo;
}
```

### 第 4 步：仓储补 insert

`domain/groupbuy/repository/IGroupBuyNotifyTaskRepository.java` 增加：

```java
int insertNotifyTask(GroupBuyNotifyTaskEntity task);
```

`infrastructure/repository/GroupBuyNotifyTaskRepository.java` 实现（包一层 DAO）：

```java
@Override
public int insertNotifyTask(GroupBuyNotifyTaskEntity task) {
    return groupBuyNotifyTaskDao.insert(toPo(task));
}

private GroupBuyNotifyTask toPo(GroupBuyNotifyTaskEntity entity) {
    return GroupBuyNotifyTask.builder()
            .teamId(entity.getTeamId())
            .activityId(entity.getActivityId())
            .notifyMq(entity.getNotifyMQ())
            .notifyStatus(entity.getNotifyStatus())
            .notifyCount(entity.getNotifyCount())
            .parameterJson(entity.getParameterJson())
            .build();
}
```

> 现成的 `toPo` 已够用；`notifyType` / `uuid` 本地 PO 没有，不需要。

### 第 5 步：`GroupBuyRefundPort` 完成落库 + 发 MQ + 移除同步恢复

`infrastructure/adapter/port/GroupBuyRefundPort.java`：

```java
@Slf4j
@Component
public class GroupBuyRefundPort implements IGroupBuyRefundPort {

    @Resource
    private IAlipayRefundPort alipayRefundPort;
    @Resource
    private IGroupBuyOrderRepository groupBuyOrderRepository;
    // @Resource private IGroupBuyTeamRepository groupBuyTeamRepository;   // ← 删除：恢复职责移到消费端
    @Resource
    private IGroupBuyNotifyTaskRepository groupBuyNotifyTaskRepository;
    @Resource
    private IGroupBuyNotifyTaskService groupBuyNotifyTaskService;

    @Override
    @Transactional
    public void groupBuyRefundNotify(GroupBuyRefundOrderBehaviorEntity behaviorEntity) throws Exception {
        String outTradeNo = behaviorEntity.getOutTradeNo();
        String teamId = behaviorEntity.getTeamId();
        boolean success = behaviorEntity.isSuccess();

        log.info("拼团退单回调 userId:{} teamId:{} outTradeNo:{} success:{} message:{}",
                behaviorEntity.getUserId(), teamId, outTradeNo, success, behaviorEntity.getMessage());

        if (!success) {
            log.warn("拼团退单失败，跳过后续处理 outTradeNo:{} message:{}", outTradeNo, behaviorEntity.getMessage());
            return;
        }

        // 1. 已支付订单：调用支付宝退款
        if (behaviorEntity.getPayAmount() != null) {
            boolean alipayRefundSuccess = alipayRefundPort.refund(outTradeNo, null, behaviorEntity.getPayAmount());
            if (!alipayRefundSuccess) {
                log.error("拼团退单支付宝退款失败 outTradeNo:{}", outTradeNo);
                return;   // 退款失败，不落消息表，等待重试
            }
            log.info("拼团退单支付宝退款成功 outTradeNo:{}", outTradeNo);
        }

        // 2. 更新拼团订单状态为已退款（status=2）
        int orderUpdated = groupBuyOrderRepository.updateOrderStatus2Refund(outTradeNo);
        if (orderUpdated != 1) {
            log.warn("拼团退单更新订单状态失败（可能已处理） outTradeNo:{}", outTradeNo);
        }

        // 3. 落库本地消息表（notifyStatus=0，等待发 MQ；兜底由 GroupBuyNotifyJob 扫）
        String parameterJson = JSON.toJSONString(new HashMap<String, Object>() {{
            put("refundType", behaviorEntity.getRefundType());
            put("userId", behaviorEntity.getUserId());
            put("teamId", teamId);
            put("orderId", behaviorEntity.getOrderId());
            put("outTradeNo", outTradeNo);
            put("activityId", behaviorEntity.getActivityId());
        }});
        GroupBuyNotifyTaskEntity task = GroupBuyNotifyTaskEntity.builder()
                .teamId(teamId)
                .activityId(behaviorEntity.getActivityId())
                .notifyType("MQ")
                .notifyMQ("topic.team_refund")          // 必须用 topic.team_refund，和消费者绑定一致
                .notifyStatus(0)
                .notifyCount(0)
                .parameterJson(parameterJson)
                .build();
        int inserted = groupBuyNotifyTaskRepository.insertNotifyTask(task);
        if (inserted != 1) {
            log.warn("拼团退单写本地消息表失败 teamId:{}", teamId);
        }

        // 4. 异步发 MQ（失败也由 GroupBuyNotifyJob 兜底重发）
        groupBuyNotifyTaskService.execNotifyJob(task);
    }
}
```

> 第 4 步直接调 `execNotifyJob(task)` 是立即发（对标参考线程池异步）；不调也只晚 1 分钟由 `GroupBuyNotifyJob` 扫到。二者留其一即可，强烈建议都留（立即发 + 表里 status=0 兜底）。

### 第 6 步：service 加 `restoreTeamLockStock` 消费入口

`domain/groupbuy/service/refund/IGroupBuyRefundOrderService.java`：

```java
/**
 * 恢复锁单量（MQ 消费端调用）
 */
void restoreTeamLockStock(GroupBuyRefundRestoreEntity restoreEntity) throws Exception;
```

`domain/groupbuy/service/refund/GroupBuyRefundOrderService.java` 实现：

```java
@Override
public void restoreTeamLockStock(GroupBuyRefundRestoreEntity restoreEntity) throws Exception {
    log.info("逆向流程，恢复锁单量 userId:{} activityId:{} teamId:{} refundType:{}",
            restoreEntity.getUserId(), restoreEntity.getActivityId(), restoreEntity.getTeamId(), restoreEntity.getRefundType());
    IGroupBuyRefundOrderStrategy strategy = refundGroupBuyOrderStrategyMap.get(restoreEntity.getRefundType());
    if (strategy == null) {
        log.warn("未找到恢复锁单量策略 refundType:{}", restoreEntity.getRefundType());
        throw new AppException("未找到恢复锁单量策略");
    }
    strategy.reverseStock(restoreEntity);
}
```

> `refundGroupBuyOrderStrategyMap` 已在 service 里注入，直接复用。

### 第 7 步：`RefundSuccessTopicListener` 填实

`trigger/listener/RefundSuccessTopicListener.java`（沿用「listener 只见 domain 接口」约定，见《成团通知-Listener改Service方式决策》）：

```java
@Slf4j
@Component
public class RefundSuccessTopicListener {

    @Resource
    private IGroupBuyRefundOrderService groupBuyRefundOrderService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "${spring.rabbitmq.config.producer.topic_team_refund.queue}"),
                    exchange = @Exchange(value = "${spring.rabbitmq.config.producer.exchange}", type = ExchangeTypes.TOPIC),
                    key = "${spring.rabbitmq.config.producer.topic_team_refund.routing_key}"
            )
    )
    public void listener(String message) {
        log.info("接收消息（退单成功）- 恢复拼团队伍锁单量:{}", message);
        GroupBuyRefundRestoreEntity restoreEntity = JSON.parseObject(message, GroupBuyRefundRestoreEntity.class);
        try {
            groupBuyRefundOrderService.restoreTeamLockStock(restoreEntity);
        } catch (Exception e) {
            log.error("接收消息（退单成功）- 恢复拼团队伍锁单量失败: message:{}", message, e);
            throw new RuntimeException(e);   // 抛异常，MQ 重试
        }
    }
}
```

---

## 5. 幂等与并发注意

1. **本地消息表补偿**：即使异步发 MQ 失败，`group_buy_notify_task` 里 `notify_status=0` 的记录会被 `GroupBuyNotifyJob` 每分钟补偿；`execNotifyJob` 内部已做「成功→status=1 / 重试→count+1,status=2 / 超 4 次→status=3」。
2. **消费端重复消费**：参考用 Redis 分布式锁（`notifyTask.lockKey()`）；本地 `updateSubtractLockCount` 自带 `lock_count > 0` 条件，天然不会扣成负数。若要更严，可在恢复前先查 `group_buy_order` 状态已是「已退款」才恢复。
3. **支付宝退款幂等**：`alipayRefundPort.refund(outTradeNo, ...)` 以 outTradeNo 为准，参考同；超时 Job 重复触发同一单会因「订单状态已退款」走了 `RefundOrderNodeFilter` 的兜底分支，不会再次退款。
4. **事务范围**：`groupBuyRefundNotify` 建议 `@Transactional`，保证「支付宝退款成功 + 订单状态 + 消息表落库」同一事务（参考仓储层同事务）。MQ 为异步，事务提交后消费。

---

## 6. 验收

前提：RabbitMQ 起、`group_buy_team` / `group_buy_order` / `group_buy_notify_task` 表存在。

1. 造一条超时未支付拼团订单（status=LOCKED）→ 跑 `TimeoutOrderJob`（或 `GroupBuyRefundTimeoutTaskProvider`）。
2. 断言 `group_buy_notify_task` 新增一条 `notify_status=0`、`notify_mq='topic.team_refund'` 的记录。
3. 观察日志：`GroupBuyNotifyJob`（或立即调 `execNotifyJob`）发出 → `RefundSuccessTopicListener` 收到 → 恢复锁单量。
4. 断言 `group_buy_team.lock_count` −1，且 `group_buy_order` 状态已退款。
5. 重复触发原 outTradeNo 退单 → 走 `RefundOrderNodeFilter` 兜底（订单已退款不再处理），不产生新的退款/恢复。

---

## 7. 参考代码对照表

| 参考（group-buy-market） | 本地（s-pay-mall-ddd-cc） |
|---|---|
| `TradeRepository.paid2Refund/paidTeam2Refund/unpaid2Refund`（DB 退款 + insert notify_task，同事务）| `GroupBuyRefundPort.groupBuyRefundNotify`（保留原结构，补 insert）|
| `AbstractRefundOrderStrategy.sendRefundNotifyMessage`（线程池异步 `tradeTaskService.execNotifyJob(notifyTask)`）| `AbstractGroupBuyRefundOrderStrategy.sendRefundNotifyMessage` + `GroupBuyNotifyTaskService.execNotifyJob(task)` |
| `TradeTaskService.execNotifyJob(notifyTask)` → `port.groupBuyNotify` 发 MQ | `GroupBuyNotifyTaskService.execNotifyJob(task)` → `GroupBuyPort.groupBuyNotify` 发 MQ（**已有**）|
| `NotifyTaskEntity` + `NotifyTask` PO | `GroupBuyNotifyTaskEntity` + `GroupBuyNotifyTask` PO（**已有**）|
| `RefundSuccessTopicListener` → `restoreTeamLockStock` | 空壳 → 照 §4 第 7 步填实 |
| `TradeRefundOrderService.restoreTeamLockStock`（按 `RefundTypeEnumVO` 分派）| `GroupBuyRefundOrderService.restoreTeamLockStock`（按 `refundType` 分派）|
| 策略 `reverseStock` → `doReverseStock` → `refund2AddRecovery`（Redis 恢复 key）| 策略 `reverseStock` → `doReverseStock` → `groupBuyTeamRepository.updateSubtractLockCount`（DB lock_count −1）|
| `TeamRefundSuccess`（消息体）| `GroupBuyRefundRestoreEntity`（新增）|
| Redis 分布式锁防重复消费 | `lock_count > 0` 条件兜底（多实例可再加分布式锁）|