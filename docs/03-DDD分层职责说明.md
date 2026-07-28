# DDD 分层职责说明

本文说明当前仓库里各模块分别负责什么，以及数据/依赖是怎么流动的。

## 1. 模块划分

### `s-pay-mall-ddd-api`

对外接口契约层。

主要放：

- Service 接口
- 请求/响应 DTO
- 统一返回对象

当前例子：

- [IPayService.java](../s-pay-mall-ddd-api/src/main/java/cn/bugstack/api/IPayService.java)
- [CreatePayRequestDTO.java](../s-pay-mall-ddd-api/src/main/java/cn/bugstack/api/dto/CreatePayRequestDTO.java)
- [Response.java](../s-pay-mall-ddd-api/src/main/java/cn/bugstack/api/response/Response.java)

### `s-pay-mall-ddd-domain`

核心业务层。

主要放：

- 领域服务
- 聚合、实体、值对象
- 领域事件
- Repository 和 Port 接口

当前例子：

- [WeixinLoginService.java](../s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/auth/service/WeixinLoginService.java)
- [AbstractOrderService.java](../s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/order/service/AbstractOrderService.java)
- [OrderStatusVO.java](../s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/order/model/valobj/OrderStatusVO.java)
- [CreateOrderAggregate.java](../s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/order/model/aggregate/CreateOrderAggregate.java)

### `s-pay-mall-ddd-infrastructure`

基础设施适配层。

主要放：

- 数据库访问
- 第三方接口调用
- RPC/HTTP 适配器

当前例子：

- [OrderRepository.java](../s-pay-mall-ddd-infrastructure/src/main/java/cn/bugstack/infrastructure/adapter/repository/OrderRepository.java)
- [LoginPort.java](../s-pay-mall-ddd-infrastructure/src/main/java/cn/bugstack/infrastructure/adapter/port/LoginPort.java)
- [ProductRPC.java](../s-pay-mall-ddd-infrastructure/src/main/java/cn/bugstack/infrastructure/gateway/ProductRPC.java)

### `s-pay-mall-ddd-trigger`

触发层。

主要放：

- REST Controller
- 消息监听器
- 定时任务

当前例子：

- [LoginController.java](../s-pay-mall-ddd-trigger/src/main/java/cn.bugstack.trigger/http/LoginController.java)
- [AliPayController.java](../s-pay-mall-ddd-trigger/src/main/java/cn.bugstack.trigger/http/AliPayController.java)
- [NoPayNotifyOrderJob.java](../s-pay-mall-ddd-trigger/src/main/java/cn.bugstack.trigger/job/NoPayNotifyOrderJob.java)

### `s-pay-mall-ddd-types`

公共工具和通用类型。

主要放：

- 异常
- 枚举
- 公共事件基类
- 微信 XML/签名工具

当前例子：

- [AppException.java](../s-pay-mall-ddd-types/src/main/java/cn/bugstack/types/exception/AppException.java)
- [BaseEvent.java](../s-pay-mall-ddd-types/src/main/java/cn/bugstack/types/event/BaseEvent.java)
- [XmlUtil.java](../s-pay-mall-ddd-types/src/main/java/cn/bugstack/types/sdk/weixin/XmlUtil.java)

## 2. 依赖方向

当前仓库的依赖方向基本是：

`trigger -> api -> domain -> infrastructure -> types`

同时：

- `app` 负责 Spring Boot 装配和第三方 Bean 注入
- `domain` 不直接依赖具体数据库实现
- `infrastructure` 负责把领域接口落到外部世界

## 3. 业务流转原则

### 先从接口层进入

Controller 只做参数接收、返回包装和异常兜底，不写核心业务。

### 领域层处理业务规则

比如：

- 下单前是否已有未支付订单
- 登录 ticket 如何映射 openid
- 订单状态如何流转

### 基础设施层负责落地

比如：

- 查 MySQL
- 调微信接口
- 调支付宝接口
- 发事件

## 4. 当前工程的一个现实情况

这个项目是“按链路拆分”的 DDD 示例，但还不是完整的企业级通用分层框架。

特点是：

- 订单和登录链路已经比较完整
- 商品部分目前只有订单链路依赖的查询能力
- 还没有独立的 `product` 后台管理 CRUD 模块

