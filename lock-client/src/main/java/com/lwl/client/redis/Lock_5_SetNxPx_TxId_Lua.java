package com.lwl.client.redis;

import com.google.common.collect.Lists;
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
    private static NamedThreadLocal<Integer> CURRENT_STATE = new NamedThreadLocal<>("redis_distribute_lock_state");
    /**
     * 锁的初始化状态
     */
    private static final Integer STATE_INIT = 0;

    @Autowired
    public StringRedisTemplate redisTemplate;
    /**
     * 删除脚本，如果key对应的值能匹配上，就删除key
     */
    private static final String CAS_DELETE_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] " +
            "then return redis.call('del', KEYS[1]) " +
            "else return 0 end";

    /**
     * 锁延时脚本，如果txId一致则延时，否则延时失败
     */
    private static final String CAS_EXPIRE_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] " +
            "then return redis.call('expire', KEYS[1], ARGV[2]) " +
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
     * 利用lua脚本比较并删除
     *
     * @param key
     * @return
     */
    @Override
    public boolean unlock(String key, String txId) {
        try {
            //redisTemplate只能接受Long
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(CAS_DELETE_SCRIPT);
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
        } catch (Exception e) {
            log.error("分布式锁解锁异常", e);
            return false;
        }

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
                if (!STATE_INIT.equals(state)) {
                    // 不为0，说明已经拿到过锁了，直接加1然后延时
                    boolean success = incrStateAndExpire(key, txId, expire, timeUnit);
                    log.info("Thread-{}分布式锁重入并延时{}, key:{}, txId:{}", success ? "成功" : "失败",
                            Thread.currentThread().getId(), key, txId);
                    return success;
                }
            }

            //首次尝试获取锁
            boolean success = setNxPx(key, txId, expire, timeUnit);
            if (success) {
                if (reenter) {
                    incrState();
                }
                return true;
            }
            //获取失败，进入重试
            success = retry(() -> setNxPx(key, txId, expire, timeUnit),
                    maxRetryTimes);
            if (success && reenter) {
                incrState();
            }
            return success;
        } catch (Exception e) {
            log.error("redis分布式锁未知异常", e);
            return false;
        }
    }

    private void incrState() {
        Integer state = getState();
        CURRENT_STATE.set(++state);
    }

    /**
     * state自增并延时key
     * @param key
     * @param txId
     * @param expire
     * @param timeUnit
     * @return
     */
    private boolean incrStateAndExpire(String key, String txId, long expire, TimeUnit timeUnit) {
        incrState();
        return casExpire(key, txId, expire, timeUnit);
    }

    /**
     * 比较key对应的值是否与给定值一致，是则延时
     * @param key
     * @param txId
     * @param expire
     * @param timeUnit
     * @return
     */
    private boolean casExpire(String key, String txId, long expire, TimeUnit timeUnit) {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(CAS_EXPIRE_SCRIPT);
        redisScript.setResultType(Long.class);
        //只能用秒，而且必须转换为字符串
        long secondsExpire = TimeUnit.SECONDS.convert(expire, timeUnit);
        Long result = redisTemplate.execute(redisScript, Lists.newArrayList(key),
                txId, secondsExpire + "");
        return SUCCESS.equals(result);
    }

    /**
     * 获取state，如果为空则返回0
     * @return
     */
    private Integer getState() {
        return Optional.ofNullable(CURRENT_STATE.get()).orElse(STATE_INIT);
    }

    private boolean setNxPx(String key, String txId, long expire, TimeUnit timeUnit) {
        return Optional.ofNullable(redisTemplate.opsForValue().setIfAbsent(key, txId, expire, timeUnit)).orElse(false);
    }

    /**
     * 分布式锁解锁
     * @param key
     * @param txId
     * @return
     */
    public boolean reentrantUnLock(String key, String txId) {
        Integer state = decrState();
        if (STATE_INIT.equals(state)) {
            return unlock(key, txId);
        }
        return true;
    }

    /**
     * state减一
     * @return 自减后的state
     */
    private Integer decrState() {
        Integer state = getState();
        if (STATE_INIT.equals(state)) {
            //如果减了之后是0，则手动删除该线程的threadLocal
            CURRENT_STATE.remove();
            return state;
        }
        CURRENT_STATE.set(--state);
        return state;
    }
}
