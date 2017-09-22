package com.nercms.receive;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.sipdroid.net.RtpPacket;
import org.sipdroid.net.RtpSocket;
import org.sipdroid.net.SipdroidSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {
    private  VideoPlayView view = null;
    private Handler handler = new Handler();

    private boolean isRunning; //线程运行标志
    private Receive decode;   //解码器

    private RtpSocket rtp_socket = null; //创建RTP套接字

    private RtpPacket rtp_receive_packet = null; //创建RTP接受包

    private MyService service;

    //接受 处理
    private long decoder_handle = 0; //拼帧器的句柄
    private byte[] frmbuf = new byte[65536]; //帧缓存
    private byte[] socket_receive_Buffer = new byte[2048]; //包缓存
    private byte[] buffer = new byte[2048];
    private Context context;
    private Intent intent;
//    private Button sendBtn,tranIpBtn;
    private MyService.MyBinder myBinder;

    private Button sendBtn,transBtn;

    private TextView textView;


    private DatagramSocket ds;


    private String clientIp,clientPort;


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (MyService.MyBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                .penaltyLog().penaltyDeath().build());
        initView();
        context = this;
//        service = new MyService();
//        intent = new Intent(this,MyService.class);
//        startConnect();
        init();
        doReceive();
    }

    private void initView() {
//        view = (VideoPlayView) this.findViewById(R.id.video_play);
//        sendBtn = (Button)findViewById(R.id.sendData);
//        tranIpBtn= (Button)findViewById(R.id.transip);
//        sendBtn.setOnClickListener(this);
//        tranIpBtn.setOnClickListener(this);
//        decode = new Receive();







        textView = (TextView)findViewById(R.id.showdata);
        sendBtn = (Button)findViewById(R.id.sendbutton);
        transBtn = (Button)findViewById(R.id.transbutton);


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                myBinder.sendData();
                Log.d("dfy","发送数据");
                try {
                    String loginStr = "2222";
                    // 构造要发送的包
                    DatagramPacket lp = new DatagramPacket(loginStr.getBytes(), loginStr.length(), InetAddress.getByName(Util.SERVER_IP), Util.SERVER_PORT);
                    ds.send(lp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        transBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                myBinder.startTransIp();
                String send = "8888";
                DatagramPacket p2;
                // 发包打洞,多发几次

                Log.d("dfy","clientIp =" +clientIp);
                Log.d("dfy","clientPort =" +clientPort);
                try {
                    p2 = new DatagramPacket(send.getBytes(),
                            send.getBytes().length, InetAddress.getByName(clientIp), Integer.parseInt(clientPort));
                    ds.send(p2);
                    ds.send(p2);
                    ds.send(p2);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });





    }


    public void init() {
        try {
            ds = new DatagramSocket(6788, InetAddress.getByName("192.168.100.70"));
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void doReceive() {
        DecoderThread2 thread = new DecoderThread2();
        thread.start();
    }





    private void startConnect()
    {
        bindService(intent,connection,BIND_AUTO_CREATE);
    }





    @Override
    protected void onResume() {
        super.onResume();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                doStart();
//            }
//        },1000);

    }

    /**
     * 开启 接受 发送rtp线程  开启本地摄像头
     */
    public void doStart() {
        Log.d("dfy","doStart");
        //初始化解码器
        if (rtp_socket == null) {
            try {
                //rtp_socket = new RtpSocket(new SipdroidSocket(20000)); //初始化套接字，20000为接收端口号
                rtp_socket = new RtpSocket(new SipdroidSocket(19888));
               Log.d("dfy","receive  rtp_socket = "+rtp_socket);
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            //初始化接受包
            rtp_receive_packet = new RtpPacket(socket_receive_Buffer, 0); //初始化 ,socketBuffer改变时rtp_Packet也跟着改变
            //初始化解码器

            decoder_handle = decode.CreateH264Packer(); //创建拼帧器
            decode.CreateDecoder(352, 288); //创建解码器
            isRunning = true;
            DecoderThread decoder = new DecoderThread();
            decoder.start(); //启动一个线程

        }






    }




    /**
     * 接收rtp数据并解码 线程
     */
    class DecoderThread extends Thread {
        public void run() {
            while (isRunning) {
                try {
                    rtp_socket.receive(rtp_receive_packet); //接收一个包
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int packetSize = rtp_receive_packet.getPayloadLength(); //获取包的大小

                if (packetSize <= 0)
                    continue;
                if (rtp_receive_packet.getPayloadType() != 2) //确认负载类型为2
                    continue;
                System.arraycopy(socket_receive_Buffer, 12, buffer, 0, packetSize); //socketBuffer->buffer
                int sequence = rtp_receive_packet.getSequenceNumber(); //获取序列号
                long timestamp = rtp_receive_packet.getTimestamp(); //获取时间戳
                int bMark = rtp_receive_packet.hasMarker() == true ? 1 : 0; //是否是最后一个包
                int frmSize = decode.PackH264Frame(decoder_handle, buffer, packetSize, bMark, (int) timestamp, sequence, frmbuf); //packer=拼帧器，frmbuf=帧缓存
                Log.d("log", "序列号:" + sequence + " bMark:" + bMark + " packetSize:" + packetSize + " PayloadType:" + rtp_receive_packet.getPayloadType() + " timestamp:" + timestamp + " frmSize:" + frmSize);
                if (frmSize <= 0)
                    continue;

                decode.DecoderNal(frmbuf, frmSize, view.mPixel);//解码后的图像存在mPixel中

                //Log.d("log","序列号:"+sequence+" 包大小："+packetSize+" 时间："+timestamp+"  frmbuf[30]:"+frmbuf[30]);

                view.postInvalidate();
            }

            //关闭
            if (decoder_handle != 0) {
                decode.DestroyH264Packer(decoder_handle);
                decoder_handle = 0;
            }
            if (rtp_socket != null) {
                rtp_socket.close();
                rtp_socket = null;
            }
            decode.DestoryDecoder();
        }
    }

    /**
     * 关闭摄像头 并释放资源
     */
    public  void close() {
        isRunning = false;

        if (rtp_socket != null) {
            rtp_socket.close();
            rtp_socket = null;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        close();
//        service.stopGroup();
//        unbindService(connection);
    }

    class DecoderThread2 extends Thread {
        public void run() {
            try {
                byte[] buf = new byte[1024];
                DatagramPacket rp = new DatagramPacket(buf, 1024);
                boolean isEnd = false;
                while (!isEnd) {
                    ds.receive(rp);
                    // 取出信息
                    final  String content = new String(rp.getData(), 0, rp.getLength());
                    String rip = rp.getAddress().getHostAddress();
                    int rport = rp.getPort();
                    // 输出接收到的数据
                    Log.d("dfy", "接收数据=" + rip + ":" + rport + " >>>> " + content);


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(content);
                        }
                    });



                    String [] arr = content.split(",");
                    if(arr[0].equals("2"))
                    {
                        clientIp = arr[1];
                        clientPort = arr[2];
                    }


                    // 处理控制部分,委托给其他方法做
//            if (content.startsWith(MyProtocol.LIST_ONLINE)) {
//                dealListOnline(ds, rp, content);
//            } else if (content.startsWith(MyProtocol.PUNCH_HOLE_TO)) {
//                dealPunchTo(ds, rp, content);
//            } else if (content.startsWith(MyProtocol.CAN_P2P_TO)) {
//                firtTimeConnectP2P(ds, rp, content);
//            } else if (content.startsWith(MyProtocol.HELLO_P2P_FRIEND)) {
//            } else if (content.startsWith(MyProtocol.P2P_MESSAGE)) {
//            } else {
//            }
                }
                ds.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

}




