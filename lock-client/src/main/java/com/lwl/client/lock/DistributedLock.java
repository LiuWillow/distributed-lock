package com.lwl.client.lock;

import java.lang.annotation.*;

/**
 * author liuweilong
 * date 2019/12/30 6:37 下午
 * desc 分布式锁注解，放在要执行的逻辑方法上即可
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface DistributedLock {
    /**
     * 键
     * @return
     */
    String key();

    /**
     * 值
     * @return
     */
    String txId();
}