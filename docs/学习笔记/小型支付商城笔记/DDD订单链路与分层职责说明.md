# DDD 订单链路与分层职责说明

这份说明对应当前项目里的订单下单链路，重点解释：

- `IOrderService` 和 `OrderService` 是什么关系
- `IOrderRepository` 和 `OrderRepository` 是什么关系
- `IOrderDao` 和 `mapper.xml` 是什么关系
- 整个调用链是怎么从接口层走到数据库的

## 1. 先给结论

针对当前项目，订单链路可以理解为：

`Controller -> IOrderService -> OrderService -> IOrderRepository -> OrderRepository -> IOrderDao -> mapper.xml -> 数据库`

如果只看订单下单主流程，最核心的一条线就是上面这条。

## 2. 各层职责

### 2.1 Controller 层

职责：

- 接收 HTTP 请求
- 解析入参
- 调用应用或领域服务
- 返回结果给前端

在本项目里，入口之一是 `AliPayController`。

### 2.2 Service 层

职责：

- 组织业务流程
- 做业务判断
- 协调多个领域对象或仓储

这里的 `IOrderService` 是服务接口，`OrderService` 是具体实现。

它们的关系是：

- `IOrderService` 负责定义“订单服务有什么能力”
- `OrderService` 负责真正实现这些能力

### 2.3 Repository 层

职责：

- 处理领域对象和持久化对象之间的转换
- 封装数据库访问细节
- 不直接写业务规则

这里的 `IOrderRepository` 是仓储接口，`OrderRepository` 是具体实现。

它们的关系是：

- `IOrderRepository` 负责定义“订单仓储有什么能力”
- `OrderRepository` 负责真正把这些能力落到数据库操作上

### 2.4 DAO 层

职责：

- 提供最底层的数据访问方法
- 方法名通常和 SQL 操作一一对应
- 本身不负责业务编排

这里的 `IOrderDao` 是 MyBatis 的接口。

你可以把它理解成“数据库操作的声明文件”。

### 2.5 Mapper XML

职责：

- 编写具体 SQL
- 把 `IOrderDao` 里的方法和 SQL 绑定起来

所以：

- `IOrderDao` 负责“声明方法”
- `mapper.xml` 负责“真正写 SQL”

这就是你问的那个问题的答案：

`IOrderDao` 是接口，不直接实现数据库逻辑
真正操作数据库的是 `mapper.xml` 里的 SQL

## 3. 调用顺序

以创建订单为例，流程一般是：

1. 前端请求进入 `Controller`
2. `Controller` 调用 `IOrderService.createOrder(...)`
3. `OrderService` 组织下单逻辑
4. `OrderService` 调用 `IOrderRepository`
5. `OrderRepository` 把领域对象转换成数据库对象
6. `OrderRepository` 调用 `IOrderDao`
7. `IOrderDao` 对应的 SQL 在 `mapper.xml` 中执行
8. 数据写入数据库

## 4. 这几个名字不要混

### 4.1 `IOrderService`

接口，只定义能力，不写具体逻辑。

### 4.2 `OrderService`

真正的业务实现类。

### 4.3 `IOrderRepository`

接口，只定义仓储能力。

### 4.4 `OrderRepository`

真正的仓储实现类，负责转换对象并调用 DAO。

### 4.5 `IOrderDao`

MyBatis 接口，只定义数据库方法。

### 4.6 `mapper.xml`

真正写 SQL 的地方。

## 5. 可以这样记

你可以把整个结构记成下面这句话：

`Service` 管业务，`Repository` 管领域对象和数据库对象的转换，`DAO` 和 `XML` 管 SQL。

再压缩一点就是：

- `Service` 负责“怎么做业务”
- `Repository` 负责“怎么把业务数据落到数据库”
- `DAO + XML` 负责“具体怎么执行 SQL”

## 6. 本项目中的一个提醒

当前项目里，`mapper.xml` 的路径在：

- `../../../s-pay-mall-ddd-app/src/main/resources/mybatis/mapper/pay_order_mapper.xml`

而 `IOrderDao` 在：

- `../../../s-pay-mall-ddd-infrastructure/src/main/java/cn/bugstack/infrastructure/dao/IOrderDao.java`

这说明：

- 接口放在基础设施层
- SQL 放在应用资源目录
- 最终由 MyBatis 绑定二者

如果后面你继续看代码，建议你按这条线去追：

1. 先看 `Controller`
2. 再看 `Service`
3. 再看 `Repository`
4. 再看 `DAO`
5. 最后看 `mapper.xml`

这样最不容易乱。
