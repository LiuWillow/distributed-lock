package com.lwl.distributed.jdk;


import com.lwl.distributed.redis.DistributedLock;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author liuweilong
 * @description
 * @date 2019/5/17 13:51
 */
@Service("jdkLock")
public class Lock_2_SingleLockClient implements DistributedLock {
    private static final String LOCK = "1";
    private static final String UN_LOCK = "0";
    private static final String SUCCESS = "1";
    private static final String FAILED = "0";
    private int port = 9821;

    private LockClientHandler sender;


    @PostConstruct
    public void init(){
        this.sender = new LockClientHandler();

        Bootstrap client = new Bootstrap();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        client.group(worker)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        ByteBuf byteBuf = Unpooled.copiedBuffer("$".getBytes());
                        pipeline.addLast(new DelimiterBasedFrameDecoder(1024, byteBuf));
                        pipeline.addLast("encoder", new StringEncoder());
                        pipeline.addLast("decoder", new StringDecoder());
                        pipeline.addLast("handler", sender);
                    }
                });

        try {
            client.connect("localhost", port).sync();
        } catch (InterruptedException e) {
            worker.shutdownGracefully();
        }

    }

    @Override
    public boolean lock(String key, String value) {
        Msg msg = new Msg();
        msg.setRequestId(Thread.currentThread().getName());
        msg.setType(LOCK);
        msg.setKey(key);
        String result = sender.send(msg);
        return SUCCESS.equals(result);
    }

    @Override
    public boolean unlock(String key, String value) {
        Msg msg = new Msg();
        msg.setRequestId(Thread.currentThread().getName());
        msg.setType(UN_LOCK);
        msg.setKey(key);
        String result = sender.send(msg);
        return SUCCESS.equals(result);
    }
}
