package cn.bugstack.infrastructure.config;

import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class RedisCacheService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisCacheProperties redisCacheProperties;

    public <T> T get(String key, Class<T> clazz) {
        String cacheValue = stringRedisTemplate.opsForValue().get(key);
        if (cacheValue == null) {
            return null;
        }
        return JSON.parseObject(cacheValue, clazz);
    }

    public void set(String key, Object value) {
        stringRedisTemplate.opsForValue().set(
                key,
                JSON.toJSONString(value),
                redisCacheProperties.getCacheTtlHours(),
                TimeUnit.HOURS
        );
    }

    public void delete(String... keys) {
        for (String key : keys) {
            if (key != null) {
                stringRedisTemplate.delete(key);
            }
        }
    }
}
