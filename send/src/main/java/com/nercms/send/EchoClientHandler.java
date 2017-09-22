package com.nercms.send;

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
                 //服务器推送对方IP和PORT
                 ByteBuf buf = (ByteBuf) datagramPacket.copy().content();
                 byte[] req = new byte[buf.readableBytes()];
                 buf.readBytes(req);
                String str = new String(req, "UTF-8");
                 String[] list = str.split(",");
        Log.d("dfy","接收到了消息···str = "+str);

                 //如果是A 则发送
         if(list[0].equals("1")){
                        String ip = list[1];
                         String port = list[2];
                    listener.receiveData(str,ip,port);
//                    channelHandlerContext.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer("打洞信息".getBytes()), new InetSocketAddress(ip, Integer.parseInt(port))));
//                       Thread.sleep(1000);
//                    channelHandlerContext.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer("P2P info..".getBytes()), new InetSocketAddress(ip, Integer.parseInt(port))));
                }else{
             listener.receiveTransDatga(str);
         }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        this.ctx = ctx;

//        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer("1111".getBytes()), new InetSocketAddress(Util.SERVER_IP, Util.SERVER_PORT)));
        super.channelActive(ctx);
    }


    public interface Listener{
        void receiveData(String value,String ip,String port);
        void receiveTransDatga(String value);
    }

    public void sendData()
    {
        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer("1111".getBytes()), new InetSocketAddress(Constant.SERVER_IP, Constant.SERVER_PORT)));
    }



    public void startTransIp(String ip,String port)
    {
        Log.d("dfy","开始穿透ip = "+ip);
        Log.d("dfy","开始穿透port = "+port);
        ctx.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer("5678".getBytes()), new InetSocketAddress(ip, Integer.parseInt(port))));



    }

}






