package cn.bugstack.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "thread.pool.executor.config", ignoreInvalidFields = true)
public class ThreadPoolConfigProperties {

    private int corePoolSize = 20;
    private int maxPoolSize = 50;
    private long keepAliveTime = 5000L;
    private int blockQueueSize = 5000;
    private String policy = "CallerRunsPolicy";

}
