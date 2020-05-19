package com.example.myapplication;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Handler handler=new Handler(){
            public void handleMessage(Message message){
                super.handleMessage(message);
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        };
        handler.sendEmptyMessageDelayed(0, 3000);

    }
}