package com.lwl.client.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * @author liuweilong
 * @description
 * @date 2019/5/17 10:56
 */
@Service("redisNxPxTx")
public class Lock_4_SetNxPx_TxId extends BaseRedisLock {
    @Autowired
    public StringRedisTemplate redisTemplate;

    /**
     * 调用set  传入nx和px参数，值为可以唯一标识当前线程的值
     * @param key
     * @return
     */
    @Override
    public boolean lock(String key, String txId) {
        return retry(() -> redisTemplate.opsForValue().setIfAbsent(key, txId, Duration.ofMillis(EXPIRE)));
    }

    /**
     * 调用set  传入nx和px参数，值为可以唯一标识当前线程的值（稍微安全）
     *
     */
    @Override
    public boolean unlock(String key, String txId) {
        String oldTxId = redisTemplate.opsForValue().get(key);
        if (!txId.equals(oldTxId)){
            //txId不同，表示不是同一个事务
            return false;
        }
        return Optional.ofNullable(redisTemplate.delete(key)).orElse(false);
    }
}
