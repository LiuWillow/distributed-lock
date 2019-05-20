package com.lwl.distributed.redis;

import com.lwl.distributed.IDistributedLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author liuweilong
 * @description
 * @date 2019/5/17 9:36
 */
@Service
public abstract class BaseRedisLock implements IDistributedLock {
    @Autowired
    public StringRedisTemplate redisTemplate;

    /**
     * 解锁
     */
    @Override
    public boolean unlock(String key, String value){
       return redisTemplate.delete(key);
    }
}
