package com.lwl.distributed.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.Collections;

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
     * @param key
     * @return
     */
    @Override
    public boolean lock(String key, String txId) {
        //TODO 要循环几次
        RedisConnection connection = getConnection();
        Boolean success = connection.set(key.getBytes(), txId.getBytes(),
                Expiration.milliseconds(EXPIRE),
                RedisStringCommands.SetOption.ifAbsent());
        releaseConnection(connection);
        return success == null ? false : success;
    }

    /**
     * 利用lua脚本比较
     * @param key
     * @return
     */
    @Override
    public boolean unlock(String key, String txId) {
        //redisTemplate只能接受Long
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(casScript);
        redisScript.setResultType(Long.class);
        Object o = redisTemplate.execute(redisScript, Collections.singletonList(key),
                txId);
        //也可以用connection
//        Boolean execute = redisTemplate.execute((RedisConnection connection) -> connection.eval(
//                casScript.getBytes(),
//                ReturnType.INTEGER,
//                1,
//                key.getBytes(),
//                txId.getBytes()
//        ));
        Long result = (Long) o;
        return SUCCESS.equals(result);
    }
}
