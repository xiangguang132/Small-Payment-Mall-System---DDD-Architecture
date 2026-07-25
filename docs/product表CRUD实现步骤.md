# product 表 CRUD 实现步骤

本文档基于当前仓库 `s-pay-mall-ddd-cc` 的 DDD 多模块结构，讲清楚如何从零实现一套 `product` 表的增删改查。

你要做的不是“把 SQL 写出来就完事”，而是把整条链路按 DDD 分层拆清楚：

```text
trigger -> app -> domain -> infrastructure -> database
```

如果你只想快速做一个能跑的 CRUD，也可以把层次压缩，但我下面给你的方案是“能长期维护”的写法。

---

## 0. 先说结论

`product` 这套功能建议这样落地：

1. `trigger` 层只负责接收 HTTP 请求、参数校验、返回统一响应
2. `app` 层负责“编排”一次 CRUD 用例
3. `domain` 层负责商品领域对象、业务规则、仓储接口
4. `infrastructure` 层负责数据库访问、MyBatis Mapper、Repository 实现
5. `app/resources/mybatis/mapper` 放 XML
6. `app/resources/application-dev.yml` 的 `mapper-locations` 保持不变

当前项目已经有登录模块的 DDD 实现风格，你会看到它偏向“入口层调用领域服务”。  
但对于 `product` 这种更典型的业务对象，我建议你按标准 DDD 写完整一点，避免以后扩展时返工。

---

## 1. 表设计

你给的字段有：

- `id`
- `name`
- `description`
- `is_del`
- `create_time`
- `update_time`
- `sku`
- `category_id`
- `status`
- `price`

我建议表结构这样设计。

### 1.1 建表 SQL

```sql
CREATE TABLE `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `name` VARCHAR(128) NOT NULL COMMENT '商品名称',
  `description` VARCHAR(512) DEFAULT NULL COMMENT '商品描述',
  `sku` VARCHAR(64) NOT NULL COMMENT '商品SKU编码',
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0下架 1上架',
  `price` DECIMAL(18,2) NOT NULL COMMENT '商品价格',
  `is_del` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0否 1是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sku` (`sku`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status_is_del` (`status`, `is_del`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';
```

### 1.2 字段设计说明

#### `id`

- 主键
- 建议使用 `BIGINT`
- 自增即可，除非你们整个系统有统一 ID 生成策略

#### `name`

- 商品名称
- 不能为空
- 建议长度 `128`

#### `description`

- 商品描述
- 可以为空
- 建议长度 `512`

#### `sku`

- 商品 SKU 编码
- 业务上通常需要唯一
- 所以建议加唯一索引 `uk_sku`

#### `category_id`

- 分类 ID
- 现在可以只存数值
- 如果后续有分类模块，再加外键校验或领域校验

#### `status`

- 商品状态
- 建议先定义为：
  - `0` = 下架
  - `1` = 上架
- 如果你后续想加“草稿 / 审核中 / 售罄”，再扩展枚举

#### `price`

- 商品价格
- 建议使用 `DECIMAL(18,2)`
- 不要用 `FLOAT` 或 `DOUBLE`

#### `is_del`

- 软删除字段
- 建议：
  - `0` = 未删除
  - `1` = 已删除
- 这意味着“删除”不是物理删行，而是更新标记位

#### `create_time` / `update_time`

- 创建时间和更新时间
- 让数据库默认维护，减少应用层漏写

### 1.3 为什么不用硬删除

商品业务很少真的物理删除，原因有三个：

1. 订单、库存、活动可能引用商品
2. 删除后审计不好做
3. 软删除更适合业务回溯

所以本项目的 `delete` 建议做成：

```sql
update product set is_del = 1, update_time = now() where id = ? and is_del = 0
```

---

## 2. 你要怎么分层

这是最重要的部分。  
不要一上来就写 Controller 和 Mapper。先把文件放对位置。

### 2.1 当前项目的模块

仓库里已有模块：

- `s-pay-mall-ddd-api`
- `s-pay-mall-ddd-app`
- `s-pay-mall-ddd-domain`
- `s-pay-mall-ddd-infrastructure`
- `s-pay-mall-ddd-trigger`
- `s-pay-mall-ddd-types`

### 2.2 推荐的 product 目录结构

下面是我建议你直接照着建的目录。

```text
s-pay-mall-ddd-api
└─ src/main/java/cn/bugstack/api/product
   ├─ request
   │  ├─ ProductAddRequest.java
   │  ├─ ProductUpdateRequest.java
   │  ├─ ProductDeleteRequest.java
   │  └─ ProductPageRequest.java
   ├─ response
   │  ├─ ProductDetailResponse.java
   │  └─ ProductPageResponse.java
   └─ IProductService.java

s-pay-mall-ddd-domain
└─ src/main/java/cn/bugstack/domain/product
   ├─ model
   │  ├─ aggregate
   │  │  └─ ProductAggregate.java
   │  ├─ entity
   │  │  └─ ProductEntity.java
   │  └─ vo
   │     ├─ ProductStatusVO.java
   │     └─ DeleteFlagVO.java
   ├─ repository
   │  └─ IProductRepository.java
   └─ service
      ├─ IProductDomainService.java
      └─ ProductDomainService.java

s-pay-mall-ddd-app
└─ src/main/java/cn/bugstack/app/product
   ├─ ProductApplicationService.java
   ├─ command
   │  ├─ ProductAddCommand.java
   │  ├─ ProductUpdateCommand.java
   │  └─ ProductPageQueryCommand.java
   └─ convert
      └─ ProductConvert.java

s-pay-mall-ddd-infrastructure
└─ src/main/java/cn/bugstack/infrastructure/product
   ├─ dao
   │  └─ IProductDao.java
   ├─ po
   │  └─ Product.java
   └─ repository
      └─ ProductRepository.java

s-pay-mall-ddd-app
└─ src/main/resources/mybatis/mapper
   └─ product_mapper.xml

s-pay-mall-ddd-trigger
└─ src/main/java/cn/bugstack/trigger/http
   └─ ProductController.java
```

### 2.3 为什么这样分

#### `api`

放对外契约，也就是请求和响应对象。  
如果以后你要把商品能力暴露给其他系统，`api` 是最合适的承载位置。

#### `domain`

放商品领域本体和业务规则。  
这里不应该出现 SQL、Mapper、Controller、Redis、HTTP 这些技术词。

#### `app`

放应用编排。  
比如：

- 先校验参数
- 再转换命令对象
- 再调用领域服务
- 再组装返回结果

#### `infrastructure`

放数据库和 MyBatis。  
只做“怎么存、怎么查”，不做业务判断。

#### `trigger`

只负责接 HTTP。  
不要在 Controller 里写 SQL，不要在 Controller 里拼复杂业务规则。

---

## 3. 每个文件里写什么

下面按“文件职责”逐个说明。

---

### 3.1 `api` 层

#### 3.1.1 `s-pay-mall-ddd-api/src/main/java/cn/bugstack/api/product/IProductService.java`

这个文件定义商品对外能力。如果你想要一层非常清晰的“接口契约”，就把它放这里。

建议方法：

```java
public interface IProductService {

    Long add(ProductAddRequest request);

    void update(ProductUpdateRequest request);

    void delete(ProductDeleteRequest request);

    ProductDetailResponse queryById(Long id);

    ProductPageResponse queryPage(ProductPageRequest request);
}
```

它的作用：

- 让调用方知道商品模块提供什么能力
- 让参数和返回值先定下来
- 以后如果你想拆成远程接口，改动会小很多

#### 3.1.2 `request/ProductAddRequest.java`

用于新增商品。

字段建议：

- `name`
- `description`
- `sku`
- `categoryId`
- `status`
- `price`

要点：

- 新增请求里不要放 `id`
- 新增时不要让前端传 `is_del`
- `create_time`、`update_time` 也不要让前端传

#### 3.1.3 `request/ProductUpdateRequest.java`

用于修改商品。

字段建议：

- `id`
- `name`
- `description`
- `sku`
- `categoryId`
- `status`
- `price`

要点：

- 修改必须带 `id`
- 修改时也不要让前端传 `is_del`
- `sku` 如果允许改，要额外校验唯一性

#### 3.1.4 `request/ProductDeleteRequest.java`

用于删除商品。

字段建议：

- `id`

如果你想做“批量软删除”，可以把 `id` 改成 `List<Long> ids`，但第一版建议先单删。

#### 3.1.5 `request/ProductPageRequest.java`

用于分页查询。

字段建议：

- `pageNo`
- `pageSize`
- `name`
- `sku`
- `categoryId`
- `status`

要点：

- 列表查询一般都要分页
- 查询条件里不要带 `is_del`
- `is_del = 0` 应该由服务层默认补上

#### 3.1.6 `response/ProductDetailResponse.java`

用于返回商品详情。

字段建议：

- `id`
- `name`
- `description`
- `sku`
- `categoryId`
- `status`
- `price`
- `createTime`
- `updateTime`

#### 3.1.7 `response/ProductPageResponse.java`

用于分页返回。

建议结构：

```java
public class ProductPageResponse {
    private Long total;
    private List<ProductDetailResponse> list;
}
```

如果你后面要标准化分页对象，也可以再提炼一个通用分页返回体。

---

### 3.2 `domain` 层

#### 3.2.1 `model/aggregate/ProductAggregate.java`

聚合根。  
它代表“一个商品”这个业务对象。

建议放这些字段：

- `id`
- `name`
- `description`
- `sku`
- `categoryId`
- `status`
- `price`
- `isDel`
- `createTime`
- `updateTime`

聚合根上可以放一些简单行为，例如：

- `changeStatus(int status)`
- `markDeleted()`
- `changePrice(BigDecimal price)`

这样比把所有逻辑散在 service 里更清楚。

#### 3.2.2 `model/entity/ProductEntity.java`

如果你想把聚合根和实体拆开，可以保留这个类。  
但对于纯 CRUD，很多时候 `Aggregate` 自己就够了。

如果要拆，通常这样理解：

- `Aggregate`：对外承载业务行为
- `Entity`：聚合中的组成部分

因为 `product` 很简单，你可以先不强行拆太细。  
如果你觉得代码量太大，也可以只保留 `ProductAggregate`。

#### 3.2.3 `repository/IProductRepository.java`

领域仓储接口，定义商品的持久化能力。

建议方法：

```java
public interface IProductRepository {

    Long save(ProductAggregate productAggregate);

    void update(ProductAggregate productAggregate);

    void deleteById(Long id);

    ProductAggregate queryById(Long id);

    List<ProductAggregate> queryPage(ProductPageQueryCommand command);
}
```

它的作用：

- 让领域层只知道“仓储能力”，不知道数据库怎么实现
- 以后如果数据库改了，只改基础设施层

#### 3.2.4 `service/IProductDomainService.java`

领域服务接口，放商品领域动作。

建议方法：

```java
public interface IProductDomainService {

    Long add(ProductAggregate productAggregate);

    void update(ProductAggregate productAggregate);

    void delete(Long id);

    ProductAggregate queryById(Long id);
}
```

#### 3.2.5 `service/ProductDomainService.java`

领域服务实现。

这里一般做三件事：

1. 检查业务规则
2. 调用仓储
3. 处理领域行为

例如：

- 新增时校验 `sku` 是否为空
- 修改时校验 `id` 是否存在
- 删除时改 `is_del = 1`
- 如果状态字段有业务限制，可以在这里判断

注意：

- 不要在这里写 SQL
- 不要直接依赖 Mapper
- 不要依赖 Controller 传来的 Web 参数

#### 3.2.6 `model/vo/ProductStatusVO.java`

如果你想把状态做成枚举风格，可以加这个类。

示例：

```java
public enum ProductStatusVO {
    OFFLINE(0, "下架"),
    ONLINE(1, "上架");
}
```

#### 3.2.7 `model/vo/DeleteFlagVO.java`

软删除标记。

示例：

```java
public enum DeleteFlagVO {
    NO(0, "未删除"),
    YES(1, "已删除");
}
```

如果你不想写枚举，也可以直接用常量类，但枚举更适合领域表达。

---

### 3.3 `app` 层

`app` 层是很多人容易写乱的地方。  
你可以把它理解为“用例编排层”。

#### 3.3.1 `ProductApplicationService.java`

这个类是商品 CRUD 的入口编排者。

它负责：

- 接收 `api` 层请求对象
- 转换为领域对象
- 调用 `domain` 层
- 转换返回结果

示例职责：

```java
public class ProductApplicationService implements IProductService {

    public Long add(ProductAddRequest request) { ... }

    public void update(ProductUpdateRequest request) { ... }

    public void delete(ProductDeleteRequest request) { ... }

    public ProductDetailResponse queryById(Long id) { ... }

    public ProductPageResponse queryPage(ProductPageRequest request) { ... }
}
```

如果你后面要加事务，通常就是加在这里。

#### 3.3.2 `command/ProductAddCommand.java`

应用层命令对象。

为什么要有它：

- `request` 是 Web 入参
- `command` 是应用层内部对象
- 两者不要直接混着用

它可以避免你以后 Controller 入参变化时，把领域层一起搞乱。

#### 3.3.3 `command/ProductUpdateCommand.java`

修改命令对象。

字段和 `ProductUpdateRequest` 类似，但这是应用层自己的语义对象。

#### 3.3.4 `command/ProductPageQueryCommand.java`

分页查询命令对象。

建议把分页参数和过滤条件都放这里。

#### 3.3.5 `convert/ProductConvert.java`

对象转换类。

职责：

- `Request -> Command`
- `Command -> Aggregate`
- `Aggregate -> Response`

这里不要写业务逻辑，只做字段搬运。

建议你用静态方法或者 Spring Bean 都可以。  
如果项目不打算引入 MapStruct，手写转换最稳。

---

### 3.4 `infrastructure` 层

这里是真正碰数据库的地方。

#### 3.4.1 `po/Product.java`

数据库持久对象，字段要和表尽量一一对应。

建议字段：

- `id`
- `name`
- `description`
- `sku`
- `categoryId`
- `status`
- `price`
- `isDel`
- `createTime`
- `updateTime`

注意：

- `PO` 是数据库结构
- `Domain Aggregate` 是业务结构
- 两者不要混成一个概念

#### 3.4.2 `dao/IProductDao.java`

MyBatis Mapper 接口。

建议方法：

```java
public interface IProductDao {

    int insert(Product product);

    int update(Product product);

    int deleteById(Long id);

    Product queryById(Long id);

    List<Product> queryPage(ProductQueryParam param);

    Long queryCount(ProductQueryParam param);
}
```

如果你分页用 `LIMIT offset, size`，DAO 里最好拆成“查列表 + 查总数”两个方法。

#### 3.4.3 `repository/ProductRepository.java`

基础设施层仓储实现。

职责：

- 把 `domain` 的仓储接口实现掉
- 在 `PO` 和 `Aggregate` 之间转换
- 调用 `DAO`

它不应该：

- 包含复杂业务规则
- 做 HTTP 参数校验
- 直接返回 Web 响应对象

#### 3.4.4 `product_mapper.xml`

这是 MyBatis SQL 文件。

放置路径：

```text
s-pay-mall-ddd-app/src/main/resources/mybatis/mapper/product_mapper.xml
```

因为当前项目的 `application-dev.yml` 已经配置了：

```yaml
mybatis:
  mapper-locations: classpath:/mybatis/mapper/*.xml
  config-location: classpath:/mybatis/config/mybatis-config.xml
```

所以你只要把 XML 放到这个目录下，MyBatis 就能扫到。

XML 里一般写：

- `insert`
- `update`
- `delete`
- `select`
- `resultMap`

---

### 3.5 `trigger` 层

#### 3.5.1 `ProductController.java`

HTTP 入口。

建议接口：

- `POST /api/v1/product/add`
- `POST /api/v1/product/update`
- `POST /api/v1/product/delete`
- `GET /api/v1/product/detail?id=1`
- `GET /api/v1/product/page?pageNo=1&pageSize=10`

Controller 里只做：

- 接参
- 调用应用层
- 返回 `Response`

不要做：

- SQL
- 复杂业务规则
- 持久化逻辑

---

## 4. 文件内容应该怎么写

这一部分是“真正照着写”的核心。

---

### 4.1 `api` 层怎么写

#### 4.1.1 `ProductAddRequest`

典型字段：

```java
private String name;
private String description;
private String sku;
private Long categoryId;
private Integer status;
private BigDecimal price;
```

建议加的校验：

- `name` 不能为空
- `sku` 不能为空
- `categoryId` 不能为空
- `price` 不能为空且不能小于 0

#### 4.1.2 `ProductUpdateRequest`

要比新增多一个 `id`：

```java
private Long id;
```

其他字段同新增。

#### 4.1.3 `ProductDeleteRequest`

最小版本只要：

```java
private Long id;
```

#### 4.1.4 `ProductPageRequest`

分页参数建议统一：

```java
private Integer pageNo;
private Integer pageSize;
private String name;
private String sku;
private Long categoryId;
private Integer status;
```

建议默认值：

- `pageNo = 1`
- `pageSize = 10`

#### 4.1.5 `ProductDetailResponse`

返回字段应包含：

- 基础信息
- 状态
- 价格
- 时间信息

`is_del` 一般不需要返回给前端。

---

### 4.2 `domain` 层怎么写

#### 4.2.1 领域对象要表达什么

商品领域对象要表达的不是“数据库长什么样”，而是“商品在业务上的状态是什么”。

所以领域对象建议有如下方法：

```java
public void changeName(String name)
public void changeSku(String sku)
public void changePrice(BigDecimal price)
public void changeStatus(Integer status)
public void markDeleted()
```

这样以后如果 `price` 改动需要做范围限制，规则可以收进去。

#### 4.2.2 领域服务要做什么

领域服务的职责是处理“单个实体之外的业务动作”。

对于商品 CRUD，领域服务里常见逻辑：

- 新增前检查 `sku` 是否已存在
- 修改时检查商品是否存在
- 删除时改软删除标识
- 查询时过滤 `is_del = 0`

如果只是简单 demo，校验可以先少写一点，但接口位置要先保留好。

#### 4.2.3 仓储接口要做什么

仓储接口只关心：

- 保存
- 修改
- 删除
- 按 ID 查
- 分页查

不要把 SQL 细节暴露到领域服务里。

---

### 4.3 `app` 层怎么写

应用层是这套流程最关键的“缝合层”。

#### 4.3.1 新增流程

`ProductApplicationService.add(...)` 建议按这个顺序：

1. 接收 `ProductAddRequest`
2. 校验参数
3. 转成 `ProductAddCommand`
4. 转成 `ProductAggregate`
5. 调用领域服务保存
6. 返回新增后的 `id`

#### 4.3.2 修改流程

1. 接收 `ProductUpdateRequest`
2. 校验 `id`
3. 查是否存在
4. 转成聚合对象
5. 调用领域服务更新

#### 4.3.3 删除流程

1. 接收 `id`
2. 校验 `id`
3. 调用领域服务软删除
4. 不返回复杂结果

#### 4.3.4 详情查询流程

1. 接收 `id`
2. 调用领域服务查商品
3. 转成 `ProductDetailResponse`
4. 返回

#### 4.3.5 分页查询流程

1. 接收分页参数
2. 拼查询条件
3. 查总数
4. 查列表
5. 封装分页返回

---

### 4.4 `infrastructure` 层怎么写

#### 4.4.1 `Product` PO

PO 就是数据库一行数据的 Java 映射。

字段名通常用驼峰：

- `categoryId`
- `createTime`
- `updateTime`

和表字段的下划线映射交给 MyBatis。

#### 4.4.2 `IProductDao`

DAO 方法建议和 SQL 一一对应。

如果你的项目需要可维护性，建议分页查询拆成两个方法：

```java
Long queryCount(ProductQueryParam param);
List<Product> queryPage(ProductQueryParam param);
```

这样不会把“总数”和“列表”混在一个 SQL 里。

#### 4.4.3 `ProductRepository`

Repository 的核心任务是转换。

典型转换流程：

```text
ProductAggregate  ->  Product PO  ->  DAO
DAO 返回 Product PO  ->  转回 ProductAggregate
```

这是 DDD 里非常重要的一层边界。

#### 4.4.4 `product_mapper.xml`

建议包含下面这些 SQL：

```xml
<resultMap id="productMap" type="cn.bugstack.infrastructure.product.po.Product">
    <id column="id" property="id"/>
    <result column="name" property="name"/>
    <result column="description" property="description"/>
    <result column="sku" property="sku"/>
    <result column="category_id" property="categoryId"/>
    <result column="status" property="status"/>
    <result column="price" property="price"/>
    <result column="is_del" property="isDel"/>
    <result column="create_time" property="createTime"/>
    <result column="update_time" property="updateTime"/>
</resultMap>
```

你至少要写四类语句：

1. `insert`
2. `update`
3. `select by id`
4. `select page`

如果是软删除，再加一个：

5. `update is_del = 1`

---

### 4.5 `trigger` 层怎么写

参考当前项目登录控制器的风格，商品控制器可以这样组织：

- `@RestController`
- `@CrossOrigin("*")`
- `@RequestMapping("/api/v1/product")`

方法命名尽量和业务一致：

- `add`
- `update`
- `delete`
- `detail`
- `page`

统一返回结构可以直接复用当前项目的：

```java
Response<T>
```

错误码继续用：

- `SUCCESS`
- `UN_ERROR`
- `ILLEGAL_PARAMETER`
- `NO_LOGIN`

对于商品 CRUD，一般最常用的是：

- `SUCCESS`
- `UN_ERROR`
- `ILLEGAL_PARAMETER`

---

## 5. 一条完整 CRUD 的数据流

你可以把整条链路理解成下面这样。

### 5.1 新增商品

```text
前端
  -> ProductController.add
  -> ProductApplicationService.add
  -> ProductDomainService.add
  -> ProductRepository.save
  -> IProductDao.insert
  -> product_mapper.xml insert SQL
  -> MySQL product 表
```

### 5.2 修改商品

```text
前端
  -> ProductController.update
  -> ProductApplicationService.update
  -> ProductDomainService.update
  -> ProductRepository.update
  -> IProductDao.update
  -> MySQL product 表
```

### 5.3 删除商品

因为是软删除，所以实际是：

```text
前端
  -> ProductController.delete
  -> ProductApplicationService.delete
  -> ProductDomainService.delete
  -> ProductRepository.deleteById
  -> IProductDao.deleteById
  -> update product set is_del = 1
```

### 5.4 查询详情

```text
前端
  -> ProductController.detail
  -> ProductApplicationService.queryById
  -> ProductDomainService.queryById
  -> ProductRepository.queryById
  -> IProductDao.queryById
```

### 5.5 分页查询

```text
前端
  -> ProductController.page
  -> ProductApplicationService.queryPage
  -> ProductDomainService.queryPage
  -> ProductRepository.queryPage
  -> IProductDao.queryCount + IProductDao.queryPage
```

---

## 6. 你在当前项目里具体要新建哪些文件

下面我按模块列一个“落地清单”。

### 6.1 `s-pay-mall-ddd-api`

建议新建：

- `src/main/java/cn/bugstack/api/product/IProductService.java`
- `src/main/java/cn/bugstack/api/product/request/ProductAddRequest.java`
- `src/main/java/cn/bugstack/api/product/request/ProductUpdateRequest.java`
- `src/main/java/cn/bugstack/api/product/request/ProductDeleteRequest.java`
- `src/main/java/cn/bugstack/api/product/request/ProductPageRequest.java`
- `src/main/java/cn/bugstack/api/product/response/ProductDetailResponse.java`
- `src/main/java/cn/bugstack/api/product/response/ProductPageResponse.java`

### 6.2 `s-pay-mall-ddd-domain`

建议新建：

- `src/main/java/cn/bugstack/domain/product/model/aggregate/ProductAggregate.java`
- `src/main/java/cn/bugstack/domain/product/model/entity/ProductEntity.java`
- `src/main/java/cn/bugstack/domain/product/model/vo/ProductStatusVO.java`
- `src/main/java/cn/bugstack/domain/product/model/vo/DeleteFlagVO.java`
- `src/main/java/cn/bugstack/domain/product/repository/IProductRepository.java`
- `src/main/java/cn/bugstack/domain/product/service/IProductDomainService.java`
- `src/main/java/cn/bugstack/domain/product/service/ProductDomainService.java`

### 6.3 `s-pay-mall-ddd-app`

建议新建：

- `src/main/java/cn/bugstack/app/product/ProductApplicationService.java`
- `src/main/java/cn/bugstack/app/product/command/ProductAddCommand.java`
- `src/main/java/cn/bugstack/app/product/command/ProductUpdateCommand.java`
- `src/main/java/cn/bugstack/app/product/command/ProductPageQueryCommand.java`
- `src/main/java/cn/bugstack/app/product/convert/ProductConvert.java`
- `src/main/resources/mybatis/mapper/product_mapper.xml`

### 6.4 `s-pay-mall-ddd-infrastructure`

建议新建：

- `src/main/java/cn/bugstack/infrastructure/product/po/Product.java`
- `src/main/java/cn/bugstack/infrastructure/product/dao/IProductDao.java`
- `src/main/java/cn/bugstack/infrastructure/product/repository/ProductRepository.java`

### 6.5 `s-pay-mall-ddd-trigger`

建议新建：

- `src/main/java/cn/bugstack/trigger/http/ProductController.java`

---

## 7. 必须注意的配置点

### 7.1 MyBatis XML 放哪里

当前项目的配置是：

```yaml
mybatis:
  mapper-locations: classpath:/mybatis/mapper/*.xml
  config-location: classpath:/mybatis/config/mybatis-config.xml
```

所以 `product_mapper.xml` 必须放到：

```text
s-pay-mall-ddd-app/src/main/resources/mybatis/mapper/
```

不要放错到 `infrastructure` 的 Java 包下。  
MyBatis 扫描的是 resources 路径，不是 Java 包路径。

### 7.2 DAO 接口如何让 MyBatis 识别

当前仓库里还没看到统一的 `@MapperScan` 配置，所以你在新增 `IProductDao` 时，需要二选一：

1. 在 DAO 接口上加 `@Mapper`
2. 在启动类或配置类上加 `@MapperScan`

更推荐第二种，因为更干净。

### 7.3 软删除查询条件

所有查询都应该默认带：

```sql
is_del = 0
```

否则你删掉的数据还会被查出来。

### 7.4 价格字段不要用浮点数

一定用：

```sql
DECIMAL(18,2)
```

不要用 `FLOAT` 或 `DOUBLE`，否则金额会出现精度问题。

### 7.5 SKU 必须考虑唯一性

如果 SKU 是业务编码，通常应该唯一。  
所以新增和修改时都要考虑：

1. 插入前查重
2. 修改时避免改成别的商品已存在的 SKU

---

## 8. 推荐的实现顺序

不要按“先 Controller，后 SQL”的顺序写。  
建议按下面步骤做。

### 第 1 步：先建表

先把 `product` 表建出来，字段和索引先定死。

### 第 2 步：先建 `api`

先定义请求和响应对象，接口边界先统一。

### 第 3 步：再建 `domain`

先把商品聚合、仓储接口、领域服务签出来。

### 第 4 步：再建 `app`

写应用编排和对象转换。

### 第 5 步：再建 `infrastructure`

写 PO、DAO、Repository、Mapper XML。

### 第 6 步：最后写 `trigger`

把 HTTP 接口露出来，接通前后端。

这个顺序的好处是：

- 先定边界，后写实现
- 避免层级打架
- 后续改动时只会动一层，不会层层回滚

---

## 9. 一套最小可运行版本应该长什么样

如果你现在只想先跑通第一版，建议做到下面这些就够了：

1. `product` 表建好
2. `ProductController` 提供 5 个接口
3. `ProductApplicationService` 负责编排
4. `ProductDomainService` 负责 CRUD
5. `ProductRepository` 负责转调 DAO
6. `IProductDao + product_mapper.xml` 负责数据库
7. `is_del` 做软删除
8. `sku` 做唯一索引

第一版不要急着做：

- 商品图片
- 商品规格
- 商品属性
- 库存
- SPU / SKU 拆分
- 类目树

那些都属于第二阶段扩展。

---

## 10. 最容易写错的地方

### 10.1 把 SQL 写进 Controller

不对。  
Controller 只负责接入层，不负责持久化。

### 10.2 把 MyBatis XML 放错目录

不对。  
XML 必须放进 `app/resources/mybatis/mapper` 才能被当前配置加载。

### 10.3 查询时忘记过滤软删除

不对。  
所有查询都要默认加 `is_del = 0`。

### 10.4 价格用浮点类型

不对。  
金额字段必须用 `DECIMAL`。

### 10.5 修改接口不带 `id`

不对。  
修改必须能定位到唯一记录。

### 10.6 SKU 不做唯一校验

不对。  
SKU 一般是业务唯一标识。

---

## 11. 你可以直接照着走的开发清单

### 数据库

- [ ] 创建 `product` 表
- [ ] 给 `sku` 加唯一索引
- [ ] 给 `category_id`、`status`、`is_del` 加合适索引

### `api`

- [ ] 建 `ProductAddRequest`
- [ ] 建 `ProductUpdateRequest`
- [ ] 建 `ProductDeleteRequest`
- [ ] 建 `ProductPageRequest`
- [ ] 建 `ProductDetailResponse`
- [ ] 建 `ProductPageResponse`
- [ ] 建 `IProductService`

### `domain`

- [ ] 建 `ProductAggregate`
- [ ] 建 `IProductRepository`
- [ ] 建 `IProductDomainService`
- [ ] 建 `ProductDomainService`

### `app`

- [ ] 建 `ProductApplicationService`
- [ ] 建 `ProductConvert`
- [ ] 建 command 对象

### `infrastructure`

- [ ] 建 `Product` PO
- [ ] 建 `IProductDao`
- [ ] 建 `ProductRepository`
- [ ] 建 `product_mapper.xml`

### `trigger`

- [ ] 建 `ProductController`
- [ ] 接通新增接口
- [ ] 接通修改接口
- [ ] 接通删除接口
- [ ] 接通详情接口
- [ ] 接通分页接口

---

## 12. 最后给你的建议

如果你是第一次在这个项目里做商品模块，我建议你严格按这个顺序写：

1. 先建表
2. 先定 `api` 请求/响应
3. 再写 `domain` 聚合和仓储接口
4. 再写 `app` 编排
5. 再写 `infrastructure` 的 MyBatis
6. 最后写 `trigger`

这样做的结果是：

- 每一层职责都清楚
- 以后加“上下架、库存、分类、图片、规格”时不会乱
- 你会真正理解 DDD 为什么要分层

如果你愿意，我下一步可以继续帮你补一份：

1. `product` 模块的**完整 Java 代码骨架**
2. `product_mapper.xml` 的**完整 SQL 示例**
3. `ProductController` 的**完整接口代码**
4. `ProductApplicationService` 和 `ProductRepository` 的**完整实现**

