# RabbitMQ 分布式多端消费 — 调研分析

更新日期：2026-08-19

## 1. 背景

`支付宝回调与通知-技术复用方案.md` 中设计了基于 `alipay_notify_task` 的可靠任务链路：验签 → 落任务 → 定时补偿 → 分布式锁 → 状态机。当前这套机制已经落地了落库部分（`saveNotifyTask`），但任务处理、定时补偿、分叉逻辑均未实现。

下一步目标是引入 RabbitMQ，将任务处理从"同步立即处理"改为"异步事件驱动"，实现分布式多端消费。参考项目 `group-buy-market` 已有成熟的 RabbitMQ 集成模式。

本文档先检查当前项目已有实现，再分析参考项目的 MQ 架构，最后给出改造方案和需确认的问题。

## 2. 当前项目已实现部分

### 2.1 已完成的类

| 层 | 类 | 状态 | 说明 |
|---|---|---|---|
| Trigger | `AliPayController.payNotify` | ✅ 完成 | 验签 + 落任务，返回 success |
| Domain | `IAlipayNotifyTaskService` | ✅ 完成 | 接口只有 `saveNotifyTask` |
| Domain | `AlipayNotifyTaskService` | ✅ 完成 | 查询已存在 → 不重复处理；不存在 → 插入 task_status=0 |
| Domain | `IAlipayNotifyTaskRepository` | ✅ 完成 | `queryByOutTradeNoAndTradeNo` + `save` |
| Infrastructure | `AlipayNotifyTaskRepository` | ✅ 完成 | 含 toEntity/toPo 转换，唯一键冲突按已接收处理 |
| Infrastructure | `IAlipayNotifyTaskDao` | ✅ 完成 | `queryByOutTradeNoAndTradeNo` + `insert` |
| Infrastructure | `AlipayNotifyTask` (PO) | ✅ 完成 | 全字段映射 |
| App | `alipay_notify_task_mapper.xml` | ✅ 完成 | MyBatis 映射 |
| Trigger | `AlipayNotifyJob` | ❌ 空壳 | 类体为空 |

### 2.2 缺失能力（复用方案中提到但未实现）

1. **`AlipayNotifyTaskService.processTask()`** — 任务处理服务，包含：
   - 查询 `task_status in (0, 2)` 的待处理任务
   - Redis 分布式锁（key: `alipay_notify_lock_<outTradeNo>`）
   - 按 `orderType` 分叉：普通订单 → `changeOrderPaySuccess`，拼团订单 → `settlementGroupBuyOrder`
   - 任务状态机流转（成功 / 重试 / 失败）
2. **`AlipayNotifyJob`** — 定时补偿任务，扫描失败任务重新处理
3. **DAO/Repository 层** — 缺少状态更新方法：
   - `queryUnExecutedTaskList()`
   - `updateTaskStatusSuccess()`
   - `updateTaskStatusRetry()`
   - `updateTaskStatusError()`

### 2.3 RabbitMQ 基础设施

当前项目 **完全没有引入 RabbitMQ**：

- pom.xml 无 `spring-boot-starter-amqp` 依赖
- application.yml 无 RabbitMQ 配置
- 无 EventPublisher、Listener 等 MQ 相关类

## 3. 参考项目 RabbitMQ 架构分析

### 3.1 整体架构

```
生产者                                    消费者
┌─────────────────┐                  ┌──────────────────────────┐
│ EventPublisher   │                  │ TeamSuccessTopicListener  │
│   ↓              │                  │ RefundSuccessTopicListener │
│ RabbitTemplate   │  ── exchange ──> │   @RabbitListener          │
│ convertAndSend() │  (Topic 类型)    │   @QueueBinding            │
└─────────────────┘                  └──────────────────────────┘
```

### 3.2 Maven 依赖

在全局 pom.xml 和 app / trigger / infrastructure 三个模块都引入了：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

### 3.3 配置（application-dev.yml）

```yaml
spring:
  rabbitmq:
    host: 192.144.234.86
    port: 5672
    username: admin
    password: admin
    listener:
      simple:
        prefetch: 1          # 每次只投递 1 条，消费完再投递
    template:
      delivery-mode: persistent  # 全局持久化
    config:
      producer:
        exchange: group_buy_market_exchange   # 统一 Topic 交换机
        topic_team_success:
          routing_key: topic.team_success
          queue: group_buy_market_queue_2_topic_team_success
        topic_team_refund:
          routing_key: topic.team_refund
          queue: group_buy_market_queue_2_topic_team_refund
```

### 3.4 生产者模式

`EventPublisher`（infrastructure 层）：

```java
@Slf4j
@Component
public class EventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.config.producer.exchange}")
    private String exchangeName;

    public void publish(String routingKey, String message) {
        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, message, m -> {
                m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                return m;
            });
        } catch (Exception e) {
            log.error("发送MQ消息失败 message:{}", message, e);
            throw e;
        }
    }
}
```

调用链：

```
TradeRepository.settlementMarketPayOrder()
  → 写入 notify_task 表（notifyType = MQ）
  → TradePort.groupBuyNotify()
    → EventPublisher.publish(routingKey, parameterJson)
```

### 3.5 消费者模式

消费者在 trigger/listener 目录，使用注解式声明：

```java
@Slf4j
@Component
public class RefundSuccessTopicListener {

    @Resource
    private ITradeRefundOrderService tradeRefundOrderService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "${spring.rabbitmq.config.producer.topic_team_refund.queue}"),
                    exchange = @Exchange(value = "${spring.rabbitmq.config.producer.exchange}", type = ExchangeTypes.TOPIC),
                    key = "${spring.rabbitmq.config.producer.topic_team_refund.routing_key}"
            )
    )
    public void listener(String message) {
        log.info("接收消息（退单成功）:{}", message);
        TeamRefundSuccess teamRefundSuccess = JSON.parseObject(message, TeamRefundSuccess.class);
        try {
            tradeRefundOrderService.restoreTeamLockStock(teamRefundSuccess);
        } catch (Exception e) {
            log.info("接收消息（退单成功）- 恢复拼团队伍锁单量失败:{}", message, e);
            throw new RuntimeException(e);  // 抛异常触发 MQ 重试
        }
    }
}
```

### 3.6 核心设计模式总结

| 特性 | 实现方式 |
|---|---|
| 交换机类型 | Topic（统一交换机，按 routingKey 分发） |
| 队列命名 | `{项目名}_queue_2_topic_{业务名}` |
| 消息格式 | JSON 字符串 |
| 消息持久化 | `delivery-mode: persistent`（生产者 + 全局配置） |
| 消费确认 | ACK 自动确认（抛异常触发重试） |
| 并发控制 | `prefetch: 1`（单线程串行消费） |
| 异常重试 | 抛 RuntimeException → MQ 自动重新投递 |
| 配置方式 | 队列/routingKey 从 yml 读取，支持动态调整 |

## 4. 引入 RabbitMQ 后的架构变化

### 4.1 改造前（当前）

```
支付宝回调
  → 验签
  → 写 alipay_notify_task (task_status=0)
  → 返回 success

AlipayNotifyTaskService.saveNotifyTask() 仅落库，无后续处理
AlipayNotifyJob 空壳
```

### 4.2 改造后

```
支付宝回调
  → 验签
  → 写 alipay_notify_task (task_status=0)
  → EventPublisher.publish(routingKey, outTradeNo)
  → 返回 success
                                      ↓
                          s_pay_mall_exchange (Topic)
                                      ↓
                          s_pay_mall_queue_alipay_notify
                                      ↓
                          AlipayNotifyListener.listener(message)
                                      ↓
                          AlipayNotifyTaskService.processTask(outTradeNo)
                            → Redis 分布式锁
                            → 按 orderType 分叉
                              - 普通订单 → changeOrderPaySuccess
                              - 拼团订单 → settlementGroupBuyOrder
                            → 更新 task_status
                            → 释放锁

AlipayNotifyJob（定时补偿）
  → 扫描 task_status in (0, 2)
  → EventPublisher.publish(routingKey, outTradeNo) 重新投递
  → 等待消费端重新处理
```

### 4.3 核心变化

1. **事件驱动替代同步调用**：`saveNotifyTask` 后不再直接处理，而是发 MQ 消息
2. **消费端解耦**：业务处理逻辑在 `AlipayNotifyListener` 中，与回调入口完全分离
3. **多实例负载均衡**：RabbitMQ 同一 queue 的消息只投递给一个 consumer，天然实现分布式消费
4. **定时补偿职责转变**：`AlipayNotifyJob` 不再直接处理任务，而是扫描失败任务重新投递 MQ

## 5. 需新增的组件

### 5.1 Maven 依赖

在以下模块的 pom.xml 中添加：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

| 模块 | 是否需要 |
|---|---|
| 全局 pom.xml（dependencyManagement） | ✅ |
| s-pay-mall-ddd-app | ✅ |
| s-pay-mall-ddd-infrastructure | ✅（EventPublisher 在此层） |
| s-pay-mall-ddd-trigger | ✅（Listener 在此层） |
| s-pay-mall-ddd-domain | ❌（领域层不直接依赖 MQ） |

### 5.2 application.yml 配置

```yaml
spring:
  rabbitmq:
    host: 127.0.0.1
    port: 5672
    username: guest
    password: guest
    listener:
      simple:
        prefetch: 1
    template:
      delivery-mode: persistent
    config:
      producer:
        exchange: s_pay_mall_exchange
        topic_alipay_notify:
          routing_key: topic.alipay_notify
          queue: s_pay_mall_queue_alipay_notify
```

### 5.3 新增文件清单

| 层 | 文件路径 | 说明 |
|---|---|---|
| Infrastructure | `infrastructure/event/EventPublisher.java` | 通用 MQ 发送器 |
| Trigger | `trigger/listener/AlipayNotifyListener.java` | 消费端，接收消息后调用 processTask |
| Domain | `AlipayNotifyTaskService` | 增加 `processTask()` 方法 |
| Domain | `IAlipayNotifyTaskService` | 增加 `processTask()` 接口定义 |
| Domain | `IAlipayNotifyTaskRepository` | 增加查询待处理任务、更新状态方法 |
| Infrastructure | `AlipayNotifyTaskRepository` | 实现上述接口方法 |
| Infrastructure | `IAlipayNotifyTaskDao` | 增加对应 DAO 方法 |
| App | `alipay_notify_task_mapper.xml` | 增加对应 SQL |

### 5.4 已有文件修改

| 文件 | 修改内容 |
|---|---|
| `AliPayController.payNotify` | 落任务后调用 `EventPublisher.publish()` |
| `AlipayNotifyJob` | 实现定时扫描 + 重新投递 MQ |
| `alipay_notify_task_mapper.xml` | 新增 updateStatus 系列 SQL |

## 6. 需确认的问题

| 序号 | 问题 | 说明 |
|---|---|---|
| 1 | RabbitMQ 服务器地址 | 当前环境是否有可用的 RabbitMQ 实例？参考项目用的是远程服务器 `192.144.234.86:5672` |
| 2 | prefetch 并发数 | 参考项目用 `prefetch:1` 串行消费。支付宝回调场景是否需要调大？建议先保持 1，后续按需调整 |
| 3 | 异常重试策略 | 参考项目直接抛 RuntimeException 让 MQ 自动重试。是否需要引入死信队列（DLQ）做最终兜底？ |
| 4 | 与"立即处理"的取舍 | 引入 MQ 后，`saveNotifyTask` 后的同步"立即处理"可以去掉，完全交给消费端。是否接受？ |
| 5 | 模块依赖 | `EventPublisher` 在 infrastructure 层，`AliPayController` 在 trigger 层。需确认 trigger → infrastructure 依赖已打通 |
| 6 | 连接信息 | `application-dev.yml` 中 RabbitMQ 的 host/username/password 如何配置？本地还是远程？ |

## 7. 风险评估

| 风险 | 级别 | 说明 | 缓解措施 |
|---|---|---|---|
| RabbitMQ 不可用导致回调处理中断 | 中 | MQ 挂了，消息发不出去 | `alipay_notify_task` 先落库再发 MQ；定时补偿扫描失败任务重新投递 |
| 消息重复消费 | 低 | 网络抖动可能导致重复投递 | 数据库唯一键 + 任务状态校验，消费端天然幂等 |
| 消费失败无限重试 | 中 | 业务异常导致消息反复重投 | 设置最大重试次数（如 5 次），超过后标记 task_status=3，由定时任务人工介入 |
| 多实例重复消费 | 低 | 担心多个实例同时处理同一消息 | RabbitMQ 同一 queue 的消息只投递给一个 consumer，天然解决 |
| 消息丢失 | 低 | 生产者发送后 MQ 未持久化 | 消息持久化 + 队列持久化 + 定时补偿三重保障 |

## 8. 实施顺序建议

```text
第 1 步：引入 Maven 依赖 + application.yml 配置
第 2 步：新增 EventPublisher（复用 group-buy-market 写法）
第 3 步：补全 AlipayNotifyTaskService.processTask()（任务处理 + 状态机 + 分叉）
第 4 步：补全 DAO/Repository 层的状态查询和更新方法
第 5 步：修改 AliPayController.payNotify，落任务后发 MQ 消息
第 6 步：新增 AlipayNotifyListener（消费端）
第 7 步：实现 AlipayNotifyJob（定时补偿重新投递）
第 8 步：联调测试
```

## 9. 与现有方案的关系

本方案是 `支付宝回调与通知-技术复用方案.md` 的延续，不是替代：

| 方案 | 解决的问题 |
|---|---|
| 技术复用方案 | 引入任务表 + 状态机 + 分布式锁 + 定时补偿，解决支付宝回调可靠性 |
| 本文（RabbitMQ） | 在技术复用方案基础上，引入异步事件驱动，解决分布式多端消费、回调入口与业务处理解耦 |

两者互补：任务表保证消息不丢（本地持久化），MQ 保证消息分发（异步多端消费），定时补偿兜底（MQ 消费失败的最终保障）。

## 10. 参考材料

```text
D:\code\group-buy-market\group-buy-market-infrastructure\src\main\java\cn\bugstack\infrastructure\event\EventPublisher.java
D:\code\group-buy-market\group-buy-market-trigger\src\main\java\cn\bugstack\trigger\listener\TeamSuccessTopicListener.java
D:\code\group-buy-market\group-buy-market-trigger\src\main\java\cn\bugstack\trigger\listener\RefundSuccessTopicListener.java
D:\code\group-buy-market\group-buy-market-app\src\main\resources\application-dev.yml
D:\code\group-buy-market\group-buy-market-infrastructure\src\main\java\cn\bugstack\infrastructure\adapter\port\TradePort.java
D:\code\s-pay-mall-ddd-cc\docs\学习笔记\支付宝回调与通知\支付宝回调与通知-技术复用方案.md
```
