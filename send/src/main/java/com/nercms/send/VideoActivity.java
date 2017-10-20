package com.nercms.send;

import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.nercms.send.receive.Videoplay;
import com.nercms.send.send.FfmpegServer;
import com.nercms.send.video.VideoServer;

import java.io.ByteArrayOutputStream;

/**
 * Created by dufangyu on 2017/10/16.
 */

public class VideoActivity extends AppCompatActivity implements VideoServer.ReceiveVideoCallback{

    private static  FfmpegServer ffmpegServer;
    private String clientip;
    private int clientPort;
    private Videoplay clientView = null;      //对面大视图视频

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat2);
        initView();

    }



    public static void actionStart(Context context,String ip,int port)
    {
        Intent intent = new Intent(context,VideoActivity.class);
        intent.putExtra("ip",ip);
        intent.putExtra("port",port);
        context.startActivity(intent);
    }


    private void initView()
    {
        clientView = (Videoplay)findViewById(R.id.video_play) ;
        clientip = getIntent().getStringExtra("ip");
        clientPort = getIntent().getIntExtra("port",0);
        ffmpegServer=new FfmpegServer(this, clientip,clientPort);
        ffmpegServer.doStart();
    }


    public static void startDecodeVideo()
    {
        ffmpegServer.doStart();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }





    @Override
    public void receiveVideoStream(byte[] frmbuf, int frmSize, long timestamp) {

        byte decode[]=ffmpegServer.ffmpeg.videodecode(frmbuf);
        YuvImage image = new YuvImage(decode, ImageFormat.NV21, 352, 288, null);            //ImageFormat.NV21  640 480
        ByteArrayOutputStream outputSteam = new ByteArrayOutputStream();
        image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 70, outputSteam); // 将NV21格式图片，以质量70压缩成Jpeg，并得到JPEG数据流
        clientView.jpegData = outputSteam.toByteArray();
        clientView.postInvalidate();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ffmpegServer.stopServer();
    }
}
