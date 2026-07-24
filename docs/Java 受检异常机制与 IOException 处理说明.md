
# Java 受检异常机制与 IOException 处理说明

## 核心原因：受检异常（Checked Exception）机制

在 Java 中，`IOException` 是一个受检异常。Java 编译器有一条硬性规定：如果一个方法内部调用了会抛出受检异常的方法，那么它必须做出以下两种选择之一：

1. 使用 `try-catch` 捕获并处理这个异常。
2. 在方法签名上使用 `throws` 关键字，将异常继续向上抛出。

如果既没有使用 `try-catch` 捕获，也没有在方法签名上声明 `throws IOException`，编译器就会报错：`Unhandled exception: java.io.IOException`。

## 为什么 `execute()` 会抛出 IOException

`call.execute()` 通常来自 OkHttp 或 Retrofit 等网络请求库。网络请求涉及底层操作，例如：

- 建立 TCP 连接
- 读写网络流（Socket）
- 文件读写或 DNS 解析

这些底层操作随时可能因为断网、超时、服务器宕机等原因失败，因此 Java 强制要求开发者必须考虑并处理这些潜在的 I/O 错误。

## 代码必须加 `throws IOException` 的原因

当代码中直接调用了 `execute()` 并且没有使用 `try-catch` 包裹时：

```java
WeixinTokenResponseDTO weixinTokenResponseDTO = call.execute().body();
```

既然没有在当前方法内部处理（捕获）这个异常，就必须在方法签名上声明 `throws IOException`，将异常传递给上层调用者处理。

## 解决方案

### 方案一：继续向上抛出（推荐用于业务逻辑中间环节）

适合当前方法只是业务逻辑的一环，不想在这里处理网络异常，让上层调用者去统一处理。

```java
// 声明 throws IOException，把异常抛给调用者
public String createQrCodeTicket() throws IOException {
    WeixinTokenResponseDTO weixinTokenResponseDTO = call.execute().body();
    // ...
}
```

### 方案二：在当前方法内捕获处理

如果希望在这个方法内部就解决网络异常问题，不需要加 `throws`，但必须写 `try-catch`。

```java
public String createQrCodeTicket() { // 不需要 throws
    try {
        WeixinTokenResponseDTO weixinTokenResponseDTO = call.execute().body();
        // 正常业务逻辑...
    } catch (IOException e) {
        // 处理异常，比如打印日志、返回默认值或抛出自定义的业务异常
        e.printStackTrace();
        throw new RuntimeException("获取微信Token失败", e);
    }
}
```

## 总结

报错是因为 `execute()` 抛出了受检异常，而代码既没有 `try-catch` 也没有 `throws`。加上 `throws IOException` 就是告诉编译器：“我知道这里可能出错，但我现在不处理，我会提醒调用我的人去处理。”