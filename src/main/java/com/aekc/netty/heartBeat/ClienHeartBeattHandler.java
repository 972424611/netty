package com.aekc.netty.heartBeat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;

public class ClienHeartBeattHandler extends ChannelInboundHandlerAdapter {

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    private ScheduledFuture<?> heartBeat;

	// 主动向服务器发送认证信息
    private InetAddress addr;
    
    private static final String SUCCESS_KEY = "auth_success_key";

    private String getHostIp() {
        try{
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()){
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()){
                    InetAddress ip = (InetAddress) addresses.nextElement();
                    if (ip instanceof Inet4Address
                            && !ip.isLoopbackAddress() //loopback地址即本机地址，IPv4的loopback范围是127.0.0.0 ~ 127.255.255.255
                            && !ip.getHostAddress().contains(":")){
                        return ip.getHostAddress();
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;

    }

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		/*addr = InetAddress.getLocalHost();
        String ip = addr.getHostAddress();*/
		String ip = getHostIp();
		String key = "1234";
		//证书
		String auth = ip + "," + key;
		ctx.writeAndFlush(auth);
	}
	
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    	try {
        	if(msg instanceof String){
        		String ret = (String)msg;
        		if(SUCCESS_KEY.equals(ret)){
        	    	// 握手成功，主动发送心跳消息
        	    	this.heartBeat = this.scheduler.scheduleWithFixedDelay(new HeartBeatTask(ctx), 0, 2, TimeUnit.SECONDS);
        		    System.out.println(msg);    			
        		}
        		else {
        			System.out.println(msg);
        		}
        	}
		} finally {
			ReferenceCountUtil.release(msg);
		}
    }

    private class HeartBeatTask implements Runnable {
    	private final ChannelHandlerContext ctx;

		public HeartBeatTask(final ChannelHandlerContext ctx) {
		    this.ctx = ctx;
		}
	
		@Override
		public void run() {
			try {
			    RequestInfo info = new RequestInfo();
			    //ip
			    //info.setIp(addr.getHostAddress());
                info.setIp(getHostIp());
		        Sigar sigar = new Sigar();
		        //cpu prec
		        CpuPerc cpuPerc = sigar.getCpuPerc();
		        HashMap<String, Object> cpuPercMap = new HashMap<String, Object>();
		        cpuPercMap.put("combined", cpuPerc.getCombined());
		        cpuPercMap.put("user", cpuPerc.getUser());
		        cpuPercMap.put("sys", cpuPerc.getSys());
		        cpuPercMap.put("wait", cpuPerc.getWait());
		        cpuPercMap.put("idle", cpuPerc.getIdle());
		        // memory
		        Mem mem = sigar.getMem();
				HashMap<String, Object> memoryMap = new HashMap<String, Object>();
				memoryMap.put("total", mem.getTotal() / 1024L);
				memoryMap.put("used", mem.getUsed() / 1024L);
				memoryMap.put("free", mem.getFree() / 1024L);
				info.setCpuPercMap(cpuPercMap);
			    info.setMemoryMap(memoryMap);
			    ctx.writeAndFlush(info);
			    
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
	    	cause.printStackTrace();
			if (heartBeat != null) {
			    heartBeat.cancel(true);
			    heartBeat = null;
			}
			ctx.fireExceptionCaught(cause);
	    }
	    
	}
}
