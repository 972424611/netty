package com.aekc.netty.basic;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class Client {

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new ClientHandler());
                    }
                });
        ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8765).sync();

        System.out.println("Client connected...");

        channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer("hello netty!!".getBytes()));
        Thread.sleep(1000);
        channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer("hello netty!!".getBytes()));
        Thread.sleep(1000);
        channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer("hello netty!!".getBytes()));

        // 等待客户端端口关闭
        channelFuture.channel().closeFuture().sync();
        group.shutdownGracefully();
    }
}
