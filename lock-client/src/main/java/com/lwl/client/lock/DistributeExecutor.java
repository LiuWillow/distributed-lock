package com.lwl.client.lock;

import com.lwl.client.redis.Lock_5_SetNxPx_TxId_Lua;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * author liuweilong
 * date 2019/12/30 6:12 下午
 * desc
 */
@Service
@Slf4j
public class DistributeExecutor {
    @Autowired
    private Lock_5_SetNxPx_TxId_Lua distributedLock;

    private static final long DEFAULT_EXPIRE = 5000L;
    private static final int DEFAULT_RETRY_TIMES = 3;

    /**
     * 执行需要分布式锁的任务
     *
     * @param task       任务
     * @param key        分布式key
     * @param txId       分布式事务id
     * @param expire     锁过期时间
     * @param timeUnit   等待时间对应的时间单位
     * @param retryTimes 重试次数
     */
    private <T> T exec(Supplier<T> task, String key, String txId, long expire, TimeUnit timeUnit, int retryTimes, boolean reenter) {
        T result;
        boolean lockSuccess = false;
        try {
            //上锁
            lockSuccess = distributedLock.reentrantLock(key, txId, expire, timeUnit, retryTimes, reenter);
            if (!lockSuccess) {
                return null;
            }
            //执行任务
            result = task.get();
        } catch (Exception currentException) {
            log.error("执行分布式锁任务异常", currentException);
            return null;
        } finally {
            //解锁
            if (lockSuccess) {
                distributedLock.reentrantUnLock(key, txId);
            }
        }
        return result;
    }

    /**
     * 执行分布式锁任务，等待时间用默认值
     *
     * @param task 任务逻辑
     * @param key  键
     * @param txId 值，事务id，如文档锁，可以用文档id + 时间戳 + 随机数
     */
    public <T> T exec(Supplier<T> task, String key, String txId) {
        //默认不可重入
        return exec(task, key, txId, DEFAULT_EXPIRE, TimeUnit.MILLISECONDS, DEFAULT_RETRY_TIMES, false);
    }

    /**
     * 执行分布式锁任务
     *
     * @param task     任务逻辑
     * @param key      键
     * @param txId     值，事务id，如文档锁，可以用文档id + 时间戳 + 随机数
     * @param expire   超时时间
     * @param timeUnit 单位
     */
    public <T> T exec(Supplier<T> task, String key, String txId, long expire, TimeUnit timeUnit) {
        //默认不可重入
        return exec(task, key, txId, expire, timeUnit, DEFAULT_RETRY_TIMES, false);
    }

    /**
     * 执行分布式锁任务，可重入
     *
     * @param task     任务逻辑
     * @param key      键
     * @param txId     值，事务id，如文档锁，可以用文档id + 时间戳 + 随机数
     * @param expire   超时时间
     * @param timeUnit 单位
     */
    public <T> T execReenter(Supplier<T> task, String key, String txId, long expire, TimeUnit timeUnit) {
        //默认不可重入
        return exec(task, key, txId, expire, timeUnit, DEFAULT_RETRY_TIMES, true);
    }

    /**
     * 执行分布式锁任务，等待时间用默认值，可重入
     *
     * @param task 任务逻辑
     * @param key  键
     * @param txId 值，事务id，如文档锁，可以用文档id + 时间戳 + 随机数
     */
    public <T> T execReenter(Supplier<T> task, String key, String txId) {
        //默认不可重入
        return exec(task, key, txId, DEFAULT_EXPIRE, TimeUnit.MILLISECONDS, DEFAULT_RETRY_TIMES, true);
    }
}
