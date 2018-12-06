package com.aekc.netty.serial;

import com.utils.GzipUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.File;
import java.io.FileInputStream;

public class Client {

    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        socketChannel.pipeline().addLast(MarshallingCodeFactory.buildMarshallingDecoder());
                        socketChannel.pipeline().addLast(MarshallingCodeFactory.buildMarshallingEncoder());

                        socketChannel.pipeline().addLast(new ClientHandler());
                    }
                });
        ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8765).sync();

        Req req = new Req();
        req.setId(1 + "");
        req.setName("pro1");
        req.setRequestMessage("数据信息1");

        String path = System.getProperty("user.dir") + File.separatorChar + "sources" + File.separatorChar + "001.jpg";
        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] data = new byte[fileInputStream.available()];
        fileInputStream.read(data);
        fileInputStream.close();
        req.setAttachment(GzipUtils.gzip(data));
        channelFuture.channel().writeAndFlush(req);

        // 等待客户端端口关闭
        channelFuture.channel().closeFuture().sync();
        group.shutdownGracefully();
    }
}
