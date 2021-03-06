package com.lwl.client.redis;

import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author liuweilong
 * @description
 * @date 2019/5/17 8:54
 */
@Service("redisDirect")
public class Lock_1_DirectLock extends BaseRedisLock {
    /**
     * 直接调用setnx
     */
    @Override
    public boolean lock(String key, String value) {
        return Optional.ofNullable(redisTemplate.opsForValue().setIfAbsent(key, value)).orElse(false);
    }
}
