package cn.bugstack.config;

import cn.bugstack.infrastructure.config.RedisCacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RedisCacheProperties.class)
public class RedisCacheConfig {
}
