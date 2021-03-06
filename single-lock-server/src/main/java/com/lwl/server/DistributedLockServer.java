package com.lwl.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * date  2019/5/19
 * author liuwillow
 * 基于netty实现的分布式锁服务端
 **/
public class DistributedLockServer {
    private static int PORT = 9821;
    public static void main(String[] args) {
        System.out.println("--------初始化netty-------");
        ServerBootstrap bootstrap = new ServerBootstrap();
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();

        try {
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            //$符号分割消息，解决粘包问题
                            ByteBuf byteBuf = Unpooled.copiedBuffer("$".getBytes());
                            pipeline.addLast(new DelimiterBasedFrameDecoder(1024, byteBuf));

                            pipeline.addLast("decoder", new StringDecoder());
                            pipeline.addLast("encoder", new StringEncoder());
                            pipeline.addLast("handler", new LockServerHandler());
                        }
                    })
                    .bind(PORT).sync();
        }catch (Exception e){
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }

    }
}
