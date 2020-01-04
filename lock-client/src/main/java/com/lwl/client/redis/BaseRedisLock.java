package com.lwl.client.redis;

import com.lwl.client.lock.IDistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author liuweilong
 * @description
 * @date 2019/5/17 9:36
 */
@Slf4j
public abstract class BaseRedisLock implements IDistributedLock {
    /**
     * 最大重试次数
     */
    protected static final int DEFAULT_MAX_RETRY_TIMES = 3;
    protected static final long DEFAULT_WAIT_TIME = 100L;
    protected static final TimeUnit DEFAULT_WAITE_TIME_UNIT = TimeUnit.MILLISECONDS;
    @Autowired
    public StringRedisTemplate redisTemplate;

    protected RedisConnectionFactory getConnectionFactory() {
        return redisTemplate.getConnectionFactory();
    }

    protected RedisConnection getConnection() {
        return getConnectionFactory().getConnection();
    }

    protected void releaseConnection(RedisConnection redisConnection) {
        RedisConnectionUtils.releaseConnection(redisConnection, getConnectionFactory());
    }

    protected Boolean retry(Supplier<Boolean> task, int maxRetryTimes, long waitTime, TimeUnit timeUnit) {
        int retryTimes = 0;
        while (true) {
            if (retryTimes++ > maxRetryTimes) {
                return false;
            }
            boolean success = task.get();
            if (success) {
                return true;
            }
            try {
                timeUnit.sleep(waitTime);
            } catch (InterruptedException e) {
                log.error("分布式锁重试，线程interrupted异常", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    protected Boolean retry(Supplier<Boolean> task) {
        return retry(task, DEFAULT_MAX_RETRY_TIMES, DEFAULT_WAIT_TIME, DEFAULT_WAITE_TIME_UNIT);
    }

    /**
     * 解锁
     */
    @Override
    public boolean unlock(String key, String value) {
        return Optional.ofNullable(redisTemplate.delete(key)).orElse(false);
    }
}
