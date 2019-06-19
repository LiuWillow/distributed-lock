package com.lwl.distributed.jdk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    private static Map<String, Condition> conditionMap = new ConcurrentHashMap<>();
    private static Map<String, Lock> lockMap = new ConcurrentHashMap<>();
    private static Map<String, String> resultMap = new ConcurrentHashMap<>();
    private static final String LOCK = "1";
    private static final String UN_LOCK = "0";
    private static final String SUCCESS = "1";
    private static final String FAILED = "0";

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.context = ctx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) {
        if (object == null) {
            return;
        }
        String str = (String) object;
        Msg msg = JSONObject.parseObject(str, Msg.class);
        String requestId = msg.getRequestId();
        resultMap.put(requestId, msg.getSuccess());
        Lock lock = lockMap.get(requestId);
        try {
            lock.lock();
            Condition condition = conditionMap.get(requestId);
            condition.signal();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public String send(Msg msg) {
        //保存请求id对应的lock和condition
        Lock lock = new ReentrantLock();
        String requestId = msg.getRequestId();
        lockMap.put(requestId, lock);
        Condition condition = lock.newCondition();
        conditionMap.put(requestId, condition);

        //发消息
        Channel channel = context.channel();
        channel.writeAndFlush(Unpooled.copiedBuffer((JSON.toJSONString(msg) + "$").getBytes()));
        try {
            lock.lock();
            condition.await();
        } catch (InterruptedException e) {
            return FAILED;
        } finally {
            lock.unlock();
        }
        return resultMap.get(requestId);
    }
}
