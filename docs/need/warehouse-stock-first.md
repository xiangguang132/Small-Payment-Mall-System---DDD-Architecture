# 先做 `warehouse_stock` 的原因

如果你现在已经完成了 `product / product_type / supplier / warehouse` 的增删改查，下一张最值得做的表不是采购单，也不是出库单，而是 `warehouse_stock`。

它的价值很直接：

- 把 `product` 和 `warehouse` 真正连接起来
- 让“库存”从概念变成可查询、可约束的数据
- 后续做采购入库、销售出库、库存预警，都会用到它

## 1. 为什么先做这张表

你当前已有的四张表，都是主数据。

- `product` 解决“卖什么”
- `product_type` 解决“属于什么类目”
- `supplier` 解决“从谁进货”
- `warehouse` 解决“放在哪里”

但 ERP 供应链真正跑起来，靠的是库存状态。  
没有库存表，后面的采购、出库、调拨都只能停留在单据层，不能形成闭环。

所以第一张交易型表建议是：

- `warehouse_stock`

## 2. 这张表解决什么问题

`warehouse_stock` 负责描述“某仓库里的某商品当前有多少库存”。

它至少要回答三个问题：

- 这个商品在哪个仓库
- 当前可用库存是多少
- 是否存在占用库存

它是库存中心的基础表，不是流水表。  
流水以后可以再加，但第一步先把“当前库存”做对。

## 3. 推荐表结构

建议放在：

- `data/sql/warehouse_stock.sql`

推荐字段如下：

```sql
CREATE TABLE `warehouse_stock` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `warehouse_id` BIGINT NOT NULL COMMENT '仓库ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `available_qty` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '可用库存',
  `locked_qty` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '锁定库存',
  `total_qty` DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '总库存',
  `is_del` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0否 1是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_product` (`warehouse_id`, `product_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_warehouse_id` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库库存表';
```

## 4. 字段说明

- `warehouse_id`：库存归属仓库
- `product_id`：库存归属商品
- `available_qty`：当前可直接使用的库存
- `locked_qty`：已预留、已锁定但未真正出库的库存
- `total_qty`：总库存，通常等于 `available_qty + locked_qty`
- `is_del`：软删除
- `create_time` / `update_time`：审计字段

## 5. 为什么用这几个库存字段

这一版不建议一上来就搞批次、批号、库位、效期。

先保留最小可用字段，原因是：

- 个人练习项目更关注链路，而不是仓储细节
- 你需要先跑通入库、出库、库存变动
- 后续如果要扩展批次库存、库位库存，可以在这张表之上继续拆

其中最关键的是：

- `available_qty`
- `locked_qty`
- `total_qty`

如果你只想做极简版，也可以先只保留 `total_qty`。  
但从 ERP 思维看，`locked_qty` 很值得一开始就留。

## 6. 后续业务怎么接

这张表建完后，后续建议按下面顺序补：

1. `inventory_log`
2. `purchase_order`
3. `purchase_order_item`
4. `inbound_order`
5. `outbound_order`

其中：

- `inventory_log` 记录每一次库存变化
- `purchase_order` 负责采购单据
- `inbound_order` 负责采购入库
- `outbound_order` 负责销售出库

最小闭环可以理解为：

`supplier + product + warehouse -> warehouse_stock -> purchase_order -> inbound_order -> outbound_order`

## 7. 在当前项目里的落点

按你现在的 DDD 分层，建议新增这些文件：

### API 层

- `s-pay-mall-ddd-api/src/main/java/cn/bugstack/api/request/stock/StockAdjustRequest.java`
- `s-pay-mall-ddd-api/src/main/java/cn/bugstack/api/response/stock/StockDetailResponse.java`

### Domain 层

- `s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/stock/model/aggregate/StockAggregate.java`
- `s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/stock/repository/IStockRepository.java`
- `s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/stock/service/IStockService.java`
- `s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/stock/service/StockService.java`

### Infrastructure 层

- `s-pay-mall-ddd-infrastructure/src/main/java/cn/bugstack/infrastructure/dao/po/WarehouseStock.java`
- `s-pay-mall-ddd-infrastructure/src/main/java/cn/bugstack/infrastructure/dao/IWarehouseStockDao.java`
- `s-pay-mall-ddd-infrastructure/src/main/java/cn/bugstack/infrastructure/repository/WarehouseStockRepository.java`

### Trigger 层

- `s-pay-mall-ddd-trigger/src/main/java/cn/bugstack/trigger/http/StockController.java`
- `s-pay-mall-ddd-trigger/src/main/java/cn/bugstack/trigger/assembler/StockAssembler.java`

## 8. 第一版先做什么接口

第一版不要做太多接口，建议只做这四个：

- 新增库存记录
- 库存调整
- 库存详情查询
- 仓库商品库存查询

如果你想更贴近真实业务，库存调整接口可以拆成：

- 入库加库存
- 出库减库存
- 锁库
- 解锁

但个人练习项目第一版，先做“加减库存”就够了。

## 9. 推荐的实现原则

- 库存不能为负
- 同一个 `warehouse_id + product_id` 只能有一条库存记录
- 每次库存变动都应该保留日志
- 不要让 Controller 直接改库存字段
- 库存变动必须经过领域服务

## 10. 结论

如果你现在只能再加一张表，优先级最高的是：

1. `warehouse_stock`

它能最快把你现在的主数据，推进到“可操作的供应链闭环”。

