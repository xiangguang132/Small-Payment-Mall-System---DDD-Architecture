package cn.bugstack.config;

import cn.bugstack.trigger.interceptor.AuthInterceptor;
import cn.bugstack.trigger.interceptor.RoleInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /** 上传根目录（与 LocalFileStorageService 的 file.upload-dir 保持一致） */
    @Value("${file.upload-dir:./upload}")
    private String uploadDir;

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

    /**
     * 本地上传图片静态映射：/files/** -> {file.upload-dir}/，供商品封面/图片集等以相对 URL 访问
     * 不再于 /api/v1/** 下，故图片访问不经过登录鉴权
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absoluteDir = Paths.get(uploadDir).toAbsolutePath().normalize().toString().replace('\\', '/');
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + absoluteDir + "/");
    }
}
