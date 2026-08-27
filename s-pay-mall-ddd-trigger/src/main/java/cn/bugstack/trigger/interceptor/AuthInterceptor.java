package cn.bugstack.trigger.interceptor;

import cn.bugstack.domain.auth.adapter.port.IJwtPort;
import cn.bugstack.api.response.Response;
import cn.bugstack.types.enums.ResponseCode;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Resource
    private IJwtPort jwtPort;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 预检请求（CORS 浏览器自动发送），手动补充 CORS 头
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setHeader("Access-Control-Max-Age", "3600");
            response.setStatus(HttpServletResponse.SC_OK);
            return false;
        }

        String path = request.getServletPath();

        if (path.contains("/api/v1/login") || path.contains("/api/v1/weixin/portal")) {
            return true;
        }

        // 检查方法上的 @PublicEndpoint 注解，标记后跳过认证
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            if (handlerMethod.hasMethodAnnotation(PublicEndpoint.class)) {
                // 公开接口：尝试解析 token 设置 userId/role（可选），不强制
                String token = resolveToken(request);
                if (StringUtils.isNotBlank(token) && jwtPort.verifyToken(token)) {
                    request.setAttribute("userId", jwtPort.parseUserId(token));
                    request.setAttribute("role", jwtPort.parseRole(token));
                }
                return true;
            }
        }

        String token = resolveToken(request);
        if (StringUtils.isBlank(token)) {
            writeNoLogin(response);
            return false;
        }
        if (!jwtPort.verifyToken(token)) {
            writeNoLogin(response);
            return false;
        }
        request.setAttribute("userId", jwtPort.parseUserId(token));
        request.setAttribute("role", jwtPort.parseRole(token));
        return true;
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isNotBlank(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if ("loginToken".equals(cookie.getName()) && StringUtils.isNotBlank(cookie.getValue())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void writeNoLogin(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(Response.<Object>builder()
                .code(ResponseCode.NO_LOGIN.getCode())
                .info(ResponseCode.NO_LOGIN.getInfo())
                .build()));
    }
}
