package cn.bugstack.trigger.interceptor;

import cn.bugstack.api.response.Response;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.enums.RoleEnum;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 角色权限拦截器，配合 @RequireRole 注解使用。
 * 需要在 AuthInterceptor 之后执行，确保 userId 和 role 已被设置。
 */
@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        // 先查方法级注解，再查类级注解
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole == null) {
            return true;
        }

        Object roleAttr = request.getAttribute("role");
        if (roleAttr == null) {
            // 未登录用户：跳过角色检查（认证由 AuthInterceptor 负责）
            return true;
        }

        int userRole = (int) roleAttr;
        boolean matched = Arrays.stream(requireRole.value())
                .anyMatch(role -> role.getCode() == userRole);

        if (!matched) {
            writeNoPermission(response);
            return false;
        }

        return true;
    }

    private void writeNoPermission(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(Response.<Object>builder()
                .code(ResponseCode.FORBIDDEN.getCode())
                .info(ResponseCode.FORBIDDEN.getInfo())
                .build()));
    }

}
