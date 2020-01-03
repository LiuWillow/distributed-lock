package com.lwl.distributed.redis;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author liuweilong
 * @description
 * @date 2019/5/17 10:56
 */
@Service("redisNxPxTxLua")
public class Lock_5_SetNxPx_TxId_Lua extends BaseRedisLock {
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
        String oldTxId = valueOperations.get(key);
        //比较txId是否一致，实现可重入锁
        if (Objects.nonNull(oldTxId) && oldTxId.equals(txId)) {
            // 延时
            redisTemplate.expire(key, EXPIRE, TimeUnit.MILLISECONDS);
            return true;
        }
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
}
