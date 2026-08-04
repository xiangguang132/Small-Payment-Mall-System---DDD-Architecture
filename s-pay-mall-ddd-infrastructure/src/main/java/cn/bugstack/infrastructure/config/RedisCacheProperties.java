package cn.bugstack.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.redis", ignoreInvalidFields = true)
public class RedisCacheProperties {

    private long cacheTtlHours = 5L;
}
