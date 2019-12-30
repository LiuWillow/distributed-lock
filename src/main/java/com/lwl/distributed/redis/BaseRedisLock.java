package com.lwl.distributed.redis;

import com.lwl.distributed.lock.IDistributedLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * @author liuweilong
 * @description
 * @date 2019/5/17 9:36
 */
@Service
public abstract class BaseRedisLock implements IDistributedLock {
    /**
     * 最大重试次数
     */
    protected static final int MAX_RETRY_TIMES = 3;
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

    protected Boolean retry(Supplier<Boolean> task) {
        int retryTimes = 0;
        Boolean success = false;
        while (true) {
            if (retryTimes++ > MAX_RETRY_TIMES) {
                return success;
            }
            success = task.get();
            if (success) {
                return true;
            }
        }
    }

    /**
     * 解锁
     */
    @Override
    public boolean unlock(String key, String value) {
        return redisTemplate.delete(key);
    }
}
