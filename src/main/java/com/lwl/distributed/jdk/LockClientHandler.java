package com.lwl.distributed.jdk;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * date  2019/5/19
 * author liuwillow
 **/
public class LockClientHandler extends ChannelInboundHandlerAdapter {
    private ChannelHandlerContext context;
    public static ThreadLocal<Condition> localCondition = new ThreadLocal<>();
    public static ThreadLocal<String> localResult = new ThreadLocal<>();
    private static final String LOCK = "1";
    private static final String UN_LOCK = "0";
    private static final String SUCCESS = "1";
    private static final String FAILED = "0";

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.context = ctx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg == null) {
            return;
        }
        String str = (String) msg;
        localResult.set(str);
        localCondition.get().signal();
    }

    public String send(Msg msg) {
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        localCondition.set(condition);
        Channel channel = context.channel();
        channel.writeAndFlush(JSON.toJSONString(msg));
        try {
            condition.await(4000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return localResult.get();
    }
}
