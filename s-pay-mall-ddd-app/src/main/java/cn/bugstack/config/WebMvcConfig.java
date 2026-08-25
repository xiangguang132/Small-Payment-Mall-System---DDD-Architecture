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
                        "/api/v1/weixin/portal/**",
                        "/api/v1/product/**",
                        // 只读展示接口放行：游客可浏览首页/商品详情的拼团价与分类导航
                        "/api/v1/groupbuy/queryGroupBuyTrial",
                        "/api/v1/product-type/valid-list"
                );
    }
}
