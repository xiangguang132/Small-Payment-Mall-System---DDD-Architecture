# 登录 Port 职责说明

这份文档只讲一件事：`ILoginService`、`ILoginPort`、`LoginPort` 三者到底怎么分工，以及为什么 `ILoginPort` 里会有 `createQrCodeTicket()` 和 `sendLoginTempleteMessage(String openid)` 这两个方法。

## 1. 先记住一句话

- `Service` 负责“业务怎么做”
- `Port` 负责“业务需要哪些外部能力”
- `Infrastructure` 负责“把外部能力真正做出来”

如果把这三个角色混在一起，代码就会变成：

- Controller 直接调微信接口
- Service 里塞 Retrofit / Cache / DTO 细节
- 以后改微信接口时，业务层也跟着大范围修改

所以这个项目把它们拆开了。

## 2. `ILoginService` 是干什么的

`ILoginService` 是给上层调用的业务接口。上层不关心微信怎么调，只关心登录流程需要什么能力。

当前登录场景里，上层只需要 3 个动作：

```java
String createQrCodeTicket();
String checkLogin(String ticket);
void saveLoginState(String ticket, String openid) throws Exception;
```

它们分别对应：

- 申请二维码 ticket
- 轮询登录状态
- 微信回调后保存登录态

也就是说，`ILoginService` 描述的是“登录业务本身”。

## 3. `ILoginPort` 是干什么的

`ILoginPort` 是 `Service` 依赖的“外部能力接口”。

`WeixinLoginService` 做业务编排时，自己不去碰微信 HTTP，不去碰 Retrofit，不去碰 token 缓存，而是调用 `ILoginPort`。

`ILoginPort` 里定义的是“登录业务需要的外部能力”：

```java
String createQrCodeTicket() throws IOException;
void sendLoginTempleteMessage(String openid) throws IOException;
```

这两个方法看起来不同，但本质上都属于同一类能力：

- 都要访问微信公众平台
- 都要先拿 `access_token`
- 都是微信扫码登录流程的一部分

所以它们被放在同一个 `ILoginPort` 里。

## 4. 为什么 `ILoginPort` 要有这两个方法

### `createQrCodeTicket()`

这个方法负责生成微信扫码登录二维码的 ticket。

内部通常会做这些事：

1. 先从缓存里读 `access_token`
2. 缓存没有就调用微信 `getToken`
3. 再调用微信二维码接口
4. 返回 `ticket`

它是“生成登录入口”的外部能力。

### `sendLoginTempleteMessage(String openid)`

这个方法负责在用户扫码成功后，给微信用户发模板消息。

内部通常会做这些事：

1. 先从缓存里读 `access_token`
2. 缓存没有就调用微信 `getToken`
3. 再调用微信模板消息接口
4. 把“登录成功”消息发给指定 `openid`

它是“登录成功通知”的外部能力。

## 5. 为什么这两个方法不放到 `ILoginService`

因为 `ILoginService` 应该只表达业务动作，不应该暴露外部技术细节。

如果把下面这些内容塞进 `ILoginService`：

- `Retrofit`
- `IWeixinApiService`
- `WeixinTokenResponseDTO`
- `Cache<String, String>`

那业务层就会直接依赖基础设施，后面你换微信 API 或者换缓存方案，业务层也要跟着改。

所以更合理的做法是：

- `ILoginService` 只定义业务动作
- `ILoginPort` 只定义业务需要的外部能力
- `LoginPort` 负责把这些能力实现出来

## 6. 真正的调用链

### 6.1 申请二维码 ticket

```text
LoginController
 -> ILoginService.createQrCodeTicket()
 -> WeixinLoginService
 -> ILoginPort.createQrCodeTicket()
 -> LoginPort
 -> IWeixinApiService
 -> 微信公众平台
```

### 6.2 扫码成功后保存登录态

```text
WeixinPortalController
 -> ILoginService.saveLoginState(ticket, openid)
 -> WeixinLoginService
 -> openidToken.put(ticket, openid)
 -> ILoginPort.sendLoginTempleteMessage(openid)
 -> LoginPort
 -> IWeixinApiService
 -> 微信公众平台
```

### 6.3 前端轮询登录状态

```text
LoginController
 -> ILoginService.checkLogin(ticket)
 -> WeixinLoginService
 -> openidToken.getIfPresent(ticket)
```

## 7. 你之前卡住的点，根本原因是什么

你卡住不是因为“接口定错了”，而是因为你把“实现细节”当成了“接口设计依据”。

正确顺序应该是：

1. 先明确业务动作
2. 再定义 `ILoginService`
3. 再定义 `ILoginPort`
4. 再写 `LoginPort`
5. 最后补 `WeixinTokenResponseDTO`、`IWeixinApiService`、缓存 Bean

不是先想“`getToken()` 怎么写”，而是先想“业务需要调用微信的哪类能力”。

## 8. `getToken()` 报错为什么会出现

你现在看到的 `getToken()` 报错，通常有两种原因：

### 8.1 把 cache 当成了 DTO

`weixinAccessToken` 是：

```java
Cache<String, String>
```

它只能用：

- `getIfPresent()`
- `put()`

不能用 `getToken()`。

### 8.2 DTO 字段名和 getter 名不一致

`WeixinTokenResponseDTO` 当前字段名是：

```java
private String access_token;
```

Lombok 生成的 getter 是：

```java
getAccess_token()
```

不是 `getToken()`。

如果你想写得更符合 Java 习惯，可以把字段改成 `accessToken`，再用 `@JsonProperty("access_token")` 对齐微信返回 JSON。

## 9. 最后给你的判断标准

以后你写登录相关代码时，按这个标准判断放哪一层：

- 这是“登录业务动作”吗？放 `ILoginService`
- 这是“登录业务需要的外部能力”吗？放 `ILoginPort`
- 这是“真的去调微信、缓存、Retrofit”吗？放 `LoginPort` 或 infrastructure

如果你愿意，我下一步可以继续给你补一份“`WeixinLoginService` 和 `LoginPort` 每个方法具体该写什么”的说明，直接按方法级别拆开。
