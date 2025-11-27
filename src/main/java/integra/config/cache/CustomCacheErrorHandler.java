package integra.config.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CustomCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.error("Error retrieving from cache '{}' with key '{}': {}", cache.getName(), key, exception.getMessage(), exception);
        // Graceful degradation: do not rethrow, application continues without cache
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        log.error("Error putting into cache '{}' with key '{}': {}", cache.getName(), key, exception.getMessage(), exception);
        // Graceful degradation: do not rethrow
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.error("Error evicting from cache '{}' with key '{}': {}", cache.getName(), key, exception.getMessage(), exception);
        // Graceful degradation: do not rethrow
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.error("Error clearing cache '{}': {}", cache.getName(), exception.getMessage(), exception);
        // Graceful degradation: do not rethrow
    }
}