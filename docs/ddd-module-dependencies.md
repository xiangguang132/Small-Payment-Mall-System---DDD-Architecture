# DDD 模块依赖方向说明

本文档用于约束 `s-pay-mall-ddd-cc` 项目中各模块的依赖方向，避免在编写 `pom.xml` 时出现循环依赖或层次混乱。

## 1. 模块划分

当前项目按 DDD 思路划分为以下模块：

- `s-pay-mall-ddd-types`
- `s-pay-mall-ddd-api`
- `s-pay-mall-ddd-domain`
- `s-pay-mall-ddd-app`
- `s-pay-mall-ddd-infrastruction`
- `s-pay-mall-ddd-trigger`

## 2. 依赖原则

DDD 的核心原则是：

- 依赖只能从外层指向内层
- 内层不能反向依赖外层
- 领域层应尽量保持纯净
- 基础设施层通过实现接口适配外部资源

可以简单理解为：

```text
trigger -> app -> domain
trigger -> infrastruction -> domain
app -> domain
infrastruction -> domain
api -> types
app -> types
domain -> types
infrastruction -> types
trigger -> api / app / infrastruction / types
types -> none
```

## 3. 各模块职责

### 3.1 `s-pay-mall-ddd-types`

公共基础模块，放置所有模块都可能复用的通用内容：

- 通用返回体
- 通用异常
- 通用枚举
- 通用常量
- 通用工具类

建议：

- 不要放业务核心对象
- 不要依赖其他业务模块

### 3.2 `s-pay-mall-ddd-api`

对外契约模块，通常放：

- 请求对象
- 响应对象
- Facade 接口
- DTO / VO

建议：

- 只定义接口和数据结构
- 尽量不要写业务实现逻辑

### 3.3 `s-pay-mall-ddd-domain`

领域层是 DDD 的核心，通常放：

- 实体 `Entity`
- 值对象 `Value Object`
- 聚合根 `Aggregate`
- 领域服务 `Domain Service`
- 仓储接口 `Repository`
- 领域事件

建议：

- 保持纯粹
- 不依赖数据库、RPC、MQ、Web 等技术细节
- 可以依赖 `types`

### 3.4 `s-pay-mall-ddd-app`

应用层负责编排领域对象完成业务流程，通常放：

- 应用服务
- Command / DTO 转换
- 用例编排
- 事务控制
- 调用领域服务

建议：

- 依赖 `domain`
- 不直接依赖基础设施实现类
- 通过领域层定义的接口完成业务协作

### 3.5 `s-pay-mall-ddd-infrastruction`

基础设施层负责具体实现，通常放：

- 仓储实现类
- DAO / Mapper
- Redis / MQ / RPC / 第三方接口适配
- 外部系统集成代码

建议：

- 实现 `domain` 中定义的接口
- 不要把核心业务规则写在这里

### 3.6 `s-pay-mall-ddd-trigger`

入口层，负责接收外部请求，通常放：

- Controller
- RPC 接口
- MQ Consumer
- Job / 定时任务
- 启动类

建议：

- 只做参数接收、校验、转换和调用
- 不承载核心领域逻辑

## 4. 推荐依赖矩阵

下面是这个项目最推荐的 `pom` 依赖关系：

| 模块 | 可依赖模块 |
| --- | --- |
| `types` | 无 |
| `domain` | `types` |
| `app` | `domain`, `types` |
| `infrastruction` | `domain`, `types` |
| `api` | `types` |
| `trigger` | `api`, `app`, `infrastruction`, `types` |

## 5. 写 `pom` 时的判断规则

可以直接按下面规则判断是否应该引入某个模块：

1. 如果是领域对象、领域规则、仓储接口，放 `domain`
2. 如果是业务流程编排，放 `app`
3. 如果是数据库、缓存、消息队列、第三方接口适配，放 `infrastruction`
4. 如果是对外接口契约、请求响应模型，放 `api`
5. 如果是通用基础能力，放 `types`
6. 如果是 Web / RPC / MQ / Job 入口，放 `trigger`

## 6. 最容易出错的地方

### 6.1 `domain` 依赖 `app`

不建议。领域层应该更纯粹，不能反向依赖应用层。

### 6.2 `domain` 依赖 `infrastruction`

不建议。领域层不能知道数据库、Redis、MQ 等具体实现。

### 6.3 `app` 直接依赖实现类

不建议。应用层应依赖抽象接口，由基础设施层负责实现。

### 6.4 `trigger` 里写业务逻辑

不建议。入口层只负责请求进入、参数转换、结果返回。

## 7. 一个简单的记忆方式

可以把模块关系记成：

```text
外层负责接入
中层负责编排
内层负责规则
底层负责实现
```

对应到项目里就是：

```text
trigger -> app -> domain
trigger -> infrastruction -> domain
```

## 8. 建议

后续编写 `pom.xml` 时，优先遵守这条原则：

- 只让上层依赖下层
- 不让实现层反向污染领域层
- 依赖越少，模块越稳定

