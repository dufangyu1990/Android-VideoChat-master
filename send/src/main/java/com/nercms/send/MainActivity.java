package com.nercms.send;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {



    private Button transBtn,queryBtn;
    private TextView textView;
    private  DatagramSocket datagramSocket;
    private static final int TIMEOUT = 3000;   // 设置超时为3秒
    private static final int MAXTRIES = 5;     // 最大重发次数5次
    private StringBuffer stringBuffer = new StringBuffer();

    private String deviceIp;//设备IP
    private String devicePort;//设备端口
    private boolean threadflag= false;

    private int loaclPort = 18228;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        init();
        startHearTask();
        doReceive();

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
        DecoderThread thread = new DecoderThread();
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
        queryBtn = (Button)findViewById(R.id.querybutton);

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

    }


    /**
     * 接收rtp数据并解码 线程
     */
    class DecoderThread extends Thread {
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
                    Log.d("dfy", "接收数据=" + content);

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
                        }else if(strArr[11].equals("0"))//离线
                        {

                        }
                    }
                }
                datagramSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

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




    public void close() {

        if(datagramSocket!=null)
        {
            if (!datagramSocket.isClosed())
                datagramSocket.close();
            datagramSocket.disconnect();
            datagramSocket = null;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        threadflag  = true;
        close();
    }














}
