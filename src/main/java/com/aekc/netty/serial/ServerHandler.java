package com.aekc.netty.serial;

import com.utils.GzipUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.io.FileOutputStream;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Req req = (Req) msg;
        System.out.println("Server: " + req.getId() + ", " + req.getName() + ", " + req.getRequestMessage());

        byte[] attachment = GzipUtils.unGzip(req.getAttachment());
        String path = System.getProperty("user.dir") + File.separatorChar + "receive" + File.separatorChar + "001.jpg";
        FileOutputStream fileOutputStream = new FileOutputStream(path);
        fileOutputStream.write(attachment);
        fileOutputStream.close();

        Resp resp = new Resp();
        resp.setId(req.getId());
        resp.setName("resp" + req.getId());
        resp.setResponseMessage("响应内容" + req.getId());
        ctx.writeAndFlush(resp);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}
