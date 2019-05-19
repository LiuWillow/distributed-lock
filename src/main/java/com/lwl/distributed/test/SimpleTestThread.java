package com.lwl.distributed.test;

import com.lwl.distributed.redis.DistributedLock;
import io.netty.util.internal.StringUtil;
import org.springframework.util.StringUtils;

/**
 * date  2019/5/19
 * author liuwillow
 **/
public class SimpleTestThread extends Thread {
    private DistributedLock lock;
    private String key;
    private String value;

    public SimpleTestThread(String threadName, DistributedLock lock,
                            String key, String value) {
        this.lock = lock;
        this.key = key;
        this.value = value;
        if (StringUtils.isEmpty(threadName)){
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
        } else {
            System.out.println("线程：" + this.getName() + "获取锁失败");
        }
    }
}
