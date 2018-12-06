package com.aekc.netty.fixedLength;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

public class Server {

    public static void main(String[] args) throws InterruptedException {

        // 用于处理服务器端接收客户端连接的
        EventLoopGroup pGroup = new NioEventLoopGroup();
        // 用于进行网络通信（网络读写）
        EventLoopGroup cGroup = new NioEventLoopGroup();
        // 2、创建辅助工具类，用于服务器通道的一系列配置
        ServerBootstrap bootstrap = new ServerBootstrap();
        // 绑定两个线程组
        bootstrap.group(pGroup, cGroup)
                // 指定NIO模式
                .channel(NioServerSocketChannel.class)
                // 设置tcp缓冲区
                .option(ChannelOption.SO_BACKLOG, 1024)
                // 设置发送缓冲大小
                .option(ChannelOption.SO_SNDBUF, 32 * 1024)
                // 设置接收缓冲大小
                .option(ChannelOption.SO_RCVBUF, 32 * 1024)
                // 保持连接
                .option(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        // 设置定长字符串接收
                        socketChannel.pipeline().addLast(new FixedLengthFrameDecoder((5)));
                        // 设置字符串形式的解码，这样就不用自己解析字符串。它会自动把byte解析为字符串
                        socketChannel.pipeline().addLast(new StringDecoder());
                        // 3、在这里配置具体数据接收方法的处理
                        socketChannel.pipeline().addLast(new ServerHandler());
                    }
                });
        // 4、经行绑定，异步阻塞
        ChannelFuture channelFuture = bootstrap.bind(8765).sync();

        System.out.println("Server start...");

        // 5、等待关闭，相当于Thread.sleep(Integer.MAX_VALUE)
        channelFuture.channel().closeFuture().sync();
        pGroup.shutdownGracefully();
        cGroup.shutdownGracefully();
    }
}
