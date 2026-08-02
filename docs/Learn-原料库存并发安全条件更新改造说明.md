# 原料库存并发安全条件更新改造说明

## 1. 改造背景

原来的库存扣减逻辑主要是：

1. 先查询 `material_stock` 当前库存
2. 在 Java 代码里判断库存是否足够
3. 修改聚合对象字段
4. 调用 `updateById(...)` 整体更新库存记录

这种写法在单个请求下没有问题，但如果多个定时任务或多个请求同时修改同一条库存，就可能出现并发问题。

例如库存可用数量是 100，两个任务同时锁定 80：

- 任务A查到 availableQty = 100
- 任务B也查到 availableQty = 100
- 两个任务都认为库存足够
- 最后都更新成功，业务上等于锁定了 160

所以本次改造目标是：

**把库存是否足够的判断下沉到数据库 update 的 where 条件里，让数据库保证一次库存变更的原子性。**

---

## 2. 核心方案

使用“数据库条件更新”替代“先查再改”。

### 改造前

```java
MaterialStockAggregate stock = materialStockRepository.queryById(id);
BigDecimal available = stock.getAvailableQty();
if (available.compareTo(lockQty) < 0) {
    throw new IllegalArgumentException("原料可用库存不足，不能锁定");
}
stock.setAvailableQty(available.subtract(lockQty));
stock.setLockedQty(stock.getLockedQty().add(lockQty));
materialStockRepository.updateById(stock);
```

### 改造后

```java
if (!materialStockRepository.lockStock(id, lockQty)) {
    throw new IllegalArgumentException("原料可用库存不足，不能锁定");
}
```

真正的并发保护放在 SQL：

```sql
update material_stock
set available_qty = available_qty - #{lockQty},
    locked_qty = locked_qty + #{lockQty},
    update_time = now()
where id = #{stockId}
  and available_qty >= #{lockQty}
  and is_del = 0
```

如果影响行数是 `1`，说明扣减成功。

如果影响行数是 `0`，说明库存已经不足、记录不存在或已删除，服务层按业务失败处理。

---

## 3. 修改文件清单

## 3.1 领域层库存仓储接口

文件：

`s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/materialstock/repository/IMaterialStockRepository.java`

新增方法：

```java
boolean lockStock(Long stockId, BigDecimal lockQty);

boolean releaseStock(Long stockId, BigDecimal releaseQty);

boolean outboundLockedStock(Long stockId, BigDecimal outboundQty);

boolean outboundAvailableStock(Long stockId, BigDecimal outboundQty);
```

作用：

- `lockStock`：锁库，可用库存减少，锁定库存增加
- `releaseStock`：释放锁库，可用库存增加，锁定库存减少
- `outboundLockedStock`：流水线出库，从锁定库存正式扣减总库存
- `outboundAvailableStock`：人工出库，直接从可用库存和总库存扣减

---

## 3.2 基础设施层 DAO 接口

文件：

`s-pay-mall-ddd-infrastructure/src/main/java/cn/bugstack/infrastructure/dao/IMaterialStockDao.java`

新增方法：

```java
int lockStock(@Param("stockId") Long stockId, @Param("lockQty") BigDecimal lockQty);

int releaseStock(@Param("stockId") Long stockId, @Param("releaseQty") BigDecimal releaseQty);

int outboundLockedStock(@Param("stockId") Long stockId, @Param("outboundQty") BigDecimal outboundQty);

int outboundAvailableStock(@Param("stockId") Long stockId, @Param("outboundQty") BigDecimal outboundQty);
```

这里返回 `int`，表示 SQL 影响行数。

- `1`：更新成功
- `0`：条件不满足，更新失败

---

## 3.3 MyBatis SQL

文件：

`s-pay-mall-ddd-app/src/main/resources/mybatis/mapper/material_stock_mapper.xml`

新增 4 条条件更新 SQL。

### 锁库

```sql
update material_stock
set available_qty = available_qty - #{lockQty},
    locked_qty = locked_qty + #{lockQty},
    update_time = now()
where id = #{stockId}
  and available_qty >= #{lockQty}
  and is_del = 0
```

含义：

只有当可用库存足够时，才允许锁定。

### 释放锁库

```sql
update material_stock
set available_qty = available_qty + #{releaseQty},
    locked_qty = locked_qty - #{releaseQty},
    update_time = now()
where id = #{stockId}
  and locked_qty >= #{releaseQty}
  and is_del = 0
```

含义：

只有当锁定库存足够时，才允许释放。

### 流水线出库

```sql
update material_stock
set locked_qty = locked_qty - #{outboundQty},
    total_qty = total_qty - #{outboundQty},
    update_time = now()
where id = #{stockId}
  and locked_qty >= #{outboundQty}
  and total_qty >= #{outboundQty}
  and is_del = 0
```

含义：

流水线出库必须先锁库，所以只扣 `locked_qty` 和 `total_qty`，不动 `available_qty`。

### 人工出库

```sql
update material_stock
set available_qty = available_qty - #{outboundQty},
    total_qty = total_qty - #{outboundQty},
    update_time = now()
where id = #{stockId}
  and available_qty >= #{outboundQty}
  and total_qty >= #{outboundQty}
  and is_del = 0
```

含义：

人工出库直接从可用库存扣，所以扣 `available_qty` 和 `total_qty`。

---

## 3.4 基础设施层 Repository 实现

文件：

`s-pay-mall-ddd-infrastructure/src/main/java/cn/bugstack/infrastructure/repository/MaterialStockRepository.java`

新增实现：

```java
@Override
public boolean lockStock(Long stockId, BigDecimal lockQty) {
    if (stockId == null || lockQty == null || lockQty.compareTo(BigDecimal.ZERO) <= 0) {
        return false;
    }
    return materialStockDao.lockStock(stockId, lockQty) == 1;
}
```

其他 3 个方法同理。

作用：

把 DAO 的影响行数转换成领域层更容易理解的 `boolean`。

---

## 3.5 单库存服务改造

文件：

`s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/materialstock/service/MaterialStockService.java`

改造方法：

- `manualOutbound(...)`
- `autoOutbound(...)`
- `lock(...)`
- `release(...)`

改造前这些方法会先查询库存，再在 Java 里判断库存是否足够，最后调用 `updateById(...)`。

改造后改为调用条件更新方法。

例如锁库：

```java
getExistingStockById(id);
BigDecimal lockQty = BigDecimal.valueOf(quantity);
if (!materialStockRepository.lockStock(id, lockQty)) {
    throw new IllegalArgumentException("原料可用库存不足，不能锁定");
}
```

其中 `getExistingStockById(id)` 用于保持原有的“库存记录不存在”提示。

真正防并发的是后面的 `lockStock(...)`。

---

## 3.6 跨库位分配单服务改造

文件：

`s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/materialstockallocation/service/MaterialStockAllocationService.java`

改造方法：

- `lock(String allocationNo)`
- `release(String allocationNo)`
- `autoOutbound(String allocationNo)`

改造前：

```java
MaterialStockAggregate stock = materialStockRepository.queryById(item.getStockId());
BigDecimal availableQty = stock.getAvailableQty();
if (availableQty.compareTo(lockQty) < 0) {
    throw new IllegalArgumentException("原料可用库存不足，不能锁定");
}
stock.setAvailableQty(availableQty.subtract(lockQty));
stock.setLockedQty(lockedQty.add(lockQty));
materialStockRepository.updateById(stock);
```

改造后：

```java
if (!materialStockRepository.lockStock(item.getStockId(), lockQty)) {
    throw new IllegalArgumentException("原料可用库存不足，不能锁定");
}
```

释放和出库同理。

由于 `lock/release/autoOutbound` 方法已有：

```java
@Transactional(rollbackFor = Exception.class)
```

所以当跨库位分配单里某一条库存更新失败时，会抛异常并回滚本次事务，避免只成功一部分。

---

## 4. 为什么这种方案适合当前阶段

当前项目只有一个服务实例，但可能有多个定时任务同时修改库存。

这种情况下不必马上引入 Redis 分布式锁，优先使用：

```text
数据库事务 + 条件更新 SQL
```

原因：

1. 实现难度适中
2. 不需要引入 Redis、锁过期、锁续期、误释放等复杂问题
3. 数据最终落在数据库，数据库条件更新是最后一道防线
4. 对当前 MyBatis + DDD 分层改动较小

---

## 5. 当前还能继续优化的地方

本次改造重点保护的是库存数量扣减。

后续如果要继续增强，可以考虑：

1. 分配单主单状态也使用条件更新
   - 例如 `where id = #{id} and status = 0`
   - 防止同一分配单被重复锁库

2. 跨库位创建分配单时增加锁定或重新校验
   - 当前创建分配单只是生成明细
   - 真正扣库存发生在 `lock`
   - 所以创建后到锁库前，库存仍可能被其他任务消耗

3. 后续多实例部署时再考虑分布式锁
   - 例如 Redis 锁
   - 锁粒度可以按 `material_stock:{stockId}`

---

## 6. 建议验证场景

### 场景1：正常锁库

库存：

```text
available_qty = 100
locked_qty = 0
```

请求锁定 60。

预期：

```text
available_qty = 40
locked_qty = 60
```

### 场景2：库存不足锁库

库存：

```text
available_qty = 50
locked_qty = 0
```

请求锁定 60。

预期：

```text
接口返回：原料可用库存不足，不能锁定
库存数量不变
```

### 场景3：释放锁库

库存：

```text
available_qty = 40
locked_qty = 60
```

请求释放 60。

预期：

```text
available_qty = 100
locked_qty = 0
```

### 场景4：流水线出库

库存：

```text
available_qty = 40
locked_qty = 60
total_qty = 100
```

请求流水线出库 60。

预期：

```text
available_qty = 40
locked_qty = 0
total_qty = 40
```

### 场景5：人工出库

库存：

```text
available_qty = 100
locked_qty = 0
total_qty = 100
```

请求人工出库 30。

预期：

```text
available_qty = 70
locked_qty = 0
total_qty = 70
```

---

## 7. 本次未完成验证

尝试执行：

```bash
mvn test
```

当前本机 Maven 环境报错：

```text
找不到或无法加载主类 org.codehaus.plexus.classworlds.launcher.Launcher
```

因此本次没有完成 Maven 编译验证。需要先修复本机 Maven 环境后再执行测试。
