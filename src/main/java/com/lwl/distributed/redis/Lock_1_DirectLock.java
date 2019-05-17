package com.lwl.distributed.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author liuweilong
 * @description
 * @date 2019/5/17 8:54
 */
@Service
public class Lock_1_DirectLock extends Lock{
    /**
     * 直接调用setnx
     */
    @Override
    public boolean lock(String key){
        return redisTemplate.opsForValue().setIfAbsent(key, "sdfsdf");
    }
}
