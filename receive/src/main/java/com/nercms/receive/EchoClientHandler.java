package com.nercms.receive;

import android.content.Context;
import android.util.Log;

import java.net.InetSocketAddress;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

/**
 * Created by dufangyu on 2017/5/24.
 */

public class EchoClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private Context mContext;
    private ChannelHandlerContext ctx;

    private Listener listener;

    public void setListener(Listener listener)
    {
        this.listener = listener;
    }
    public EchoClientHandler(Context context)
    {
        mContext = context;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) throws Exception {

        Log.d("dfy","接收到信息:");
                 //服务器推送对方IP和PORT
                 ByteBuf buf = (ByteBuf) datagramPacket.copy().content();
                 byte[] req = new byte[buf.readableBytes()];
                 buf.readBytes(req);
                String str = new String(req, "UTF-8");
        Log.d("dfy","接收到的信息:"+ str);
                 String[] list = str.split(",");
                 //如果是A 则发送
                if(list[0].equals("2")){
                        String ip = list[1];
                         String port = list[2];
//                    this.ctx = channelHandlerContext;
                    listener.receiveData(ip,port);
//                    channelHandlerContext.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer("5678".getBytes()), new InetSocketAddress(ip, Integer.parseInt(port))));
//                    Thread.sleep(10000);
//                    channelHandlerContext.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer("5678".getBytes()), new InetSocketAddress(ip, Integer.parseInt(port))));
                     }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Log.d("dfy","客户端向服务器发送自己的IP和PORT");
        this.ctx = ctx;
//         ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer("2222".getBytes()), new InetSocketAddress(Util.SERVER_IP, Util.SERVER_PORT)));
        super.channelActive(ctx);
    }

    public void sendData()
    {
        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer("2222".getBytes()), new InetSocketAddress(Constant.SERVER_IP, Constant.SERVER_PORT)));
    }


    public void startTransIp(String ip,String port)
    {
        Log.d("dfy","开始穿透ip = "+ip);
        Log.d("dfy","开始穿透port = "+port);
        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer("5678".getBytes()), new InetSocketAddress(ip, Integer.parseInt(port))));




    }




    public interface Listener{
        void receiveData(String ip,String port);
    }

}






