package com.aekc.netty.sign;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String request = (String) msg;
        System.out.println("Server: " + request);
        // 服务器给客户端的响应
        String response = "Hi Client!!$_";
        // 因为这里有写操作，所以不用显示释放ByteBuf对象。
        ctx.writeAndFlush(Unpooled.copiedBuffer(response.getBytes()));
                // 默认是长连接，如果想要传输完就断开加上这个设置
                //.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
