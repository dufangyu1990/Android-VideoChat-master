package com.nercms.receive;

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



    private Button transBtn;
    private TextView textView;
    private  DatagramSocket datagramSocket;
    private StringBuffer stringBuffer = new StringBuffer();

    private String UserIp;//设备IP
    private String UserPort;//设备端口

    private int localPort = 16228;
    private boolean threadflag= false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        init();
        startHearTask();
        doReceive();

    }


    private void initView() {
        textView = (TextView)findViewById(R.id.showdata);
        transBtn = (Button)findViewById(R.id.mybtn);
        transBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String send = "[2002^0003^201^WG12345678901235^^^^^^^^^^^^^^^^]";
                DatagramPacket p2;
                // 发包打洞,多发几次
                Log.d("dfy","UserIp = "+UserIp);
                Log.d("dfy","UserPort = "+UserPort);
                try {
                    p2 = new DatagramPacket(send.getBytes(),send.getBytes().length, InetAddress.getByName(UserIp),Integer.parseInt(UserPort));
                    datagramSocket.send(p2);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        });


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
        DecoderThread thread = new DecoderThread();
        thread.start();
    }





    private void startHearTask()
    {
        Log.d("dfy","启动心跳线程");
        // 启动心跳线程
        //[2002^0001^201^WG12345678901235^^^^^^^^^用户本地IP^用户本地端口^^^^^^]
        stringBuffer.delete(0,stringBuffer.length());
        stringBuffer.append("[2001^0001^201^WG12345678901235^^^^^^^^^").append(Util.getIpAddressString()).append("^").append(String.valueOf(localPort)).append("^^^^^^]");
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
                        Log.d("dfy", "接收数据=" + content);
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
                datagramSocket.close();
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


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        threadflag = true;
        close();
    }














}
