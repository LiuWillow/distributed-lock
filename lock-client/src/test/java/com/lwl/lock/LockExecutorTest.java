package com.lwl.lock;


import com.lwl.client.LockApp;
import com.lwl.client.lock.DistributeExecutor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

/**
 * date  2019/5/19
 * author liuwillow
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LockApp.class)
@Slf4j
public class LockExecutorTest {
    @Autowired
    private DistributeExecutor distributeExecutor;

    @Test
    public void test_exec_reenter() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            new Thread(() -> distributeExecutor.execReenter(() -> {
                log.info("Tread-{}执行第一段程序", Thread.currentThread().getId());

                distributeExecutor.execReenter(() -> {
                    log.info("Tread-{}执行第二段程序", Thread.currentThread().getId());
                    return null;
                }, "lwl_key", "lwl_txId", 20, TimeUnit.SECONDS);

                return null;
            }, "lwl_key", "lwl_txId", 20, TimeUnit.SECONDS)).start();

            TimeUnit.MILLISECONDS.sleep(200);
        }

        TimeUnit.SECONDS.sleep(100);
    }
}