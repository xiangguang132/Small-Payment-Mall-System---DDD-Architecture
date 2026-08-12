package cn.bugstack.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(ThreadPoolConfigProperties.class)
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolConfigProperties properties) {
        return new ThreadPoolExecutor(
                properties.getCorePoolSize(),
                properties.getMaxPoolSize(),
                properties.getKeepAliveTime(),
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(properties.getBlockQueueSize()),
                rejectedExecutionHandler(properties.getPolicy())
        );
    }

    private RejectedExecutionHandler rejectedExecutionHandler(String policy) {
        if ("AbortPolicy".equalsIgnoreCase(policy)) {
            return new ThreadPoolExecutor.AbortPolicy();
        }
        if ("DiscardPolicy".equalsIgnoreCase(policy)) {
            return new ThreadPoolExecutor.DiscardPolicy();
        }
        if ("DiscardOldestPolicy".equalsIgnoreCase(policy)) {
            return new ThreadPoolExecutor.DiscardOldestPolicy();
        }
        return new ThreadPoolExecutor.CallerRunsPolicy();
    }

}
