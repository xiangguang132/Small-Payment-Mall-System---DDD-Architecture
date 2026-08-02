# 原料库存分配阶段开发总结

## 1. 当前目标

当前阶段围绕“原料库存跨库位分配”展开。

目标是：

1. 按 `materialId` 创建一张原料库存分配单
2. 系统自动从多个 `material_stock` 库位中拆分数量
3. 后续可以按分配单执行锁库、释放、出库
4. 支持按状态分页查询分配单列表
5. 库存数量更新使用数据库条件更新，降低并发扣错库存的风险

---

## 2. 核心表

## 2.1 material_stock

原料库存表。

关键字段：

- `available_qty`：可用库存
- `locked_qty`：锁定库存
- `total_qty`：总库存

数量变化规则：

| 动作 | available_qty | locked_qty | total_qty |
| --- | --- | --- | --- |
| 锁库 | 减少 | 增加 | 不变 |
| 释放 | 增加 | 减少 | 不变 |
| 流水线出库 | 不变 | 减少 | 减少 |
| 人工出库 | 减少 | 不变 | 减少 |

## 2.2 material_stock_allocation

原料库存分配主单表。

关键字段：

- `allocation_no`：分配单号
- `material_id`：原料ID
- `request_qty`：本次请求数量
- `locked_qty`：本次已锁定总数量
- `outbound_qty`：本次已出库总数量
- `released_qty`：本次已释放总数量
- `status`：分配单状态

状态值：

| status | 含义 |
| --- | --- |
| 0 | 待锁库 |
| 1 | 已锁库 |
| 2 | 已出库 |
| 3 | 已释放 |
| 4 | 已取消 |

## 2.3 material_stock_allocation_item

原料库存分配明细表。

每条明细对应一个实际被分配的 `material_stock.id`。

关键字段：

- `allocation_id`：主单ID
- `stock_id`：库存ID
- `material_id`：原料ID
- `storage_address`：分配时库位快照
- `allocate_qty`：本明细分配数量
- `locked_qty`：本明细锁定数量
- `outbound_qty`：本明细出库数量
- `released_qty`：本明细释放数量
- `sort_no`：分配顺序
- `status`：明细状态

---

## 3. 当前接口

Controller：

`s-pay-mall-ddd-trigger/src/main/java/cn.bugstack.trigger/http/MaterialStockAllocationController.java`

## 3.1 创建分配单

```http
POST /api/v1/material-stock-allocation/create
```

请求：

```json
{
  "materialId": 1,
  "quantity": 60,
  "reason": "测试跨库分配"
}
```

说明：

- 先校验 `materialId` 是否存在、是否启用
- 再查询同一 `materialId` 下所有可用库存
- 按顺序贪心拆分成多条 allocation item
- 创建分配主单和明细

如果 `materialId` 不存在，应返回：

```text
原料不存在
```

如果原料存在但没有可用库存，应返回：

```text
无可用库位库存，不能创建分配单
```

## 3.2 查询详情

```http
GET /api/v1/material-stock-allocation/{allocationNo}
```

返回：

`MaterialStockAllocationDetailResponse`

该响应包含主单字段和 `items` 明细。

## 3.3 按 ID 查询详情

```http
GET /api/v1/material-stock-allocation/id/{id}
```

返回：

`MaterialStockAllocationDetailResponse`

## 3.4 锁库

```http
POST /api/v1/material-stock-allocation/lock/{allocationNo}
```

要求：

- 分配单必须是 `status = 0`
- 分配单必须有明细

成功后：

- 主单 `status = 1`
- 明细 `status = 1`
- `material_stock.available_qty` 减少
- `material_stock.locked_qty` 增加

## 3.5 释放锁库

```http
POST /api/v1/material-stock-allocation/release/{allocationNo}
```

要求：

- 分配单必须是 `status = 1`

成功后：

- 主单 `status = 3`
- 明细 `status = 3`
- `material_stock.locked_qty` 减少
- `material_stock.available_qty` 增加

## 3.6 流水线出库

```http
POST /api/v1/material-stock-allocation/auto-outbound/{allocationNo}
```

要求：

- 分配单必须是 `status = 1`

成功后：

- 主单 `status = 2`
- 明细 `status = 2`
- `material_stock.locked_qty` 减少
- `material_stock.total_qty` 减少
- `material_stock.available_qty` 不变

## 3.7 按状态查询列表

```http
GET /api/v1/material-stock-allocation/status/{status}?pageNo=1&pageSize=20
```

返回：

`List<MaterialStockAllocationStatusResponse>`

这个响应是轻量列表响应，不包含 `items`。

示例：

```json
{
  "code": "0000",
  "info": "原料库存分配单-按状态查询完成",
  "data": [
    {
      "id": 1,
      "allocationNo": "MSA202608011230001234",
      "materialId": 1,
      "requestStockId": null,
      "requestQty": 60,
      "lockedQty": 60,
      "outboundQty": 0,
      "releasedQty": 0,
      "status": 1,
      "reason": "测试跨库分配",
      "createTime": "2026-08-01T12:30:00",
      "updateTime": "2026-08-01T12:31:00"
    }
  ]
}
```

注意：

如果前端已经配置 `baseURL = /api/v1`，调用时不要再写一次 `/api/v1`，否则会变成：

```text
/api/v1/api/v1/material-stock-allocation/status/1
```

正确路径是：

```text
/api/v1/material-stock-allocation/status/1
```

---

## 4. Response 类型

## 4.1 MaterialStockAllocationDetailResponse

文件：

`s-pay-mall-ddd-api/src/main/java/cn/bugstack/api/response/materialstockallocation/MaterialStockAllocationDetailResponse.java`

用途：

详情接口使用。

特点：

- 包含主单字段
- 包含 `items`

## 4.2 MaterialStockAllocationItemResponse

文件：

`s-pay-mall-ddd-api/src/main/java/cn/bugstack/api/response/materialstockallocation/MaterialStockAllocationItemResponse.java`

用途：

详情接口里的明细项。

## 4.3 MaterialStockAllocationStatusResponse

文件：

`s-pay-mall-ddd-api/src/main/java/cn/bugstack/api/response/materialstockallocation/MaterialStockAllocationStatusResponse.java`

用途：

按状态查询列表接口使用。

特点：

- 只包含主单字段
- 不包含 `items`
- 避免列表接口返回结构过重

---

## 5. Assembler

文件：

`s-pay-mall-ddd-trigger/src/main/java/cn.bugstack.trigger/assembler/MaterialStockAllocationAssembler.java`

当前有两个转换方法：

```java
toDetailResponse(MaterialStockAllocationAggregate aggregate)
```

用于详情接口，包含 `items`。

```java
toStatusResponse(MaterialStockAllocationAggregate aggregate)
```

用于按状态列表接口，不包含 `items`。

---

## 6. 分配单创建逻辑

Service：

`s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/materialstockallocation/service/MaterialStockAllocationService.java`

创建流程：

1. 校验 `materialId` 和 `quantity`
2. 调用 `materialService.validateMaterialEnabled(materialId)`
3. 查询同一 `materialId` 下所有可用库存
4. 调用 `splitAllocationItems(...)` 贪心拆分
5. 创建分配单号
6. 保存主单和明细
7. 返回 `allocationNo`

当前分配策略：

- 按 `material_stock` 查询结果顺序分配
- 查询 SQL 当前按 `create_time asc, id asc`
- 不考虑距离、批次、先进先出、成本等复杂规则

---

## 7. 锁库、释放、出库逻辑

当前仍然由：

`MaterialStockAllocationService`

驱动业务流程。

也就是说：

```text
MaterialStockAllocationService
  -> 查询分配单
  -> 校验状态
  -> 遍历 allocation item
  -> 调用 MaterialStockRepository 修改 material_stock
  -> 更新主单和明细状态
```

职责划分：

```text
MaterialStockAllocationService：
负责分配单业务流程、状态流转、明细处理。

MaterialStockRepository / IMaterialStockDao：
负责 material_stock 数量的安全更新。

SQL：
负责并发条件判断。
```

---

## 8. 并发安全改造

说明文档：

`docs/Learn-原料库存并发安全条件更新改造说明.md`

本次没有使用 Redis 分布式锁。

原因：

- 当前只有一个 8080 实例
- 主要风险来自多个定时任务或请求同时修改同一条库存
- 数据库条件更新更简单，也更适合当前阶段

核心方案：

```text
数据库事务 + 条件更新 SQL
```

示例：锁库 SQL

```sql
update material_stock
set available_qty = available_qty - #{lockQty},
    locked_qty = locked_qty + #{lockQty},
    update_time = now()
where id = #{stockId}
  and available_qty >= #{lockQty}
  and is_del = 0
```

如果两个任务同时锁同一条库存：

- 第一个任务成功，影响行数为 1
- 第二个任务基于最新库存重新判断
- 如果库存不足，影响行数为 0
- Service 抛业务异常
- 事务回滚

---

## 9. 条件更新方法

领域仓储接口：

`s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/materialstock/repository/IMaterialStockRepository.java`

新增：

```java
boolean lockStock(Long stockId, BigDecimal lockQty);

boolean releaseStock(Long stockId, BigDecimal releaseQty);

boolean outboundLockedStock(Long stockId, BigDecimal outboundQty);

boolean outboundAvailableStock(Long stockId, BigDecimal outboundQty);
```

DAO：

`s-pay-mall-ddd-infrastructure/src/main/java/cn/bugstack/infrastructure/dao/IMaterialStockDao.java`

新增：

```java
int lockStock(...);
int releaseStock(...);
int outboundLockedStock(...);
int outboundAvailableStock(...);
```

Mapper：

`s-pay-mall-ddd-app/src/main/resources/mybatis/mapper/material_stock_mapper.xml`

新增 4 条条件更新 SQL。

---

## 10. 定时任务当前结论

目前不建议立刻写定时任务。

原因：

1. 当前项目还没有原料库存分配相关 job
2. 现在刚补了按状态查询接口
3. 还需要先验证接口流程是否完整稳定
4. 定时任务真正需要的是“按状态扫描待处理分配单”的能力

当前建议顺序：

1. 先手动验证完整接口流程
2. 完善按状态查询能力
3. 再写定时任务

后续如果写定时任务，建议位置：

`s-pay-mall-ddd-trigger/src/main/java/cn.bugstack.trigger/job/MaterialStockAllocationJob.java`

定时任务异常处理建议：

```java
for (String allocationNo : allocationNos) {
    try {
        materialStockAllocationService.lock(allocationNo);
    } catch (IllegalArgumentException e) {
        log.warn("原料库存分配单处理失败 allocationNo:{} reason:{}",
                allocationNo, e.getMessage());
    } catch (Exception e) {
        log.error("原料库存分配单处理异常 allocationNo:{}", allocationNo, e);
    }
}
```

要点：

- `try-catch` 放在循环内部
- 单笔失败不影响后续分配单
- 库存不足这种业务失败用 `warn`
- 非预期异常用 `error`

---

## 11. 下一步建议

## 11.1 先验证接口流程

建议按顺序验证：

```text
create
detail
lock
detail
auto-outbound
detail
status list
```

同时查看数据库：

```text
material_stock
material_stock_allocation
material_stock_allocation_item
```

确认数量和状态都正确。

## 11.2 补按状态查询的完整链路

如果还没完全补齐，需要确认这些文件：

- `IMaterialStockAllocationService`
- `MaterialStockAllocationService`
- `IMaterialStockAllocationRepository`
- `MaterialStockAllocationRepository`
- `IMaterialStockAllocationDao`
- `material_stock_allocation_mapper.xml`
- `MaterialStockAllocationController`

分页参数建议：

```text
pageNo 默认 1
pageSize 默认 20
pageSize 最大 100
```

## 11.3 再考虑定时任务

定时任务可以按状态扫描：

```text
status = 0 待锁库
status = 1 已锁库
```

但是具体自动做什么，需要先明确业务：

- 是自动锁库？
- 是自动出库？
- 是超时释放？
- 是失败重试？

业务目标明确后再写 job。

---

## 12. 当前未完成验证

执行：

```bash
mvn test
```

当前本机 Maven 环境报错：

```text
找不到或无法加载主类 org.codehaus.plexus.classworlds.launcher.Launcher
```

所以目前没有完成 Maven 编译验证。

后续继续开发前，建议先修复 Maven 环境。
