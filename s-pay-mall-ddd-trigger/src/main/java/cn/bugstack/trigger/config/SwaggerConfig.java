package cn.bugstack.trigger.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI API文档配置
 *
 * @author cc_132678
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("S-Pay-Mall DDD 支付商城API文档")
                .description("基于DDD架构的支付商城系统API文档")
                .version("1.0")
                .contact(new Contact()
                        .name("cc_132678")
                        .url("https://gitee.com/cc132678")
                        .email("1326784764@qq.com"));
    }

}
