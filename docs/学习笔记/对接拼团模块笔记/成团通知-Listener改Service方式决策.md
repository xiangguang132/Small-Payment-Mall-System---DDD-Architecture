# 成团通知 — Listener 实现方式决策（改 Service 方式）

> 用途：记录 `GroupBuySuccessTopicListener`（对应参考实现的 `TeamSuccessTopicListener`）采用 **domain Service 方式** 而非参考代码的 **dao + repository 内联方式** 的决策。
> 配套参考：[成团通知-参考实现.md](./成团通知-参考实现.md)

---

## 0. 结论

`GroupBuySuccessTopicListener` **用 Service 方式实现**（对齐 `AlipayNotifyListener` 的写法），
不照抄参考实现里 `dao + repository` 内联的写法。

即：**listener 只留 `@RabbitListener` 壳，注入并调用一个 domain 层 service**，由 service 内聚
"解析消息 → 查团内成员 → 批量写站内信"，领域层再走 `IUserNotifyRepository` 落库。

---

## 1. 为什么两种写法大家都没错

| 写法 | 出处 | 适用场景 |
|---|---|---|
| `dao + repository` 内联 | 参考项目 `TeamSuccessTopicListener` | 消费逻辑很薄（查成员 + 批量 insert ≈ 10 行胶水），参考项目直接内联在 trigger |
| `domain Service` | 本项目 `AlipayNotifyListener` → `IAlipayNotifyTaskService.processTask` | 回调背后有完整业务处理管线（规则链/状态机），必须下沉 domain |

成团通知逻辑虽轻，但二者皆可；最终选 **Service**，理由见 §2。

## 2. 选 Service 的理由

1. **符合本项目现有约定**：trigger 层统一面向 domain service，`trigger → service → repository/dao`。
   `AlipayNotifyListener` 就是这条链的既有范例，成团通知照同一形状，代码风格一致。
2. **避免 infra 类型泄漏进 trigger**：参考实现把 `IGroupBuyOrderDao`（infrastructure 层）直接注入 listener。
   虽然能跑，但 trigger 层带上 infra 依赖是 DDD 上的小味道。包一层 domain service 后，
   DAO 被收进领域层仓储（`IUserNotifyRepository` 实现）后面，listener 只见 domain 接口。
3. **后续可扩展**：若以后站内信要加标题/内容模板、多通知类型，service 是更自然的落点。

> 参考实现的 `dao + repository` 内联写法不是错误，只是更"薄"；本项目既有约定偏向 service，故采用之。

---

## 3. 现状检查（2026-08-23 记录）

Debug 时发现 `GroupBuySuccessTopicListener.java` 目前是 **AlipayNotifyListener 的复制改名版，编译不过**：

```java
// s-pay-mall-ddd-trigger/**/listener/GroupBuySuccessTopicListener.java
@Resource                                       // ← 注入了 DAO/repository，但方法体没用
private IGroupBuyOrderDao groupBuyOrderDao;
@Resource
private IUserNotifyRepository userNotifyRepository;

public void listener(String message) {
    log.info("接收消息（支付宝异步通知）:{}", message);   // ← 日志文案还是支付宝的
    try {
        alipayNotifyTaskService.processTask(message);    // ← 本类无此字段，直接编译错误
    } catch ...
}
```

无论如何都要整体重写，不是一个"已有实现改改"的问题。

### 缺口清单

| 缺口 | 现状 |
|---|---|
| `IGroupBuyOrderDao.queryUserIdListByTeamId` | ❌ DAO 接口没这个方法；SQL 已在 `user_notify_task_mapper.xml` 里 |
| `IUserNotifyRepository` / `UserNotifyRepository` | ⚠️ 都是空壳，`insertList` 未实现 |
| `UserNotifyEntity`（domain 实体） | ❌ 未建；只有基础设施 PO `UserNotifyTask` 和 `IUserNotifyDao.insertList(List<UserNotifyTask>)` |
| `GroupBuyNotifyTaskService`（投递侧） | ✅ 已按参考实现建好，`execNotifyJob` 循环已实现 |

> 投递侧（账本 `group_buy_notify_task` + `GroupBuyNotifyJob` + `GroupBuyTradePort`）走参考实现即可，不受本决策影响。

---

## 4. 计划实现形状（待落地）

```
trigger: GroupBuySuccessTopicListener
  @RabbitListener(topic_team_success)
  → iUserNotifyService.writeTeamSuccessNotify(message)     // 薄壳，与 AlipayNotifyListener 同形

domain: IUserNotifyService + UserNotifyService
  parse message → teamId
  IGroupBuyOrderRepository.queryUserIdListByTeamId(teamId)  // 收进领域仓储，DAO 不外漏
  → build List<UserNotifyEntity>
  → iUserNotifyRepository.insertList(list)                  // 落 user_notify，唯一键防重

infrastructure: UserNotifyRepository
  实现 insertList → userNotifyDao.insertList(UserNotifyTask 转换体)
```

依赖唯一键 `uk_user_team_type(user_id, team_id, notify_type)` 挡 MQ 重复消费，不必在消费端加锁（同参考实现 §9.2）。

---

## 5. 待办

- [ ] 领域层：建 `UserNotifyEntity`（对齐 PO `UserNotifyTask` 字段）
- [ ] 领域层：`IUserNotifyRepository` 加 `insertList(List<UserNotifyEntity>)`
- [ ] 基础设施：`UserNotifyRepository` 实现 `insertList`（Entity ↔ PO ↔ `IUserNotifyDao`）
- [ ] 领域层：`IGroupBuyOrderRepository` 补 `queryUserIdListByTeamId`，
      `GroupBuyOrderRepository` 实现走 `IGroupBuyOrderDao`
- [ ] 领域层：建 `IUserNotifyService` + `UserNotifyService.writeTeamSuccessNotify(message)`
- [ ] trigger：重写 `GroupBuySuccessTopicListener`，注入 `IUserNotifyService`，删除 `alipayNotifyTaskService` 残留
- [ ] 校验：删除本类引用 `alipayNotifyTaskService` / `IGroupBuyOrderDao`（infra）的残留 import