package com.lwl.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * date  2019/5/19
 * author liuwillow
 **/
public class LockServerHandler extends ChannelInboundHandlerAdapter {
    private static final Map<String, Boolean> lockMap = new ConcurrentHashMap<>();
    private static final String LOCK = "1";
    private static final String UN_LOCK = "0";
    private static final String SUCCESS = "1";
    private static final String FAILED = "0";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) throws Exception {
        if (object == null) {
            return;
        }
        String objStr = (String) object;
        System.out.println("服务端收到消息： " + objStr);
        Msg msg = JSONObject.parseObject(objStr, Msg.class);
        Channel channel = ctx.channel();

        if (LOCK.equals(msg.getType())) {
            System.out.println(msg.getRequestId() + "发起加锁请求");
            boolean success = lock(channel, msg);
            if (success) {
                System.out.println(msg.getRequestId() + "请求锁成功");
            } else {
                System.out.println(msg.getRequestId() + "请求锁失败");
            }
        } else {
            System.out.println(msg.getRequestId() + "发起解锁请求");
            unlock(channel, msg);
            System.out.println(msg.getRequestId() + "解锁锁成功");
        }
    }

    private void unlock(Channel channel, Msg msg) {
        String key = msg.getKey();
        lockMap.put(key, false);
        msg.setSuccess(SUCCESS);
        channel.writeAndFlush(SUCCESS);
    }

    private boolean lock(Channel channel, Msg msg) throws InterruptedException {
        String key = msg.getKey();
        boolean success = tryLock(key);
        if (success){
            msg.setSuccess(SUCCESS);
            channel.writeAndFlush(Unpooled.copiedBuffer((JSON.toJSONString(msg) + "$").getBytes()));
            return success;
        }

        msg.setSuccess(FAILED);
        channel.writeAndFlush(Unpooled.copiedBuffer((JSON.toJSONString(msg) + "$").getBytes()));
        return false;
    }

    private boolean tryLock(String key) {
        synchronized (lockMap){
            Boolean isLock = lockMap.get(key);
            if (isLock == null || !isLock){
                lockMap.put(key, true);
                return true;
            }
        }
        return false;
    }
}
