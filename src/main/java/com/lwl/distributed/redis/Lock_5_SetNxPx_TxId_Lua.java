package com.lwl.distributed.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.types.Expiration;
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

    /**
     * 调用set  传入nx和px参数，值为可以唯一标识当前线程的值
     * @param key
     * @return
     */
    @Override
    public boolean lock(String key, String txId) {
        //TODO 要循环几次
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        Boolean success = connection.set(key.getBytes(), txId.getBytes(),
                Expiration.milliseconds(EXPIRE),
                RedisStringCommands.SetOption.ifAbsent());
        return success == null ? false : success;
    }

    /**
     * 利用lua脚本比较
     * @param key
     * @return
     */
    @Override
    public boolean unlock(String key, String txId) {
        DefaultRedisScript redisScript = new DefaultRedisScript();
        String script = "if redis.call('get', KEYS[1] == ARGV[1] ) " +
                "then return redis.call('del', KEYS[1]) " +
                "else return 0 end";
        redisScript.setScriptText(script);

        Object o = redisTemplate.execute(redisScript, Collections.singletonList(key),
                txId);
        return true;
//        return success == null ? false : success;
    }
}
