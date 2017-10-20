package com.nercms.receive;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nercms.send.Send;

import org.sipdroid.net.RtpPacket;
import org.sipdroid.net.RtpSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {



    private Button transBtn,videoBtn;
    private TextView textView;
    private StringBuffer stringBuffer = new StringBuffer();

    private String UserIp;//设备IP
    private String UserPort;//设备端口

    private int localPort = 16228;
    private boolean threadflag= false;



    private Camera mCamera = null; //创建摄像头处理类
    private int cameraPosition = 1;//1代表前置摄像头，0代表后置摄像头


    private SurfaceHolder holder = null; //创建界面句柄，显示视频的窗口句柄
    private SurfaceView mSurfaceView;
    private com.nercms.send.FfmpegServer ffmpegServer;

    //---------------------------------------------------------------------------------------------------

    private Send encode;      //编码器
    private RtpSocket rtp_socket = null; //创建RTP套接字

    private DatagramSocket datagramSocket;

    private InetSocketAddress serverAddress;
    private DatagramPacket datagramPacket;
    private RtpPacket rtp_send_packet = null; //创建RTP发送包

    //发送
    private long encoder_handle = -1; //创建编码器的句柄
    private int send_packetNum = 0; //包的数目
    private int[] send_packetSize = new int[200]; //包的尺寸
    private byte[] send_stream = new byte[65536]; //码流
    private byte[] socket_send_Buffer = new byte[65536]; //缓存 stream->socketBuffer->rtp_socket

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
//        init();
//        startHearTask();
//        doReceive();

    }


    private void initView() {
        textView = (TextView)findViewById(R.id.showdata);
        transBtn = (Button)findViewById(R.id.mybtn);
        videoBtn = (Button)findViewById(R.id.transVideo) ;
        mSurfaceView = (SurfaceView)findViewById(R.id.video_surfaceview);


        holder = mSurfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        transBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String send = "[2002^0003^201^WG12345678901235^^^^^^^^^^^^^^^^]";
                DatagramPacket p2;
                // 发包打洞,多发几次
                LogUtil.d("dfy","UserIp = "+UserIp);
                LogUtil.d("dfy","UserPort = "+UserPort);
                try {
                    if(TextUtils.isEmpty(UserIp)||TextUtils.isEmpty(UserPort))
                    {
                        Toast.makeText(getApplicationContext(),"未获取ip和端口，无法穿透",Toast.LENGTH_LONG).show();
                        return;
                    }
                    p2 = new DatagramPacket(send.getBytes(),send.getBytes().length, InetAddress.getByName(UserIp),Integer.parseInt(UserPort));
                    datagramSocket.send(p2);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });

        videoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                if(TextUtils.isEmpty(UserIp)|| TextUtils.isEmpty(UserPort))
//                {
//                    Toast.makeText(getApplicationContext(),"未获取客户端Ip和端口，无法传输视频",Toast.LENGTH_LONG);
//                    return;
//                }
//                ffmpegServer=new FfmpegServer(UserIp,Integer.parseInt(UserPort));
//                doStart();




                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        doStart("192.168.100.3","19888");
                    }
                }.start();





//

            }
        });

        encode = new Send();



    }




    /**
     * 开启 发送rtp线程  开启本地摄像头
     */
    public void doStart(String UserIp,String UserPort) {

//        serverAddress = new InetSocketAddress(UserIp, Integer.parseInt(UserPort));
//        //初始化解码器
//        if (rtp_socket == null) {
//            try {
//                //rtp_socket = new RtpSocket(new SipdroidSocket(20000)); //初始化套接字，20000为接收端口号
//                rtp_socket = new RtpSocket(new SipdroidSocket(20000));
//            } catch (SocketException e) {
//                e.printStackTrace();
//            } catch (UnknownHostException e) {
//                e.printStackTrace();
//            }
//            //初始化发送包
//            rtp_send_packet = new RtpPacket(socket_send_Buffer, 0);
//        }


        if(datagramSocket == null)
        {
            try {
                datagramSocket = new DatagramSocket(20000);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }


        String text = "测试数据";

        byte[] buf = text.getBytes();
        // 构造数据报包，用来将长度为 length 的包发送到指定主机上的指定端口号。
        try {
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(UserIp), Integer.parseInt(UserPort));
            datagramSocket.send(packet);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        //初始化编码器
//        if (encoder_handle == -1)
//            encoder_handle = encode.CreateEncoder(352, 288); //调用底层函数，创建编码器
//        openCamera();

    }

    public void openCamera() {
        if (mCamera == null) {

//            //摄像头设置，预览视频
//            mCamera = Camera.open(cameraPosition); //实例化摄像头类对象
//            Camera.Parameters p = mCamera.getParameters(); //将摄像头参数传入p中
//            p.setFlashMode("off");
//            p.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
//            p.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
//            p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
//
//            p.setPictureFormat(PixelFormat.JPEG); // Sets the image format for
//            // picture 设定相片格式为JPEG，默认为NV21
//            p.setPreviewFormat(PixelFormat.YCbCr_420_SP); // Sets the image format
//            //p.setPreviewFormat(ImageFormat.NV21);
//            //p.setPreviewFormat(PixelFormat.YCbCr_420_SP); //设置预览视频的格式
//
//
//            p.setPreviewSize(352, 288); //设置预览视频的尺寸，CIF格式352×288
//            //p.setPreviewSize(800, 600);
//            p.setPreviewFrameRate(15); //设置预览的帧率，15帧/秒
//            mCamera.setParameters(p); //设置参数



            //摄像头设置，预览视频
            mCamera = Camera.open(cameraPosition); //实例化摄像头类对象
            Camera.Parameters p = mCamera.getParameters(); //将摄像头参数传入p中
            p.setPreviewFormat(PixelFormat.YCbCr_420_SP); //设置预览视频的格式
            p.setPreviewSize(352,288); //设置预览视频的尺寸，CIF格式352×288
            p.setPreviewFrameRate(15); //设置预览的帧率，15帧/秒
            mCamera.setParameters(p); //设置参数
            mCamera.setDisplayOrientation(90); //视频旋转90度




  /*          int PreviewWidth = 0;
            int PreviewHeight = 0;
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);//获取窗口的管理器
            Display display = wm.getDefaultDisplay();//获得窗口里面的屏幕
            Camera.Parameters parameters  = mCamera.getParameters();
            // 选择合适的预览尺寸
            List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();

            // 如果sizeList只有一个我们也没有必要做什么了，因为就他一个别无选择
            if (sizeList.size() > 1) {
                Iterator<Camera.Size> itor = sizeList.iterator();
                while (itor.hasNext()) {
                    Camera.Size cur = itor.next();
                    if (cur.width >= PreviewWidth
                            && cur.height >= PreviewHeight) {
                        PreviewWidth = cur.width;
                        PreviewHeight = cur.height;
                        break;
                    }
                }
            }
            parameters.setPreviewSize(PreviewWidth, PreviewHeight); //获得摄像区域的大小
            parameters.setPreviewFrameRate(3);//每秒3帧  每秒从摄像头里面获得3个画面
            parameters.setPictureFormat(PixelFormat.JPEG);//设置照片输出的格式
            parameters.set("jpeg-quality", 85);//设置照片质量
            parameters.setPictureSize(PreviewWidth, PreviewHeight);//设置拍出来的屏幕大小
            //
            mCamera.setParameters(parameters);//把上面的设置 赋给摄像头
            */


//            byte[] rawBuf = new byte[1400];
//            mCamera.addCallbackBuffer(rawBuf);
            mCamera.setDisplayOrientation(90); //视频旋转90度
            try {
                mCamera.setPreviewDisplay(holder); //预览的视频显示到指定窗口
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.startPreview(); //开始预览


            Log.d("dfy","openCamera");
            //获取帧
            //预览的回调函数在开始预览的时候以中断方式被调用，每秒调用15次，回调函数在预览的同时调出正在播放的帧
            Callback a = new Callback();
            mCamera.setPreviewCallback(a);


        }
    }

    //mCamera回调的类
    class Callback implements Camera.PreviewCallback {
        @Override
        public void onPreviewFrame(byte[] frame, Camera camera) {
            //audioServer.encode();
            // Log.d("xxxxx","size1:"+Arrays.toString(frame));
            //YUVtoRGBUtil.decodeYUV420SP(selfVideo.mPixel,frame,352,288);

//            Log.d("dfy","enter onPreviewFrame");
//            YuvImage image = new YuvImage(ffmpegServer.sendData(frame), ImageFormat.NV21, 352, 288, null);            //ImageFormat.NV21  640 480
//            ByteArrayOutputStream outputSteam = new ByteArrayOutputStream();
//            image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 70, outputSteam); // 将NV21格式图片，以质量70压缩成Jpeg，并得到JPEG数据流








            if (encoder_handle != -1) {
                //底层函数，返回包的数目，返回包的大小存储在数组packetSize中，返回码流在stream中
                send_packetNum = encode.EncoderOneFrame(encoder_handle, -1, frame, send_stream, send_packetSize);
//                Log.d("log", "原始数据大小：" + frame.length + "  转码后数据大小：" + send_stream.length);
                if (send_packetNum > 0) {

                    //通过RTP协议发送帧
                    final int[] pos = {0}; //从码流头部开始取
                    final long timestamp = System.currentTimeMillis(); //设定时间戳
                    /**
                     * 因为可能传输数据过大 会将一次数据分割成好几段来传输
                     * 接受方 根据序列号和结束符 来将这些数据拼接成完整数据
                     */
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int sequence = 0; //初始化序列号
                            for (int i = 0; i < send_packetNum; i++) {

                                rtp_send_packet.setPayloadType(2);//定义负载类型，视频为2
                                rtp_send_packet.setMarker(i == send_packetNum - 1 ? true : false); //是否是最后一个RTP包
                                rtp_send_packet.setSequenceNumber(sequence++); //序列号依次加1
                                rtp_send_packet.setTimestamp(timestamp); //时间戳
                                //Log.d("log", "序列号:" + sequence + " 时间：" + timestamp);
                                rtp_send_packet.setPayloadLength(send_packetSize[i]); //包的长度，packetSize[i]+头文件
                                //从码流stream的pos处开始复制，从socketBuffer的第12个字节开始粘贴，packetSize为粘贴的长度
                                System.arraycopy(send_stream, pos[0], socket_send_Buffer, 12, send_packetSize[i]); //把一个包存在socketBuffer中
                                pos[0] += send_packetSize[i]; //重定义下次开始复制的位置
                                //rtp_packet.setPayload(socketBuffer, rtp_packet.getLength());
                                //  Log.d("log", "序列号:" + sequence + " bMark:" + rtp_packet.hasMarker() + " packetSize:" + packetSize[i] + " tPayloadType:2" + " timestamp:" + timestamp);
                                try {
                                    rtp_socket.send(rtp_send_packet);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    }).start();


                }
            }












        }
    }

    public void init() {
        try {
            datagramSocket = new DatagramSocket(localPort,InetAddress.getByName(Util.getIpAddressString()));
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
        stringBuffer.append("[2001^0001^201^WG12345678901235^^^^^^^^^").append(Util.getIpAddressString()).append("^").append(String.valueOf(localPort)).append("^^^^^^]");
//        stringBuffer.append("[2001^0001^201^WG12345678901234^^^^^^^^^").append(Util.getIpAddressString()).append("^").append(String.valueOf(localPort)).append("^^^^^^]");
        String heartStr = stringBuffer.toString();
        try {
            DatagramPacket hp = new DatagramPacket(heartStr.getBytes(), heartStr.length(),InetAddress.getByName(Constant.SERVER_IP), Constant.SERVER_PORT);
            HeartThread thread = new HeartThread(hp);
            thread.start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }


    class HeartThread extends Thread {
        private DatagramPacket p;
        public HeartThread(DatagramPacket p) {
            this.p = p;
        }

        public void run() {
            while (!threadflag) {
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




















    /**
     * 接收服务器数据 线程
     */
    class ReceiveThread extends Thread {
        public void run() {
            try {
                byte[] buf = new byte[1024];
                DatagramPacket rp = new DatagramPacket(buf, 1024);
                while (!threadflag) {
                    datagramSocket.receive(rp);
                    // 取出信息
                    final  String content = new String(rp.getData(), 0, rp.getLength());
//                    String rip = rp.getAddress().getHostAddress();
//                    int rport = rp.getPort();
                    // 输出接收到的数据
//                    Log.d("dfy", "接收数据=" + rip + ":" + rport + " >>>> " + content);
//                    Log.d("dfy", "接收数据=" +  content);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(content);
                        }
                    });
                    String[] strArr = content.split("\\^");
                    if(strArr[1].equals(Constant.HEART_JUMP))
                    {

                    }else if(strArr[1].equals(Constant.QUERY_ORDER))
                    {
                        LogUtil.d("dfy", "接收数据=" + content);
                        UserIp = strArr[12];
                        UserPort = strArr[13];
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                transBtn.setEnabled(true);
                            }
                        });

                    }
                }
//                datagramSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
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
        threadflag = true;
        close();
    }














}
