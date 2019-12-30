package com.lwl.distributed.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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
    @Qualifier("redisNxPxTxLua")
    private IDistributedLock distributedLock;

    /**
     * 需要上锁的业务，调用该方法执行（或者用AOP的方式）
     * @param task 业务逻辑
     * @param key
     * @param txId 事务id
     */
    public <T> T exec(Supplier<T> task, String key, String txId) {
        T result;
        try {
            boolean success = distributedLock.lock(key, txId);
            if (!success) {
                log.error("分布式锁上锁失败，key:{}, txId:{}", key, txId);
                return null;
            }
            //执行任务
            result = task.get();
        } catch (Exception e) {
            log.error("分布式锁未知异常", e);
            return null;
        } finally {
            distributedLock.unlock(key, txId);
        }
        return result;
    }
}
