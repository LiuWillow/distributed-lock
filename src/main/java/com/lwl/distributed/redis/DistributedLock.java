package com.lwl.distributed.redis;

/**
 * date  2019/5/19
 * author liuwillow
 **/
public interface DistributedLock {
    boolean lock(String key, String value);
    boolean unlock(String key, String value);
}
