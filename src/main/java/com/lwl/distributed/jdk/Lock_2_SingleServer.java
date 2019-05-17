package com.lwl.distributed.jdk;



import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author liuweilong
 * @description
 * @date 2019/5/17 13:51
 */
public class Lock_2_SingleServer {
    private static Lock LOCK = new ReentrantLock();

    public boolean lock(){
       if (LOCK.tryLock()){
           LOCK.lock();
           return true;
       }
       return false;
    }

    public void unlock(){
        LOCK.unlock();
    }
}
