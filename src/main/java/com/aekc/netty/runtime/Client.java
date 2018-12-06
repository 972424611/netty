package com.aekc.netty.runtime;

import com.aekc.netty.serial.MarshallingCodeFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.util.concurrent.TimeUnit;


public class Client {

    private EventLoopGroup group;

    private Bootstrap bootstrap;

    private ChannelFuture channelFuture;

    private static class SingletonHolder {
        static final Client instance = new Client();
    }

    public static Client getInstance(){
        return SingletonHolder.instance;
    }

    private Client() {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(MarshallingCodeFactory.buildMarshallingDecoder());
                        socketChannel.pipeline().addLast(MarshallingCodeFactory.buildMarshallingEncoder());
                        // 超时handler，当服务端与客户端在指定时间以上没有任何进行通信，则会关闭响应的通道，主要为减小服务端资源占用。
                        socketChannel.pipeline().addLast(new ReadTimeoutHandler(5));
                        socketChannel.pipeline().addLast(new ClientHandler());
                    }
                });
    }

    public void connect() {
        try {
            this.channelFuture = bootstrap.connect("127.0.0.1", 8765).sync();
            System.out.println("远程服务器已经连接，可以进行数据交换..");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public ChannelFuture getChannelFuture() {
        if(this.channelFuture == null) {
            this.connect();
        }
        if(!this.channelFuture.channel().isActive()) {
            this.connect();
        }
        return this.channelFuture;
    }

    public static void main(String[] args) throws Exception {
        final Client client = Client.getInstance();
        ChannelFuture channelFuture = client.getChannelFuture();
        for(int i = 1; i <= 3; i++) {
            Request request = new Request();
            request.setId("" + i);
            request.setName("pro" + i);
            request.setRequestMessage("数据信息" + i);
            channelFuture.channel().writeAndFlush(request);
            // 如果这里设置超过5秒，就会断开与服务端的连接
            TimeUnit.SECONDS.sleep(5);
        }
        channelFuture.channel().closeFuture().sync();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("进入子线程...");
                    ChannelFuture channelFuture = client.getChannelFuture();
                    System.out.println(channelFuture.channel().isActive());
                    System.out.println(channelFuture.channel().isOpen());

                    //再次发送数据
                    Request request = new Request();
                    request.setId("" + 4);
                    request.setName("pro" + 4);
                    request.setRequestMessage("数据信息" + 4);
                    channelFuture.channel().writeAndFlush(request);
                    channelFuture.channel().closeFuture().sync();
                    System.out.println("子线程结束.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        System.out.println("断开连接，主线程结束..");
    }
}
