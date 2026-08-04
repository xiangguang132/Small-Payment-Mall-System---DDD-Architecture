# warehouse_stock_flow 库存流水表说明

## 1. 表的定位

`warehouse_stock_flow` 是成品仓库库存流水表。

它不负责保存当前库存余额，当前库存余额保存在 `warehouse_stock` 表中。

两张表的职责区别：

| 表 | 作用 |
| --- | --- |
| `warehouse_stock` | 保存某个仓库、某个商品当前的库存余额 |
| `warehouse_stock_flow` | 记录每一次库存变动的业务来源和变动数量 |

可以理解为：

- `warehouse_stock` 是账户余额。
- `warehouse_stock_flow` 是账户流水。

## 2. 表结构含义

主要字段：

| 字段 | 含义 |
| --- | --- |
| `warehouse_id` | 仓库 ID |
| `product_id` | 商品 ID |
| `quantity` | 库存变动数量，入库为正数，出库为负数 |
| `biz_type` | 业务类型 |
| `biz_no` | 业务单号 |
| `reason` | 变动原因 |
| `create_time` | 流水创建时间 |

其中最关键的是：

```sql
UNIQUE KEY `uk_biz` (`biz_type`, `biz_no`)
```

这个唯一索引用来保证同一笔业务单据只生成一次库存流水。

## 3. 在生产单入库链路中的作用

生产需求单执行到成品入库阶段时，会调用：

```java
stockService.inbound(
    order.getWarehouseId(),
    order.getProductId(),
    order.getProductQuantity().intValue(),
    "PRODUCTION_ORDER",
    order.getOrderNo()
);
```

这里传入的业务标识是：

| 参数 | 示例 | 含义 |
| --- | --- | --- |
| `bizType` | `PRODUCTION_ORDER` | 表示来源是生产单 |
| `bizNo` | `PO202608041036505191462` | 生产单号 |

执行时先插入 `warehouse_stock_flow`：

```java
boolean saved = stockRepository.saveFlow(...);
```

如果流水插入成功，才会继续更新 `warehouse_stock` 的库存余额。

如果流水已经存在，说明这张业务单据已经入过库，方法会直接返回，不再重复增加库存。

## 4. 为什么要先写流水再改库存

生产单任务可能失败后被重试。

如果没有流水表，可能出现这种问题：

1. 第一次执行生产单。
2. 成品库存已经增加。
3. 后面更新生产单状态失败。
4. 定时任务再次重试。
5. 成品库存又增加一次。

这样会导致库存重复入库。

引入 `warehouse_stock_flow` 后，流程变成：

1. 先插入流水。
2. 使用 `biz_type + biz_no` 唯一键防止同一业务单重复插入。
3. 只有流水插入成功，才更新库存余额。
4. 如果重试时发现流水已存在，说明之前已经处理过，直接跳过库存增加。

所以 `warehouse_stock_flow` 的核心作用是库存变更幂等。

## 5. 本次异常和 flow 表的关系

本次生产单失败信息：

```text
Invalid bound statement (not found): cn.bugstack.infrastructure.dao.IWarehouseStockFlowDao.insert
```

含义是：

代码调用了 `IWarehouseStockFlowDao.insert`，准备插入成品库存流水，但 MyBatis 没有找到对应的 mapper statement。

这不是业务库存不足问题，而是 MyBatis mapper 绑定问题。

涉及链路：

```text
ProductionOrderExecutor
  -> stockService.inbound(...)
  -> stockRepository.saveFlow(...)
  -> warehouseStockFlowDao.insert(...)
```

失败发生在 `INBOUND_PRODUCT` 阶段。

当前代码中，`INBOUND_PRODUCT` 被标记为需要人工介入：

```java
INBOUND_PRODUCT("INBOUND_PRODUCT", true)
```

因此该阶段失败后，生产单会直接进入 `status = 4`。

## 6. status=4 后是否还会重试

不会。

生产单状态定义：

| 状态 | 含义 |
| --- | --- |
| `0` | 待处理 |
| `1` | 处理中 |
| `2` | 已完成 |
| `3` | 失败可重试 |
| `4` | 失败终态 |
| `5` | 已取消 |

自动任务只扫描：

```sql
where status in (0, 3)
  and (next_retry_time is null or next_retry_time <= now())
```

所以 `status = 4` 的生产单不会被自动任务再次捞起。

手动重试也只允许 `status = 3`：

```java
public static boolean canRetry(Integer status) {
    return Integer.valueOf(RETRYABLE_FAILED).equals(status);
}
```

因此 `status = 4` 也不能通过当前手动重试接口继续重试。

## 7. 为什么 INBOUND_PRODUCT 要人工介入

`INBOUND_PRODUCT` 是生产链路靠后的阶段。

执行到这里时，前面的动作可能已经完成：

1. 生产单已接单。
2. 原料分配单已创建。
3. 原料已锁定。
4. 原料已出库。
5. 开始成品入库。

如果这个阶段失败，系统不能简单地自动重复执行，因为需要确认：

- 原料是否已经扣减。
- 成品库存是否已经增加。
- 库存流水是否已经写入。
- 生产单状态是否和实际库存一致。

所以当前设计选择将它标记为需要人工介入，失败后直接进入终态失败。

## 8. 修复方向

如果遇到 `IWarehouseStockFlowDao.insert` 找不到绑定，需要优先检查：

1. `warehouse_stock_flow_mapper.xml` 是否存在。
2. mapper 文件 namespace 是否是 `cn.bugstack.infrastructure.dao.IWarehouseStockFlowDao`。
3. `<insert id="insert">` 是否和 DAO 方法名一致。
4. `application-dev.yml` 的 `mybatis.mapper-locations` 是否能扫描到该 mapper。
5. 编译后的 `target/classes/mybatis/mapper/` 下是否包含 `warehouse_stock_flow_mapper.xml`。

修复 mapper 绑定后，不建议直接把 `status = 4` 改回 `3` 盲目重试。

更稳妥的处理方式：

1. 查 `warehouse_stock_flow` 是否已经存在该生产单流水。
2. 查 `warehouse_stock` 是否已经增加成品库存。
3. 查原料分配单是否已经完成出库。
4. 确认数据一致后，再人工决定是补状态、补流水、补库存，还是重新开放重试。

