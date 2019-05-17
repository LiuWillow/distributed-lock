package com.lwl.distributed.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.concurrent.TimeUnit;


@SpringBootTest
@RunWith(SpringRunner.class)
public class Lock_1_DirectLockTest {
    @Autowired
    private Lock_1_DirectLock directLock;

    @Test
    public void testDirectLock() throws InterruptedException {
        String goodsId = "1";
        for (int i = 0; i < 10; i++) {
            Thread task = new Thread(() -> {
                boolean success = directLock.lock(goodsId);
                if (success) {
                    try {
                        System.out.println("线程：" + Thread.currentThread().getName() + "成功获取分布式锁");
                        TimeUnit.SECONDS.sleep(5);
                        directLock.unlock(goodsId);
                        System.out.println("线程：" + Thread.currentThread().getName() + "释放分布式锁");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("线程：" + Thread.currentThread().getName() + "获取分布式锁失败");
                }
            });
            task.join();
            task.start();
        }

    }

}