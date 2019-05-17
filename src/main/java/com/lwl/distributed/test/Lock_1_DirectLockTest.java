package com.lwl.distributed.test;

import com.lwl.distributed.redis.Lock_1_DirectLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author liuweilong
 * @description
 * @date 2019/5/17 9:57
 */
@SpringBootApplication
@ComponentScan("com.lwl")
public class Lock_1_DirectLockTest {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Lock_1_DirectLockTest.class, args);
        Lock_1_DirectLock lock = context.getBean(Lock_1_DirectLock.class);
        lock.lock("asdf");
        lock.unlock("asdf");
        System.exit(0);
    }
}
