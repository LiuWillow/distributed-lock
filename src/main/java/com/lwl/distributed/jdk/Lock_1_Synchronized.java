package com.lwl.distributed.jdk;

import org.springframework.stereotype.Service;

/**
 * @author liuweilong
 * @description
 * @date 2019/5/17 13:54
 */
@Service("jdkSynchronized")
public class Lock_1_Synchronized {
    public synchronized void order(){
        //下单操作
    }
}
