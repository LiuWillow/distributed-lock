package com.lwl.lock;


import com.lwl.client.LockApp;
import com.lwl.client.lock.DistributeExecutor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * date  2019/5/19
 * author liuwillow
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LockApp.class)
public class LockExecutorTest {
    @Autowired
    private DistributeExecutor distributeExecutor;

    @Test
    public void test_exec_reenter() {
        distributeExecutor.execReenter(() -> {
            System.out.println("执行第一段程序");

            distributeExecutor.execReenter(() -> {
                System.out.println("执行第二段程序");
                return null;
            }, "lwl_key", "lwl_txId");

            return null;
        }, "lwl_key", "lwl_txId");
    }
}