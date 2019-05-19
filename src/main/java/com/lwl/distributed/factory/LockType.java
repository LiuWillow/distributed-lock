package com.lwl.distributed.factory;

/**
 * date  2019/5/19
 * author liuwillow
 **/
public class LockType {
    public static final String JDK_SYNCHRONIZED = "jdkSynchronized";
    public static final String JDK_LOCK = "jdkLock";
    public static final String REDIS_DIRECT = "redisDirect";
    public static final String REDIS_DIRECT_PX = "redisDirectPx";
    public static final String REDIS_NX_PX = "redisNxPx";
    public static final String REDIS_NX_PX_TX = "redisNxPxTx";
    public static final String REDIS_NX_PX_TX_LUA = "redisNxPxTxLua";
    public static final String RED_LOCK = "redLock";
}
