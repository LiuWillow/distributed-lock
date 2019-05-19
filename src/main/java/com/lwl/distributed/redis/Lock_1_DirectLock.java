package com.lwl.distributed.redis;

import org.springframework.stereotype.Service;

/**
 * @author liuweilong
 * @description
 * @date 2019/5/17 8:54
 */
@Service("redisDirect")
public class Lock_1_DirectLock extends RedisLock {
    /**
     * 直接调用setnx
     */
    @Override
    public boolean lock(String key, String value){
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }
}
