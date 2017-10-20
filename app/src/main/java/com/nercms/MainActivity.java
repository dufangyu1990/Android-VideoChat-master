package com.nercms;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.editText);


        editText.setText(Util.getIpAddressString());
    }

    public void doStart(View v) {
        String ip = editText.getText().toString();
        Intent intent = new Intent(this, VideoChatActivity2.class);
        intent.putExtra("remote_ip", ip);
        intent.putExtra("remote_video_port", 19888);
        intent.putExtra("remote_audio_port", 19887);
        startActivity(intent);

    }




}
