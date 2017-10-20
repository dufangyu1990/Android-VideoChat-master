package com.nercms.send;

import android.content.Context;
import android.util.Log;

import com.zsg.ffmpegvideolib.Ffmpeg;

import org.sipdroid.net.RtpPacket;
import org.sipdroid.net.RtpSocket;
import org.sipdroid.net.SipdroidSocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by zsg on 2017/3/16.
 */
public class FfmpegServer {


    //private Send encode;      //编码器
    //public Receive decode;   //解码器
    private boolean isRunning; //线程运行标志

    private RtpSocket rtp_socket = null; //创建RTP套接字

    private  RtpPacket rtp_send_packet = null; //创建RTP发送包
    private RtpPacket rtp_receive_packet = null; //创建RTP接受包

    //接受 处理
    //private long decoder_handle = 0; //拼帧器的句柄
    private byte[] frmbuf = new byte[65536]; //帧缓存
    private byte[] socket_receive_Buffer = new byte[10240]; //包缓存
    private byte[] buffer = new byte[2048];

    //发送
    private long encoder_handle = -1; //创建编码器的句柄
    private int send_packetNum = 0; //包的数目
    private int[] send_packetSize = new int[200]; //包的尺寸
   // private byte[] send_stream = new byte[65536]; //码流
    private byte[] socket_send_Buffer = new byte[65536]; //缓存 stream->socketBuffer->rtp_socket

    private int server_video_port;
    private String clientIp;

    private Context context;

//    private VideoServer.ReceiveVideoCallback receiveVideoCallback;

    private SendDataThread sendDataThread;
    private boolean isSendThreadStart = false;
    public static long sendDatatime = 0;
    private StringBuffer stringBuffer = new StringBuffer();
    InetSocketAddress serverAddress;        //服务器地址
    InetSocketAddress clientAddress;        //客户端地址   用于p2p

    int type;

    private static String GET_CLIENT_IP_CMD = "001";            //向服务器获取对方ip和端口
    private static String GET_CLIENT_IP_RSP = "002";
    private static String REPLAY_CMD = "003";
    private static String REPLAY_RSP = "004";

    public Ffmpeg ffmpeg;

//    public FfmpegServer(VideoServer.ReceiveVideoCallback callback, int server_port) {
//        this.receiveVideoCallback = callback;
//        this.server_video_port = server_port;
//        ffmpeg = new Ffmpeg();
//        initServer();
//    }
//
//
//
//    public FfmpegServer(VideoServer.ReceiveVideoCallback callback, String clientIp,int server_port) {
//        this.receiveVideoCallback = callback;
//        this.server_video_port = server_port;
//        this.clientIp = clientIp;
//        ffmpeg = new Ffmpeg();
//        initServer();
//    }

    public FfmpegServer(String clientIp,int server_port) {
        this.server_video_port = server_port;
        this.clientIp = clientIp;
        ffmpeg = new Ffmpeg();
        initServer();
    }


    public void initServer() {
        // encode = new Send();
        // decode = new Receive();
        ffmpeg.videoinit();
        isRunning = true;
        //初始化发送包
        rtp_send_packet = new RtpPacket(socket_send_Buffer, 0);
//        serverAddress = new InetSocketAddress(Config.serverIP, server_video_port);
        Log.d("dfy","clientIp = "+clientIp+",server_video_port = "+server_video_port);
        serverAddress = new InetSocketAddress(clientIp, server_video_port);
        try {
            //rtp_socket = new RtpSocket(new SipdroidSocket(20000)); //初始化套接字，20000为接收端口号
            rtp_socket = new RtpSocket(new SipdroidSocket(20000));
            //doStart();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }


        //throughNet();

    }


//    /**
//     * 心跳线程
//     */
//    private void startHearTask()
//    {
//        Log.d("dfy","启动心跳线程");
//        // 启动心跳线程
//        //[2002^0001^201^WG12345678901235^^^^^^^^^用户本地IP^用户本地端口^^^^^^]
//        stringBuffer.delete(0,stringBuffer.length());
//        stringBuffer.append("[2001^0001^201^WG12345678901235^^^^^^^^^").append(Util.getIpAddressString()).append("^").append(String.valueOf(localPort)).append("^^^^^^]");
//        String heartStr = stringBuffer.toString();
//        try {
//            DatagramPacket hp = new DatagramPacket(heartStr.getBytes(), heartStr.length(),InetAddress.getByName(Constant.SERVER_IP), Constant.SERVER_PORT);
//            HeartThread thread = new HeartThread(hp);
//            thread.start();
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//    }




    /**
     * 发起net穿透
     */
    /*private void throughNet() {

        Thread thread = new Thread(new Runnable() {
            boolean b = true;
            byte data[] = new byte[2048];
            DatagramPacket packet = new DatagramPacket(data, 2048);

            @Override
            public void run() {
                String msg = "QC@001@00";
                try {
                    rtp_socket.send(msg, serverAddress);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while (b) {
                    try {
                        rtp_socket.socket.receive(packet);
                        String receivemsg = new String(data).trim();
                        Log.d("dfy", "收到消息：" + receivemsg);
                        String m[] = receivemsg.split("[@]");
                        if (m.length > 0 && m[1].equals(GET_CLIENT_IP_RSP)) {
                            Log.d("InetSocketAddress", m[2] + " " + m[3]);
                            clientAddress = new InetSocketAddress(m[2], Integer.parseInt(m[3]) + 1);
                            rtp_socket.send("QC@003@00", clientAddress);
                            rtp_socket.send("QC@003@00", serverAddress);
                            b = false;
                            doStart();
                        } else if (m.length > 0 && m[1].equals(REPLAY_RSP)) {

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }*/




    int count = 0;
    int sequence=0;
    public byte[] sendData(byte[] frame) {

//        Log.d("dfy","数据："+ Arrays.toString(frame));

        final byte[] send_stream = ffmpeg.videoencode(frame);
        //通过RTP协议发送帧
        final long timestamp = System.currentTimeMillis(); //设定时间戳
        sendDatatime = timestamp;
        /**
         * 因为可能传输数据过大 会将一次数据分割成好几段来传输
         * 接受方 根据序列号和结束符 来将这些数据拼接成完整数据
         * sendDataThread 发送数据线程
         */
        if(!isSendThreadStart)
        {
            isSendThreadStart = true;
            sendDataThread = new SendDataThread();
            sendDataThread.setSendStream(send_stream);
            sendDataThread.setSendTime(timestamp);
            sendDataThread.start();
        }



        return ffmpeg.videodecode(send_stream);

    }



     class SendDataThread extends Thread {

        private  byte[] send_stream = null;
        private  long timestamp =0;
        public void setSendStream(byte[] send_stream)
        {
            this.send_stream = send_stream;
        }

        public void setSendTime(long timestamp)
        {
            this.timestamp = timestamp;
        }

        public void run() {
            rtp_send_packet.setPayloadType(2);//定义负载类型，视频为2
            rtp_send_packet.setMarker(true); //是否是最后一个RTP包
            rtp_send_packet.setSequenceNumber(sequence); //序列号依次加1
            rtp_send_packet.setTimestamp(timestamp); //时间戳

            //Log.d("log", "序列号:" + sequence + " 时间：" + timestamp);
            rtp_send_packet.setPayloadLength(send_stream.length); //包的长度，packetSize[i]+头文件
            //从码流stream的pos处开始复制，从socketBuffer的第12个字节开始粘贴，packetSize为粘贴的长度
            System.arraycopy(send_stream, 0, socket_send_Buffer, 12, send_stream.length); //把一个包存在socketBuffer中
            //rtp_packet.setPayload(socketBuffer, rtp_packet.getLength());
            //Log.e(Config.TAG, "发送 timestamp:" + timestamp+"  发送大小："+rtp_send_packet.getLength());
//                Log.e(Config.TAG, "发送 sequence:" + sequence+" 总长度："+rtp_send_packet.getLength()+"  包长度"+send_stream.length+"  "+Arrays.toString(send_stream));
            sequence++;
            try {
                //Log.e(Config.TAG, "发送视频数据："+send_stream.length);
//                LogUtil.d("dfy","rtp_socket = "+rtp_socket);
                rtp_socket.send(rtp_send_packet, serverAddress);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }







    public void stopServer() {
        isRunning = false;
        if (rtp_socket != null) {
            rtp_socket.close();
            rtp_socket = null;
        }


    }


}
