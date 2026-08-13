# DCCValue 动态配置总结

## 背景

参考项目 `E:\group-buy-market` 中定义了一个动态配置注解 `@DCCValue`，用于让业务配置可以在运行期通过 Redis 动态修改，不需要重启服务。

当前项目 `D:\code\s-pay-mall-ddd-cc` 已按指定范围移植该机制，暂时不包含 `scBlacklist` 渠道黑名单部分。

## 核心机制

`@DCCValue` 是一个字段级注解，使用格式如下：

```java
@DCCValue("downgradeSwitch:0")
private String downgradeSwitch;
```

其中：

- 冒号前是配置 key。
- 冒号后是默认值。
- `DCCValueBeanFactory` 在 Spring Bean 初始化后扫描带注解的字段。
- 配置值来自 Redis，Redis 不存在时写入默认值。
- 配置更新通过 Redis topic 发布消息。
- 收到消息后通过反射更新当前 JVM 中 Bean 字段，实现热更新。

## 已移植模块

```text
s-pay-mall-ddd-types
  cn.bugstack.types.annotations.DCCValue

s-pay-mall-ddd-app
  cn.bugstack.config.DCCValueBeanFactory

s-pay-mall-ddd-infrastructure
  cn.bugstack.infrastructure.dcc.DCCService

s-pay-mall-ddd-api
  cn.bugstack.api.IDCCService

s-pay-mall-ddd-trigger
  cn.bugstack.trigger.http.DCCController

s-pay-mall-ddd-domain
  IGroupBuyActivityRepository 增加降级、切量方法
  SwitchNode 接入降级、切量判断
```

## 接口

接口保持原来的查询参数形式，不接收 JSON body：

```text
POST /api/v1/gbm/dcc/update_config?key=downgradeSwitch&value=1
```

示例：

```bash
curl -X POST "http://localhost:8091/api/v1/gbm/dcc/update_config?key=downgradeSwitch&value=1"
```

## 自定义返回提示

### 开启降级

```text
key=downgradeSwitch
value=1
```

返回：

```json
{
  "code": 200,
  "info": "开启降级，拼团试算将被拦截",
  "data": null
}
```

### 关闭降级

```text
key=downgradeSwitch
value=0
```

返回：

```json
{
  "code": 200,
  "info": "关闭降级，拼团试算正常放行",
  "data": null
}
```

### 无效降级值

```text
key=downgradeSwitch
value=100
```

返回：

```json
{
  "code": 200,
  "info": "downgradeSwitch=100 不是“降级 100%”，而是无效值；未开启降级，拼团试算正常放行",
  "data": null
}
```

### 修改切量范围

```text
key=cutRange
value=50
```

返回：

```json
{
  "code": 200,
  "info": "已修改切量范围为50",
  "data": null
}
```

## 配置语义

### downgradeSwitch

- 只接受 `0` 和 `1`。
- `0` 表示不降级，拼团试算正常放行。
- `1` 表示开启降级，拼团试算被拦截。
- 其他值如 `100` 不会被视为降级，按未开启降级处理。

### cutRange

- 范围是 `0` 到 `100`。
- `100` 表示所有用户放行。
- 数值越小，放行用户比例越小。
- 判断逻辑：

```java
Math.abs(userId.hashCode()) % 100 <= cutRange
```

### scBlacklist

- 按当前要求暂未移植。

## 验证结果

```text
Tests run: 13, Failures: 0, Errors: 0
BUILD SUCCESS
```

