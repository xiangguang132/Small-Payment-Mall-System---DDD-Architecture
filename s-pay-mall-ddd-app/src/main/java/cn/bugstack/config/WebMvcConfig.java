package cn.bugstack.config;

import cn.bugstack.trigger.interceptor.AuthInterceptor;
import cn.bugstack.trigger.interceptor.RoleInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private AuthInterceptor authInterceptor;
    @Resource
    private RoleInterceptor roleInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns(
                        "/api/v1/login/**",
                        "/api/v1/weixin/portal/**",
                        // 只读展示接口放行：游客可浏览分类导航
                        "/api/v1/product-type/valid-list"
                );
        // 角色权限拦截器，在 AuthInterceptor 之后执行
        // 仅对标注了 @RequireRole 的方法生效
        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/api/v1/**");
    }
}
