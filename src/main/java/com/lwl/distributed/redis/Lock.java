package com.lwl.distributed.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author liuweilong
 * @description
 * @date 2019/5/17 9:36
 */
@Service
public abstract class Lock {
    @Autowired
    public RedisTemplate redisTemplate;

    /**
     * 解锁
     */
    public boolean unlock(String key){
        return redisTemplate.delete(key);
    }

    /**
     * 获取分布式锁抽象方法
     */
    public abstract boolean lock(String key);
}
