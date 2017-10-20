package com.nercms.send.video;

/**
 * Created by zsg on 2016/10/20.
 */
public class VideoServer {
    public interface ReceiveVideoCallback {
        public void receiveVideoStream(byte[] frmbuf, int frmSize, long timestamp);

    }

}
