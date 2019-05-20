package com.lwl.distributed.jdk;

import org.springframework.stereotype.Service;

/**
 * @author liuweilong
 * @description
 * @date 2019/5/17 13:54
 * 如果业务服务为单机部署，直接在业务接口上加synchronized
 */
@Service("jdkSynchronized")
public class Lock_1_Synchronized {
    public synchronized void order(){
        //下单操作
    }
}
