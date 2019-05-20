package com.lwl.distributed.redis;


import com.lwl.distributed.IDistributedLock;
import com.lwl.distributed.LockApp;
import com.lwl.distributed.factory.LockType;
import com.lwl.distributed.test.SimpleTestThread;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * date  2019/5/19
 * author liuwillow
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LockApp.class)
public class LockTest {
    @Autowired
    private Map<String, IDistributedLock> factory;

    private static final long RETRY_TIME_OUT = 2000;
    private static final long TASK_TIME = IDistributedLock.EXPIRE - 1000;
    private static final long GC_TIME = 3000;
    private static final int RETRY_TIMES = 5;
    private AtomicBoolean hasGc = new AtomicBoolean(false);


    private static final String GOODS_ID = "1";

    @Test
    public void testLock() throws InterruptedException {
        IDistributedLock lock = factory.get(LockType.ZK_TMP);
        for (int i = 0; i < 10; i++) {
            String threadName = i + "";
            new SimpleTestThread(threadName, lock, GOODS_ID, "").start();
        }
        TimeUnit.SECONDS.sleep(20000);
    }

    @Test
    public void testTx() throws InterruptedException {
        IDistributedLock lock = factory.get(LockType.REDIS_NX_PX_TX);
        for (int i = 0; i < 3; i++) {
            Runnable task = generateTask(lock);
            new Thread(task).start();
        }
        Thread.sleep(30000);
    }

    private Runnable generateTask(IDistributedLock lock) {
        return () -> {
            String txId = Thread.currentThread().getName();
            boolean success = lock.lock(GOODS_ID, txId);
            for (int i = 0; i < RETRY_TIMES && !success; i++) {
                success = retry(lock, txId, i + 1);
            }
            if (!success) {
                System.out.println("线程：" + txId + "获取锁失败");
                return;
            }
            System.out.println("-----------线程：" + txId + "上锁成功--------");
            System.out.println("线程：" + txId + "开始执行事务");
            if (!hasGc.getAndSet(true)) {
                System.out.println("线程：" + txId + "触发gc");
                sleepSeconds(TASK_TIME + GC_TIME);
            } else {
                sleepSeconds(TASK_TIME);
            }
            System.out.println("线程：" + txId + "任务执行完毕，开始释放获取到的锁");
            boolean unlock = lock.unlock(GOODS_ID, txId);
            if (unlock) {
                System.out.println("线程：" + txId + "锁释放成功");
                return;
            }
            System.out.println("线程：" + txId + "锁释放失败，事务回滚");
        };
    }

    private void sleepSeconds(long timeout) {
        try {
            TimeUnit.MILLISECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean retry(IDistributedLock lock, String txId, int i) {
        System.out.println("线程：" + Thread.currentThread().getName() + "正在尝试第" +
                i + "次重新获取锁");
        sleepSeconds(RETRY_TIME_OUT);
        return lock.lock(GOODS_ID, txId);
    }

}