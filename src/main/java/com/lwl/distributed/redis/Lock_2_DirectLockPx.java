package com.lwl.distributed.redis;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author liuweilong
 * @description
 * @date 2019/5/17 9:26
 */
@Service("redisDirectPx")
public class Lock_2_DirectLockPx extends BaseRedisLock {
    /**
     * 先调用setnx   再调用expire（超老的版本redis才用这个）
     */
    @Override
    public boolean lock(String key, String value){
        Boolean nxSuccess = Optional.ofNullable(redisTemplate.opsForValue().setIfAbsent(key, value)).orElse(false);
        Boolean expireSuccess = Optional.ofNullable(redisTemplate.expire(key, EXPIRE, TimeUnit.MILLISECONDS)).orElse(false);
        return nxSuccess && expireSuccess;
    }
}
