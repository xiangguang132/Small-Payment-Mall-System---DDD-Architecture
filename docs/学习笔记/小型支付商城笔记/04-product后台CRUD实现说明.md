# product 后台 CRUD 实现说明

这份文档只回答一件事：在当前仓库里，怎么补一个最基本的 `product` 后台管理 CRUD。

## 1. 先回答你的问题

对，当前仓库里没有完整的商品后台管理。

现有的 `product` 相关能力只有：

- 订单下单时通过 `ProductPort` 查商品信息
- `ProductRPC` 只是模拟一个商品查询返回值

也就是说，当前没有：

- `product` 表的 DAO
- `product` 的后台 Controller
- `product` 的增删改查服务
- `product` 的持久化 Repository

对应现状可见：

- [ProductPort.java](../../../s-pay-mall-ddd-infrastructure/src/main/java/cn/bugstack/infrastructure/adapter/port/ProductPort.java)
- [ProductRPC.java](../../../s-pay-mall-ddd-infrastructure/src/main/java/cn/bugstack/infrastructure/gateway/ProductRPC.java)

## 2. 你要做的最小版本

建议先做这 5 个接口：

- 新增商品
- 修改商品
- 删除商品
- 商品详情
- 商品分页列表

如果你想“最基本”，这已经够用了。

## 3. 推荐落点

按当前工程风格，建议仍然分 4 层写：

### 3.1 `api`

放对外接口定义和请求/响应对象。

建议新增：

- `IProductService`
- `ProductAddRequest`
- `ProductUpdateRequest`
- `ProductDeleteRequest`
- `ProductDetailResponse`
- `ProductPageRequest`
- `ProductPageResponse`

### 3.2 `trigger`

放 `ProductController`。

Controller 只做：

- 接收参数
- 调应用服务
- 返回统一 `Response`

### 3.3 `domain`

放商品领域模型和业务规则。

建议新增：

- `ProductEntity`
- `ProductAggregate`
- `ProductStatusVO`
- `IProductRepository`
- `IProductDomainService`
- `ProductDomainService`

### 3.4 `infrastructure`

放数据库映射和仓储实现。

建议新增：

- `Product` PO
- `IProductDao`
- `ProductRepository`
- `product_mapper.xml`

## 4. 表设计建议

如果你要做后台 CRUD，建议至少有这些字段：

- `id`
- `product_name`
- `product_desc`
- `price`
- `status`
- `stock`
- `create_time`
- `update_time`
- `is_del`

最小可用版本里，`status` 和 `is_del` 很重要。

## 5. 推荐接口设计

### 新增

- `POST /api/v1/product/add`

### 修改

- `POST /api/v1/product/update`

### 删除

- `POST /api/v1/product/delete`

### 详情

- `GET /api/v1/product/detail?id=1`

### 分页

- `GET /api/v1/product/page?pageNo=1&pageSize=10`

## 6. 推荐实现顺序

先按这个顺序写，最稳：

1. 建 `product` 表
2. 建 `api` 请求/响应对象
3. 建 `ProductController`
4. 建 `ProductApplicationService`
5. 建 `domain` 的聚合、实体、领域服务、仓储接口
6. 建 `infrastructure` 的 PO、DAO、Repository、mapper
7. 跑通新增和查询，再补修改和删除

## 7. 目录建议

如果保持当前仓库的风格，建议新增这些路径：

```text
s-pay-mall-ddd-api/src/main/java/cn/bugstack/api/product
s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/product
s-pay-mall-ddd-infrastructure/src/main/java/cn/bugstack/infrastructure/product
s-pay-mall-ddd-trigger/src/main/java/cn/bugstack/trigger/http/ProductController.java
s-pay-mall-ddd-app/src/main/resources/mybatis/mapper/product_mapper.xml
```

## 8. 具体写法建议

### 8.1 Controller

Controller 里不要直接写 SQL，也不要直接操作 PO。

调用链建议是：

`ProductController -> IProductService -> ProductApplicationService -> ProductDomainService -> ProductRepository -> IProductDao`

### 8.2 应用层

应用层负责：

- 参数转换
- 组装命令对象
- 调度领域服务
- 返回响应对象

### 8.3 领域层

领域层负责：

- 商品状态校验
- 删除状态控制
- 价格、库存等业务规则

如果你后面还要扩展上架、下架、库存扣减，就一定要把这些规则留在领域层。

### 8.4 基础设施层

基础设施层负责：

- MyBatis CRUD
- PO 和领域对象互转
- 分页 SQL

## 9. 最小实现清单

如果你现在就准备开写，最少要补这些文件：

- `ProductController`
- `IProductService`
- `ProductApplicationService`
- `ProductAddRequest`
- `ProductUpdateRequest`
- `ProductDeleteRequest`
- `ProductDetailResponse`
- `ProductPageRequest`
- `ProductPageResponse`
- `ProductAggregate`
- `ProductEntity`
- `IProductRepository`
- `IProductDomainService`
- `ProductDomainService`
- `Product`
- `IProductDao`
- `ProductRepository`
- `product_mapper.xml`

## 10. 和当前支付链路的关系

当前支付链路依赖的是 `ProductPort -> ProductRPC` 模拟商品信息。

如果你做了真正的商品后台管理，后续可以把这条链路改成：

- 订单下单时直接查商品库
- 或者保留 `ProductPort`，但内部改成查数据库

这样支付链路和商品后台可以逐步收敛到同一份商品数据源。

