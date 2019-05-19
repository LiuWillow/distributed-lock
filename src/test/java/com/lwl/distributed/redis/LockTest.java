package com.lwl.distributed.redis;


import com.lwl.distributed.LockApp;
import com.lwl.distributed.factory.LockType;
import com.lwl.distributed.test.TestThread;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * date  2019/5/19
 * author liuwillow
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LockApp.class)
public class LockTest {
    @Autowired
    private Map<String, DistributedLock> factory;

    private static final String GOODS_ID = "1";
    @Test
    public void testRedisDirect() throws InterruptedException {
        DistributedLock lock = factory.get(LockType.REDIS_DIRECT);
        for (int i = 0; i < 10; i++) {
            new TestThread(i + "", lock, GOODS_ID, "").start();
        }
        TimeUnit.SECONDS.sleep(3);
    }
}