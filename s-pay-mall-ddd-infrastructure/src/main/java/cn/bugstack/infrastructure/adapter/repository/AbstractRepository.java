package cn.bugstack.infrastructure.adapter.repository;

import cn.bugstack.infrastructure.dcc.DCCService;
import cn.bugstack.infrastructure.redis.IRedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Resource;
import java.util.function.Supplier;

/**
 * Repository 抽象基类
 * 封装函数式缓存 + DB 降级，子类通过 getFromCacheOrDb 统一缓存操作
 */
public class AbstractRepository {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    protected IRedisService redisService;

    @Resource
    protected DCCService dccService;

    /**
     * 通用缓存查询：优先读缓存，未命中则执行 dbFallback 查库并回填
     *
     * @param cacheKey  缓存 key
     * @param dbFallback 数据库兜底查询（延迟执行）
     */
    protected <T> T getFromCacheOrDb(String cacheKey, Supplier<T> dbFallback) {
        if (dccService.isCacheOpenSwitch()) {
            T cacheResult = redisService.getValue(cacheKey);
            if (null != cacheResult) {
                return cacheResult;
            }
            T dbResult = dbFallback.get();
            if (null == dbResult) {
                return null;
            }
            redisService.setValue(cacheKey, dbResult);
            return dbResult;
        } else {
            logger.warn("缓存降级，未开启：{}", cacheKey);
            return dbFallback.get();
        }
    }

    /**
     * 通用缓存查询（带过期时间）
     */
    protected <T> T getFromCacheOrDb(String cacheKey, Supplier<T> dbFallback, long expired) {
        if (dccService.isCacheOpenSwitch()) {
            T cacheResult = redisService.getValue(cacheKey);
            if (null != cacheResult) {
                return cacheResult;
            }
            T dbResult = dbFallback.get();
            if (null == dbResult) {
                return null;
            }
            redisService.setValue(cacheKey, dbResult, expired);
            return dbResult;
        } else {
            logger.warn("缓存降级：{}", cacheKey);
            return dbFallback.get();
        }
    }
}
