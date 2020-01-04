package com.lwl.client.test;

import com.lwl.client.lock.IDistributedLock;
import org.springframework.util.StringUtils;

/**
 * date  2019/5/19
 * author liuwillow
 **/
public class SimpleTestThread extends Thread {
    private IDistributedLock lock;
    private String key;
    private String value;

    public SimpleTestThread(String threadName, IDistributedLock lock,
                            String key, String value) {
        this.lock = lock;
        this.key = key;
        this.value = value;
        if (StringUtils.isEmpty(threadName)) {
            return;
        }
        this.setName(threadName);
    }

    @Override
    public void run() {
        System.out.println("线程：" + this.getName() + "正在争抢分布式锁");
        boolean lock = this.lock.lock(key, value);
        if (lock) {
            System.out.println("线程：" + this.getName() + "获取锁成功");
            System.out.println("线程：" + this.getName() + "开始执行任务");
            try {
                Thread.sleep(500);
                this.lock.unlock(key, value);
                System.out.println("线程：" + this.getName() + "释放锁");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("线程：" + this.getName() + "获取锁失败");
        }
    }
}
