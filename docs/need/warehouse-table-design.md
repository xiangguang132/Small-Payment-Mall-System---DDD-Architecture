# warehouse 表设计

这张表是供应链/ERP式后台管理的第一张基础表。  
它的作用是承接后续的库存、入库、出库、调拨、发货等业务。

## 为什么先做它

- `inventory` 需要明确库存属于哪个仓库
- 后续做采购、入库、出库时，必须有仓库维度
- 先有仓库，再做库存和流水，数据模型更稳定

## 建表建议

已落在：

- [`data/sql/warehouse.sql`](../../data/sql/warehouse.sql)

## 字段说明

- `warehouse_code`：仓库唯一编码，建议业务侧生成并唯一约束
- `name`：仓库名称
- `type`：仓库类型，当前先区分自营仓和第三方仓
- `address`：仓库地址
- `contact_name` / `contact_phone`：仓库联系人信息
- `status`：启用/禁用
- `is_del`：软删除标记

## 为什么这样设计

- 保留 `warehouse_code`，便于外部系统对接
- 保留 `status` 和 `is_del`，与当前项目其他主数据保持一致
- 不提前加太多复杂字段，避免第一版就做成重型 ERP

## 下一张最建议补的表

下一步优先补：

1. `inventory`
2. `inventory_log`

如果你后面要做采购闭环，再补：

1. `purchase_order`
2. `purchase_order_item`

