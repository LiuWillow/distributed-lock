package com.lwl.distributed.jdk;


import com.lwl.distributed.lock.IDistributedLock;
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

/**
 * @author liuweilong
 * @description
 * @date 2019/5/17 13:51
 * 基于netty实现的分布式锁客户端
 */
@Service("jdkLock")
public class Lock_2_SingleLock implements IDistributedLock {
    private static final String LOCK = "1";
    private static final String UN_LOCK = "0";
    private static final String SUCCESS = "1";
    private static final String FAILED = "0";
    private int port = 9821;

    private LockClientHandler sender;


    @PostConstruct
    public void init(){
        //注入发送器
        this.sender = new LockClientHandler();

        //初始化客户端并建立连接
        Bootstrap client = new Bootstrap();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        client.group(worker)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        //$作为分隔符，解决粘包问题
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

        //发送上锁的消息给服务端
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
