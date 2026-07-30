# 商品和商品分类接口逻辑审计

本文基于当前仓库中的商品与商品分类实现，整理现有接口的逻辑漏洞、数据风险和建议补强点。

## 结论

当前接口已经具备基础的登录保护和部分业务约束，但还没有达到完整的后台 CRUD 闭环。主要问题集中在以下几类：

- 分类删除约束不完整
- 新增和更新缺少字段级校验
- 分类树关系缺少完整性校验
- 上下架接口采用 toggle 语义，存在并发和误操作风险

## 已经做对的地方

- 商品和分类接口已经接入 `AuthInterceptor`，并通过 `WebMvcConfig` 统一拦截 `/api/v1/**`
- 商品新增时会校验分类是否存在且处于启用状态
- 商品分类删除时，至少会阻止“启用中的分类被商品引用后删除”
- 全局异常处理已经接入，`IllegalArgumentException` 会返回 422

## 发现的问题

### 1. 分类删除的保护条件不完整

当前逻辑只在分类处于启用状态时，才会检查是否被商品引用。

这会带来一个明显漏洞：

- 如果分类已经禁用
- 但仍然有商品引用这个分类
- 该分类仍然可以被删除

这样会留下“商品指向不存在分类”的脏数据。

另外，当前删除逻辑没有处理父子分类关系：

- 删除父分类前，没有检查是否存在子分类
- 删除后会破坏分类树结构

### 2. 更新接口复用新增请求对象，约束太松

`ProductController.update()` 直接使用 `ProductAddRequest` 做更新入参，这会导致：

- 新增和更新共用同一套宽松字段
- 很难表达“部分更新”和“字段必填”之间的差异
- 空字符串、非法价格、非法状态值都可能进入业务层

当前 `ProductAssembler` 和 `ProductTypeAssembler` 只做了 `trim`，没有做更强的输入校验。

### 3. 状态值缺少显式校验

商品状态虽然定义了 `ProductStatusVO`，但没有在入参层真正校验。

风险包括：

- 传入 `2`、`3` 这种非法状态值
- 数据库里出现业务无法解释的状态
- 下游展示和订单逻辑产生分支错误

商品分类也存在同类问题。

### 4. 分类新增缺少父分类存在性校验

`ProductTypeRepository.save()` 对 `parentId` 只是做了默认值处理，没有校验：

- 父分类是否存在
- 父分类是否已删除
- 父分类是否处于禁用状态

这会导致分类树可能出现断链或脏引用。

### 5. 上下架接口是翻转状态，不是显式设置状态

当前 `onSale()` 的实现本质是“状态翻转”：

- 当前是 `1`，就改成 `0`
- 其他情况就改成 `1`

这类设计的缺点是：

- 连点两次会反复切换
- 并发请求容易产生竞态
- API 语义不清晰

更稳妥的方式是提供显式接口，例如：

- `setStatus(id, 0)`
- `setStatus(id, 1)`

### 6. 缺少唯一性和幂等性保护

当前代码中可以看到：

- 分类表有 `type_code` 唯一索引
- 但业务层没有提前做重复校验

如果重复提交，会直接把数据库异常抛给前端，不够友好。

商品侧也建议补充：

- `sku` 唯一性校验
- 商品名称和分类编码的重复处理

## 建议补强项

### 1. 补输入校验

建议在 API 层为请求对象加上校验注解，例如：

- `@NotBlank`
- `@NotNull`
- `@Positive`
- `@Size`

并在 Controller 中启用 `@Validated`。

### 2. 拆分新增和更新请求

建议把请求对象拆成：

- `ProductAddRequest`
- `ProductUpdateRequest`
- `ProductTypeAddRequest`
- `ProductTypeUpdateRequest`

这样可以更准确表达字段必填规则。

### 3. 补删除前置校验

商品分类删除建议增加：

- 是否存在子分类
- 是否被任意商品引用
- 是否存在未下线的关联数据

### 4. 将上下架改成显式状态修改

建议改成：

- `PUT /status/{id}` 只接收目标状态
- 而不是 toggle

这样更适合后台管理场景，也更利于审计。

### 5. 增加服务层幂等和唯一性判断

建议在落库前先查重：

- 分类编码是否已存在
- 商品 SKU 是否已存在
- 关键字段是否为空

这样可以把异常前置成业务错误，而不是数据库错误。

## 需要优先修的点

如果只做最小修复，建议优先顺序如下：

1. 分类删除要同时校验商品引用和子分类
2. 新增/更新请求补强校验
3. 分类新增补父分类存在性检查
4. 上下架从 toggle 改成显式设置状态
5. 补唯一性校验

## 相关代码位置

- [`ProductController.java`](../../s-pay-mall-ddd-trigger/src/main/java/cn/bugstack/trigger/http/ProductController.java)
- [`ProductTypeController.java`](../../s-pay-mall-ddd-trigger/src/main/java/cn/bugstack/trigger/http/ProductTypeController.java)
- [`ProductService.java`](../../s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/product/service/ProductService.java)
- [`ProductTypeService.java`](../../s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/producttype/service/ProductTypeService.java)
- [`ProductRepository.java`](../../s-pay-mall-ddd-infrastructure/src/main/java/cn/bugstack/infrastructure/repository/ProductRepository.java)
- [`ProductTypeRepository.java`](../../s-pay-mall-ddd-infrastructure/src/main/java/cn/bugstack/infrastructure/repository/ProductTypeRepository.java)
- [`WebMvcConfig.java`](../../s-pay-mall-ddd-app/src/main/java/cn/bugstack/config/WebMvcConfig.java)

