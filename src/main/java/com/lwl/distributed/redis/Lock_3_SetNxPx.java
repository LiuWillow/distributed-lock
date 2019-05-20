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
@Service("redisNxPx")
public class Lock_3_SetNxPx extends BaseRedisLock {

    /**
     * 调用set  传入nx参数
     * 问题：
     * 1、事务执行的时间大于锁的超时时间，导致事务没执行完，别的线程就拿到了锁
     * 2、上面那个问题还会导致当前线程释放其他线程的锁
     */
    @Override
    public boolean lock(String key, String value) {
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        Boolean success = connection.set(key.getBytes(), value.getBytes(),
                Expiration.milliseconds(EXPIRE),
                RedisStringCommands.SetOption.ifAbsent());
        return success == null ? false : success;
    }
}
