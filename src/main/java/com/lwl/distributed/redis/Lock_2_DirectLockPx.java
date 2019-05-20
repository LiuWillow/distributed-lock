package com.lwl.distributed.redis;

import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author liuweilong
 * @description
 * @date 2019/5/17 9:26
 */
@Service("redisDirectPx")
public class Lock_2_DirectLockPx extends BaseRedisLock {
    /**
     * 先调用setnx   再调用expire
     */
    @Override
    public boolean lock(String key, String value){
        Boolean nxSuccess = redisTemplate.opsForValue().setIfAbsent(key, value);
        Boolean expireSuccess = redisTemplate.expire(key, EXPIRE, TimeUnit.MILLISECONDS);
        return nxSuccess && expireSuccess;
    }
}
