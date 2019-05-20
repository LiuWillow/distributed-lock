package com.lwl.distributed;

/**
 * date  2019/5/19
 * author liuwillow
 **/
public interface IDistributedLock {
    long EXPIRE = 3000;
    long WAIT_TIME = 2000;
    boolean lock(String key, String value);
    boolean unlock(String key, String value);
}
