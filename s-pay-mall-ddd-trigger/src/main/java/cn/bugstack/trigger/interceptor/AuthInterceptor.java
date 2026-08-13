package cn.bugstack.trigger.interceptor;

import cn.bugstack.domain.auth.adapter.port.IJwtPort;
import cn.bugstack.api.response.Response;
import cn.bugstack.types.enums.ResponseCode;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
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
        String path = request.getServletPath();

        if (path.contains("/api/v1/login") || path.contains("/api/v1/weixin/portal")) {
            return true;
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
        request.setAttribute("openid", jwtPort.parseOpenid(token));
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
