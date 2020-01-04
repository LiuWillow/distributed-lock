package com.lwl.client.redis;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NamedThreadLocal;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author liuweilong
 * @description
 * @date 2019/5/17 10:56
 */
@Service("redisNxPxTxLua")
@Slf4j
public class Lock_5_SetNxPx_TxId_Lua extends BaseRedisLock {
    private static NamedThreadLocal<Integer> STATE = new NamedThreadLocal<>("redis_distribute_lock_state");

    @Autowired
    public StringRedisTemplate redisTemplate;
    private static final String casScript = "if redis.call('get', KEYS[1]) == ARGV[1] " +
            "then return redis.call('del', KEYS[1]) " +
            "else return 0 end";
    private static final Long SUCCESS = 1L;

    /**
     * 调用set  传入nx和px参数，值为可以唯一标识当前线程的值
     *
     * @param key
     * @return
     */
    @Override
    public boolean lock(String key, String txId) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        //自带重试逻辑
        return retry(() -> Optional.ofNullable(valueOperations.setIfAbsent(key, txId, EXPIRE, TimeUnit.MILLISECONDS)).orElse(false));
    }

    /**
     * 利用lua脚本比较
     *
     * @param key
     * @return
     */
    @Override
    public boolean unlock(String key, String txId) {
        //redisTemplate只能接受Long
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(casScript);
        redisScript.setResultType(Long.class);
        Long result = redisTemplate.execute(redisScript, Lists.newArrayList(key),
                txId);
        //也可以用connection
//        Boolean execute = redisTemplate.execute((RedisConnection connection) -> connection.eval(
//                casScript.getBytes(),
//                ReturnType.INTEGER,
//                1,
//                key.getBytes(),
//                txId.getBytes()
//        ));
        return SUCCESS.equals(result);
    }

    /**
     * 可重入锁
     *
     * @param key
     * @param txId
     * @return
     */
    public boolean reentrantLock(String key, String txId, long expire, TimeUnit timeUnit, int maxRetryTimes, boolean reenter) {
        try {
            if (reenter) {
                Integer state = getState();
                if (state != 0) {
                    // 不为0，说明已经拿到过锁了，直接加1然后延时
                    incrStateAndExpire(key, expire, timeUnit);
                    log.info("分布式锁重入并延时成功, key:{}, txId:{}", key, txId);
                    return true;
                }
            }

            //首次尝试获取锁
            boolean success = setNxPx(key, txId, expire, timeUnit);
            if (success) {
                if (reenter) {
                    incrStateAndExpire(key, expire, timeUnit);
                }
                return true;
            }
            //获取失败，进入重试
            success = retry(() -> setNxPx(key, txId, expire, timeUnit),
                    maxRetryTimes);
            if (success) {
                incrStateAndExpire(key, expire, timeUnit);
            }
            return success;
        } catch (Exception e) {
            log.error("redis分布式锁未知异常", e);
            return false;
        }
    }

    private void incrStateAndExpire(String key, long expire, TimeUnit timeUnit) {
        Integer state = getState();
        STATE.set(state + 1);
        redisTemplate.expire(key, expire, timeUnit);
    }

    private Integer getState() {
        return Optional.ofNullable(STATE.get()).orElse(0);
    }

    private boolean setNxPx(String key, String txId, long expire, TimeUnit timeUnit) {
        return Optional.ofNullable(redisTemplate.opsForValue().setIfAbsent(key, txId, expire, timeUnit)).orElse(false);
    }

    public boolean reentrantUnLock(String key, String txId) {
        decrState();
        Integer state = getState();
        if (state == 0) {
            return unlock(key, txId);
        }
        return true;
    }

    private void decrState() {
        Integer state = getState();
        if (state == 0) {
            STATE.remove();
        }
    }
}
