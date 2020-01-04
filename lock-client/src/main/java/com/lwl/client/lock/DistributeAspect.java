package com.lwl.client.lock;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


/**
 * author liuweilong
 * date 2019/12/30 6:34 下午
 * desc
 */
@Aspect
@Component
@Order(100)
@Slf4j
public class DistributeAspect {
    @Autowired
    private DistributeExecutor distributeExecutor;

    @Pointcut("@annotation(com.lwl.client.lock.DistributedLock)")
    public void distributedLockPointCut() {}

    @Around("distributedLockPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        DistributedLock annotation = signature.getMethod().getAnnotation(DistributedLock.class);
        String key = annotation.key();
        String txId = annotation.txId();
        return distributeExecutor.exec(() -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                log.error("执行分布式锁对应的逻辑出现异常", throwable);
                return null;
            }
        }, key, txId);
    }
}