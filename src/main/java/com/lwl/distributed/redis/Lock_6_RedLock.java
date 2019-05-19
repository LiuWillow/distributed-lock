package com.lwl.distributed.redis;

import org.redisson.Redisson;
import org.redisson.RedissonRedLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author liuweilong
 * @description
 * @date 2019/5/17 11:23
 */
@Service("redLock")
public class Lock_6_RedLock extends RedisLock{
    @Override
    public boolean lock(String key, String value) {
        Config config1 = new Config();
        config1.useSingleServer().setAddress("address1")
                .setPassword("password").setDatabase(1);
        RedissonClient client1 = Redisson.create(config1);

        Config config2 = new Config();
        config1.useSingleServer().setAddress("address2")
                .setPassword("password").setDatabase(1);
        RedissonClient client2 = Redisson.create(config2);

        Config config3 = new Config();
        config1.useSingleServer().setAddress("address3")
                .setPassword("password").setDatabase(1);
        RedissonClient client3 = Redisson.create(config3);

        RLock lock1 = client1.getLock(key);
        RLock lock2 = client2.getLock(key);
        RLock lock3 = client3.getLock(key);

        RedissonRedLock redLock = new RedissonRedLock(lock1, lock2, lock3);
        try {
            return redLock.tryLock(3, 5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
}
