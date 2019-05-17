package com.lwl.distributed.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author liuweilong
 * @description
 * @date 2019/5/17 9:26
 */
@Service
public class Lock_2_DirectLockDirectPx extends Lock{
    /**
     * 先调用setnx   再调用expire
     */
    @Override
    public boolean lock(String key){
        Boolean nxSuccess = redisTemplate.opsForValue().setIfAbsent(key, "lalal");
        Boolean expireSuccess = redisTemplate.expire(key, 10, TimeUnit.SECONDS);
        return nxSuccess && expireSuccess;
    }
}
