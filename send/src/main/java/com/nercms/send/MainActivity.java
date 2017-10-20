package com.nercms.send;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nercms.receive.Receive;
import com.nercms.receive.VideoPlayView;
import com.nercms.send.send.FfmpegServer;

import org.sipdroid.net.RtpPacket;
import org.sipdroid.net.RtpSocket;
import org.sipdroid.net.SipdroidSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity{



    private Button transBtn,queryBtn,videoBtn;
    private TextView textView;
    private  DatagramSocket datagramSocket;
    private static final int TIMEOUT = 3000;   // 设置超时为3秒
    private static final int MAXTRIES = 5;     // 最大重发次数5次
    private StringBuffer stringBuffer = new StringBuffer();

    private String deviceIp;//设备IP
    private String devicePort;//设备端口
    private boolean threadflag= false;
    private boolean heartthreadflag= false;

    private int loaclPort = 18228;



    private FfmpegServer ffmpegServer;
    private VideoPlayView clientView = null;      //对面大视图视频
    //----------------------------------------------------------------------------------------------

    private Receive decode;   //解码器

    private RtpSocket rtp_socket = null; //创建RTP套接字

    private RtpPacket rtp_receive_packet = null; //创建RTP接受包


    //接受 处理
    private long decoder_handle = 0; //拼帧器的句柄
    private byte[] frmbuf = new byte[65536]; //帧缓存
    private byte[] socket_receive_Buffer = new byte[2048]; //包缓存
    private byte[] buffer = new byte[2048];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        new Thread(){
            @Override
            public void run() {
                super.run();
                initRTP("192.168.100.210","19888");
            }
        }.start();

//        init();
//        startHearTask();
//        doReceive();

    }



    public void init() {
        try {
            datagramSocket = new DatagramSocket(loaclPort,InetAddress.getByName(Util.getIpAddressString()));
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }


    //开启接收线程
    public void doReceive() {
        ReceiveThread thread = new ReceiveThread();
        thread.start();
    }





    private void startHearTask()
    {
        Log.d("dfy","启动心跳线程");
        // 启动心跳线程
        //[2002^0001^201^WG12345678901235^^^^^^^^^用户本地IP^用户本地端口^^^^^^]
        stringBuffer.delete(0,stringBuffer.length());
        stringBuffer.append("[2002^0001^201^WG12345678901235^^^^^^^^^").append(Util.getIpAddressString()).append("^").append(String.valueOf(loaclPort)).append("^^^^^^]");
        String heartStr = stringBuffer.toString();
        try {
            DatagramPacket hp = new DatagramPacket(heartStr.getBytes(), heartStr.length(),InetAddress.getByName(Constant.SERVER_IP), Constant.SERVER_PORT);
            HeartThread thread = new HeartThread(hp);
            thread.start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

















    private void initView() {
        textView = (TextView)findViewById(R.id.showdata);
        transBtn = (Button)findViewById(R.id.transbutton);
        videoBtn = (Button)findViewById(R.id.startVideo);
        queryBtn = (Button)findViewById(R.id.querybutton);
        clientView = (VideoPlayView)findViewById(R.id.video_play);
        queryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String queryStr = "[2002^0002^201^WG12345678901235^^^^^^^^^^^^^^^^]";
                    // 构造要发送的包
                    DatagramPacket query_packet = new DatagramPacket(queryStr.getBytes(), queryStr.length(), InetAddress.getByName(Constant.SERVER_IP), Constant.SERVER_PORT);
                    datagramSocket.send(query_packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });




        transBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String send = "[2002^0003^201^WG12345678901235^^^^^^^^^^^^^^^^]";
                DatagramPacket p2;
                // 发包打洞,多发几次
                Log.d("dfy","deviceIp = "+deviceIp);
                Log.d("dfy","devicePort = "+devicePort);
                try {
                    p2 = new DatagramPacket(send.getBytes(),send.getBytes().length, InetAddress.getByName(deviceIp),Integer.parseInt(devicePort.trim()));
                    datagramSocket.send(p2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        videoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                VideoActivity.actionStart(MainActivity.this,deviceIp,Integer.parseInt(devicePort.trim()));
            }
        });

        decode = new Receive();
        threadflag = true;
        heartthreadflag = true;

    }






    private void initRTP(String deviceIp,String devicePort)
    {
        if (rtp_socket == null) {
            try {
                //rtp_socket = new RtpSocket(new SipdroidSocket(20000)); //初始化套接字，20000为接收端口号
                rtp_socket = new RtpSocket(new SipdroidSocket(20000), InetAddress.getByName(deviceIp), Integer.parseInt(devicePort));
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            //初始化接受包
            rtp_receive_packet = new RtpPacket(socket_receive_Buffer, 0); //初始化 ,socketBuffer改变时rtp_Packet也跟着改变
            /**
             * 因为可能传输数据过大 会将一次数据分割成好几段来传输
             * 接受方 根据序列号和结束符 来将这些数据拼接成完整数据
             */
            //初始化解码器
//            decoder_handle = decode.CreateH264Packer(); //创建拼帧器
//            decode.CreateDecoder(352, 288); //创建解码器
//            DecoderThread decoder = new DecoderThread();
//            decoder.start(); //启动一个线程

//            byte data[] = new byte[2048];
//            DatagramPacket packet = new DatagramPacket(data, 2048);
//            try {
//                rtp_socket.socket.receive(packet);
//                byte data2[] = packet.getData();// 接收的数据
//                LogUtil.d("dfy","收到数据"+new String(data2));
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }


            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket(Integer.parseInt(devicePort), InetAddress.getByName(deviceIp));
                //接收数据的buf数组并指定大小
                byte[] buf = new byte[1024];
                //创建接收数据包，存储在buf中
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                //接收操作
                socket.receive(packet);
                byte data[] = packet.getData();// 接收的数据
                LogUtil.d("dfy","收到数据"+new String(data));
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }




    /**
     * 接收rtp数据并解码 线程
     */
    class DecoderThread extends Thread {
        public void run() {
            while (threadflag) {
                try {
                    rtp_socket.receive(rtp_receive_packet); //接收一个包
                    LogUtil.d("dfy","接收数据");
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
                LogUtil.d("dfy", "序列号:" + sequence + " bMark:" + bMark + " packetSize:" + packetSize + " PayloadType:" + rtp_receive_packet.getPayloadType() + " timestamp:" + timestamp + " frmSize:" + frmSize);
                if (frmSize <= 0)
                    continue;

                decode.DecoderNal(frmbuf, frmSize, clientView.mPixel);//解码后的图像存在mPixel中

                //Log.d("log","序列号:"+sequence+" 包大小："+packetSize+" 时间："+timestamp+"  frmbuf[30]:"+frmbuf[30]);
                clientView.postInvalidate();
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
     * 接收服务器数据 线程
     */
    class ReceiveThread extends Thread {
        public void run() {
            try {
                byte[] buf = new byte[1024];
                DatagramPacket rp = new DatagramPacket(buf, 1024);
                while (threadflag) {
                    datagramSocket.receive(rp);
                    // 取出信息
                    final  String content = new String(rp.getData(), 0, rp.getLength());
//                    String rip = rp.getAddress().getHostAddress();
//                    int rport = rp.getPort();
                    // 输出接收到的数据
//                    LogUtil.d("dfy", "接收数据长度=" + rp.getLength());
                    LogUtil.d("dfy", "接收数据=" + content);
                    if(content.startsWith("[200"))
                    {
                        LogUtil.d("dfy", "接收数据=" + content);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textView.setText(content);
                            }
                        });
                        String[] strArr = content.split("\\^");
                        if(strArr[1].equals(Constant.HEART_JUMP))
                        {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    queryBtn.setEnabled(true);
                                }
                            });


                        }else if(strArr[1].equals(Constant.QUERY_ORDER))
                        {
                            //1代表不在同一局域网
                            //2 代表在同一局域网
                            if(strArr[11].equals("1")||strArr[11].equals("2"))
                            {
                                deviceIp = strArr[12];
                                devicePort = strArr[13];
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        transBtn.setEnabled(true);
                                    }
                                });



//
                            }else if(strArr[11].equals("0"))//离线
                            {

                            }
                        }else if(strArr[1].equals(Constant.THROUGH_ORDER))
                        {
                            threadflag = false;
                        }
                    }

                }
//                datagramSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }



//    private void playVideo()
//    {
//        if(TextUtils.isEmpty(deviceIp)||(TextUtils.isEmpty(devicePort)))
//        {
//            Toast.makeText(getApplicationContext(),"未获取设备端Ip和端口，无法视频",Toast.LENGTH_LONG);
//            return;
//        }
//        ffmpegServer=new FfmpegServer(new VideoServer.ReceiveVideoCallback() {
//            @Override
//            public void receiveVideoStream(byte[] frmbuf, int frmSize, long timestamp) {
//
//                byte decode[]=ffmpegServer.ffmpeg.videodecode(frmbuf);
//                YuvImage image = new YuvImage(decode, ImageFormat.NV21, 352, 288, null);            //ImageFormat.NV21  640 480
//                ByteArrayOutputStream outputSteam = new ByteArrayOutputStream();
//                image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 70, outputSteam); // 将NV21格式图片，以质量70压缩成Jpeg，并得到JPEG数据流
//                clientView.jpegData = outputSteam.toByteArray();
//                clientView.postInvalidate();
//
//
//            }
//        }, deviceIp, Integer.parseInt(devicePort));
//
//        ffmpegServer.doStart();
//    }


    class HeartThread extends Thread {
        private DatagramPacket p;
        public HeartThread(DatagramPacket p) {
            this.p = p;
        }

        public void run() {
                while (heartthreadflag) {
                    try {
                        datagramSocket.send(p);
                        Thread.sleep(45000);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


        }

    }




    public void close() {

        if(datagramSocket!=null)
        {
            if (!datagramSocket.isClosed())
                datagramSocket.close();
            datagramSocket.disconnect();
            datagramSocket = null;
        }
//        ffmpegServer.stopServer();


        if (rtp_socket != null) {
            rtp_socket.close();
            rtp_socket = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        threadflag  = false;
        heartthreadflag = false;
        close();
    }














}
