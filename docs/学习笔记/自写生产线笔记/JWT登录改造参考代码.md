# JWT 登录改造参考代码

这份文档给你一套可以直接照着写的参考方案，目标是把当前“微信扫码后直接返回 openid”的方式，改成“微信确认身份，后端签发 JWT，前端保存 JWT，业务接口统一校验 JWT”。

## 1. 当前问题

现在的登录链路是：

1. 微信扫码回调写入 `ticket -> openid`
2. 前端轮询 `check_login`
3. 成功后后端直接把 `openid` 返回给前端
4. 前端把这个值当成登录 token 存起来

这个方案的问题是：

- `openid` 被直接暴露
- 没有标准的业务 token
- 没有统一鉴权入口
- 只能算演示链路，不是生产级登录

## 2. 推荐改造目标

改成下面这个结构：

```text
微信扫码
  -> 微信回调确认 openid
  -> 后端签发 JWT
  -> 前端保存 JWT
  -> 后续业务请求带 Authorization: Bearer xxx
  -> 后端拦截器统一校验 JWT
```

## 3. 建议的文件位置

按当前项目结构，建议这样放：

### `s-pay-mall-ddd-domain`

- `src/main/java/cn/bugstack/domain/auth/adapter/port/IJwtPort.java`
- `src/main/java/cn/bugstack/domain/auth/service/WeixinLoginService.java`

### `s-pay-mall-ddd-infrastructure`

- `src/main/java/cn/bugstack/infrastructure/adapter/port/JwtPort.java`

### `s-pay-mall-ddd-trigger`

- `src/main/java/cn.bugstack.trigger/http/LoginController.java`
- `src/main/java/cn.bugstack.trigger/interceptor/AuthInterceptor.java`

### `s-pay-mall-ddd-app`

- `src/main/java/cn/bugstack/config/WebMvcConfig.java`
- `src/main/java/cn/bugstack/config/JwtConfig.java`

## 4. 第一步：定义 JWT 端口

先在领域层定义一个抽象接口，让 `domain` 只知道“能生成 token、能校验 token”，不知道具体实现细节。

### 文件

`../../../s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/auth/adapter/port/IJwtPort.java`

### 参考代码

```java
package cn.bugstack.domain.auth.adapter.port;

public interface IJwtPort {

    String createToken(String openid);

    String parseOpenid(String token);

    boolean verify(String token);
}
```

### 这一层的作用

- `createToken`：登录成功后签发 JWT
- `parseOpenid`：后续业务需要时，从 token 中拿出 openid
- `verify`：统一校验 token 是否有效

## 5. 第二步：实现 JWT 端口

JWT 的具体实现放在基础设施层，使用项目里已经引入的 `java-jwt`。

### 文件

`../../../s-pay-mall-ddd-infrastructure/src/main/java/cn/bugstack/infrastructure/adapter/port/JwtPort.java`

### 参考代码

```java
package cn.bugstack.infrastructure.adapter.port;

import cn.bugstack.domain.auth.adapter.port.IJwtPort;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtPort implements IJwtPort {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.expire-seconds}")
    private Long expireSeconds;

    @Override
    public String createToken(String openid) {
        Date now = new Date();
        Date expireAt = new Date(System.currentTimeMillis() + expireSeconds * 1000L);

        return JWT.create()
                .withIssuer(issuer)
                .withIssuedAt(now)
                .withExpiresAt(expireAt)
                .withClaim("openid", openid)
                .sign(Algorithm.HMAC256(secret));
    }

    @Override
    public String parseOpenid(String token) {
        DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC256(secret))
                .withIssuer(issuer)
                .build()
                .verify(token);
        return decodedJWT.getClaim("openid").asString();
    }

    @Override
    public boolean verify(String token) {
        try {
            JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer(issuer)
                    .build()
                    .verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

### 配套配置

`../../../s-pay-mall-ddd-app/src/main/java/cn/bugstack/config/JwtConfig.java`

```java
package cn.bugstack.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    private String secret;
    private String issuer;
    private Long expireSeconds;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public Long getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(Long expireSeconds) {
        this.expireSeconds = expireSeconds;
    }
}
```

### `application.yml` 里加配置

```yaml
jwt:
  secret: your-secret-key
  issuer: s-pay-mall
  expire-seconds: 604800
```

## 6. 第三步：改登录服务

现在 `WeixinLoginService` 只返回 `openid`，要改成返回 JWT。

### 现在的逻辑

```java
public String checkLogin(String ticket) {
    return openidToken.getIfPresent(ticket);
}
```

### 改造后的思路

1. 根据 `ticket` 找到 `openid`
2. 找到了就调用 `IJwtPort.createToken(openid)`
3. 返回 JWT 给控制层
4. 可选：登录成功后把 `ticket` 从缓存里清掉，避免重复签发

### 参考代码

`../../../s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/auth/service/WeixinLoginService.java`

```java
package cn.bugstack.domain.auth.service;

import cn.bugstack.domain.auth.adapter.port.IJwtPort;
import cn.bugstack.domain.auth.adapter.port.ILoginPort;
import cn.bugstack.types.exception.AppException;
import com.google.common.cache.Cache;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

@Service
public class WeixinLoginService implements ILoginService {

    @Resource
    private ILoginPort loginPort;

    @Resource
    private IJwtPort jwtPort;

    @Resource
    private Cache<String, String> openidToken;

    @Override
    public String createQrCodeTicket() {
        try {
            return loginPort.createQrCodeTicket();
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
    }

    @Override
    public String checkLogin(String ticket) {
        String openid = openidToken.getIfPresent(ticket);
        if (openid == null) {
            return null;
        }
        String token = jwtPort.createToken(openid);
        openidToken.invalidate(ticket);
        return token;
    }

    @Override
    public void saveLoginState(String ticket, String openid) throws IOException {
        openidToken.put(ticket, openid);
        loginPort.sendLoginTempleteMessage(openid);
    }
}
```

## 7. 第四步：改登录控制器

控制器不再返回 `openid`，而是返回 JWT。

### 文件

`../../../s-pay-mall-ddd-trigger/src/main/java/cn.bugstack.trigger/http/LoginController.java`

### 改造点

- 成功后 `data` 里放 JWT
- 不要再打印 openid 作为 token
- 返回结构可以保持不变

### 参考代码

```java
@RequestMapping(value = "check_login", method = RequestMethod.GET)
public Response<String> checkLogin(@RequestParam String ticket) {
    try {
        String token = loginService.checkLogin(ticket);
        if (StringUtils.isNotBlank(token)) {
            return Response.<String>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(token)
                    .build();
        }
        return Response.<String>builder()
                .code(Constants.ResponseCode.NO_LOGIN.getCode())
                .info(Constants.ResponseCode.NO_LOGIN.getInfo())
                .build();
    } catch (Exception e) {
        return Response.<String>builder()
                .code(Constants.ResponseCode.UN_ERROR.getCode())
                .info(Constants.ResponseCode.UN_ERROR.getInfo())
                .build();
    }
}
```

## 8. 第五步：加统一鉴权拦截器

JWT 真正有价值的地方，不是“登录成功返回一个字符串”，而是“后续所有业务接口都统一验 token”。

### 文件

`../../../s-pay-mall-ddd-trigger/src/main/java/cn.bugstack.trigger/interceptor/AuthInterceptor.java`

### 参考代码

```java
package cn.bugstack.trigger.interceptor;

import cn.bugstack.domain.auth.adapter.port.IJwtPort;
import cn.bugstack.types.enums.ResponseCode;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

public class AuthInterceptor implements HandlerInterceptor {

    @Resource
    private IJwtPort jwtPort;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        if (path.contains("/api/v1/login/")
                || path.contains("/api/v1/weixin/portal/")) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (StringUtils.isBlank(authorization) || !authorization.startsWith("Bearer ")) {
            writeNoLogin(response);
            return false;
        }

        String token = authorization.substring(7);
        if (!jwtPort.verify(token)) {
            writeNoLogin(response);
            return false;
        }

        request.setAttribute("openid", jwtPort.parseOpenid(token));
        return true;
    }

    private void writeNoLogin(HttpServletResponse response) throws Exception {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(
                ResponseCode.NO_LOGIN
        ));
    }
}
```

### 说明

上面这个拦截器只是参考写法，你实际项目里最好返回统一 `Response` 结构，而不是直接写枚举对象。

如果你想更规范一点，可以把“写 JSON”抽成一个工具方法。

## 9. 第六步：注册拦截器

### 文件

`../../../s-pay-mall-ddd-app/src/main/java/cn/bugstack/config/WebMvcConfig.java`

### 参考代码

```java
package cn.bugstack.config;

import cn.bugstack.trigger.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns(
                        "/api/v1/login/**",
                        "/api/v1/weixin/portal/**"
                );
    }
}
```

### 注意

`AuthInterceptor` 如果要被 Spring 扫到，建议加上 `@Component`，或者在配置类里直接 `new`。

## 10. 第七步：前端怎么改

前端不要再存 `openid`，只存 JWT。

### 参考代码

```javascript
if (data.code === "0000") {
    clearInterval(intervalId);
    localStorage.setItem("loginToken", data.data);
    window.location.href = "index.html";
}
```

### 请求业务接口时

```javascript
fetch('/api/v1/product/1', {
    headers: {
        'Authorization': 'Bearer ' + localStorage.getItem('loginToken')
    }
})
```

## 11. 推荐修改顺序

按这个顺序做，返工最少：

1. 先加 `IJwtPort`
2. 再加 `JwtPort`
3. 再改 `WeixinLoginService`
4. 再改 `LoginController`
5. 再加 `AuthInterceptor`
6. 再加 `WebMvcConfig`
7. 最后改前端存储和请求头

## 12. 你先不要做的事

- 先不要上 Redis 刷新令牌
- 先不要做单点登录
- 先不要做复杂的黑名单注销
- 先不要把 JWT 放到 `openidToken` 里混用

先把“扫码登录 -> 签发 JWT -> 鉴权拦截”这条主链路跑通。

## 13. 现有文件对应关系

你现在仓库里已经有的文件，可以直接对照：

- `LoginController.java`
- `WeixinPortalController.java`
- `WeixinLoginService.java`
- `LoginPort.java`
- `GuavaConfig.java`
- `ProductController.java`

## 14. 最后结论

当前项目的最佳落地方式不是“替换一个 token 字符串”，而是：

1. 微信扫码只负责确认身份
2. 后端统一签发 JWT
3. 前端只保存 JWT
4. 后端所有业务接口统一校验 JWT

如果你愿意，我下一步可以继续直接给你生成第二份文档：

- `JWT登录改造逐文件修改清单.md`

我会把每个文件要删什么、加什么、为什么这么改，按文件粒度写清楚。
