package com.nercms.send;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * Created by dufangyu on 2017/5/24.
 */

public class MyService extends Service implements EchoClientHandler.Listener{

    private Context context;
    private EventLoopGroup group;
    private  Bootstrap bootstrap;
    private MyBinder myBinder = new MyBinder();
    private EchoClientHandler echoClientHandler;
    private String ip,port;
    private Messenger msg;



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {


        if(msg==null)
        {
            msg = (Messenger) intent.getExtras().get("messenger");
        }

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Looper.prepare();
//                Log.d("dfy","enter buildEcho");
//                try {
//                    bootstrap.group(group)
//                            .channel(NioDatagramChannel.class)
//                            .option(ChannelOption.SO_BROADCAST, true)
//                            .handler(echoClientHandler);
//                    bootstrap.bind(7777).sync().channel().closeFuture().await();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                } finally{
//                    group.shutdownGracefully();
//                }
//            }
//        }).start();





        return myBinder;
    }

    @Override
    public void onCreate() {
        context = this;
        bootstrap = new Bootstrap();
        group = new NioEventLoopGroup();
        echoClientHandler = new EchoClientHandler(context);
        echoClientHandler.setListener(this);
        super.onCreate();
    }

    public void stopGroup()
    {
        group.shutdownGracefully();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void receiveData(String value,String ip, String port) {
        this.ip = ip;
        this.port = port;
        try {
            Message message = Message.obtain();
            message.what = 1;
            Bundle bundle = new Bundle();
            bundle.putString("data",value);
            message.setData(bundle);
            msg.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void receiveTransDatga(String value) {
        try {
            Message message = Message.obtain();
            message.what = 2;
            Bundle bundle = new Bundle();
            bundle.putString("data",value);
            message.setData(bundle);
            msg.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    class MyBinder extends Binder{
        public void sendData()
        {
            echoClientHandler.sendData();
        }

        public  void startTransIp()
        {
            echoClientHandler.startTransIp(ip,port);
        }
    }

}
