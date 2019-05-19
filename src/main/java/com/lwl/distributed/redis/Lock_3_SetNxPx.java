package com.lwl.distributed.redis;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Service;

/**
 * @author liuweilong
 * @description
 * @date 2019/5/17 10:56
 */
@Service("redisSetNxPx")
public class Lock_3_SetNxPx extends RedisLock {

    /**
     * 调用set  传入nx和px参数
     * @param key
     * @return
     */
    @Override
    public boolean lock(String key, String value) {
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        Boolean success = connection.set(key.getBytes(), value.getBytes(), Expiration.milliseconds(5000),
                RedisStringCommands.SetOption.ifAbsent());
        return success == null ? false : success;
    }
}
