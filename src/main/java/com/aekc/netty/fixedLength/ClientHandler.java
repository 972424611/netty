package com.aekc.netty.fixedLength;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            String request = (String) msg;
            System.out.println("Client: " + request);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // 管道激活后调用
        System.out.println("channel 激活...");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        System.out.println("读完毕...");
    }
}
