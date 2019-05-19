package com.lwl.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * date  2019/5/19
 * author liuwillow
 **/
public class LockServerHandler extends ChannelInboundHandlerAdapter {
    private static Lock lock = new ReentrantLock();
    private static final int RETRY_TIMES = 4;
    private static final long RETRY_TIME_OUT = 2000;
    private static final String LOCK = "1";
    private static final String UN_LOCK = "0";
    private static final String SUCCESS = "1";
    private static final String FAILED = "0";



    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg == null){
            return;
        }
        Channel channel = ctx.channel();
        if (LOCK.equals(msg)) {
            lock(channel);
        } else {
            unlock(channel);
        }
    }

    private void unlock(Channel channel) {
        lock.unlock();
        channel.writeAndFlush(SUCCESS);
    }

    private void lock(Channel channel) throws InterruptedException {
        boolean success = false;
        if (lock.tryLock()) {
            lock.lock();
            success = true;
        }
        if (success) {
            channel.writeAndFlush(SUCCESS);
            return;
        }
        for (int i = 0; i < RETRY_TIMES && !success; i++) {
            Thread.sleep(RETRY_TIME_OUT);
            if (lock.tryLock()) {
                lock.lock();
                success = true;
            }
        }
        if (success) {
            channel.writeAndFlush(SUCCESS);
            return;
        }
        channel.writeAndFlush(FAILED);
    }
}
