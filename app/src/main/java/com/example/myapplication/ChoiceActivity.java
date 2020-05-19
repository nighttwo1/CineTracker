package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ChoiceActivity extends AppCompatActivity {

    String title=null;
    String theater=null;
    String date=null;
    Button button_reserve, button_reservationinfo;
    TextView textView_AccountName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);
        button_reserve=findViewById(R.id.button_reserve);
        button_reservationinfo=findViewById(R.id.button_reservationinfo);

        //예약하기 버튼 임시로 연결함.
        button_reserve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent6=new Intent(ChoiceActivity.this,getTask.class);
                intent6.putExtra("accountName", textView_AccountName.getText().toString());
                startActivity(intent6);
            }
        });

        textView_AccountName=findViewById(R.id.textView_AccountName);
        String action=getIntent().getStringExtra("A");
        if(action.equals("From Reserved MovieInfo")) {
            Intent intent5 = getIntent();
            title = intent5.getStringExtra("reserved_info_title");
            theater = intent5.getStringExtra("reserved_info_theater");
            date = intent5.getStringExtra("reserved_info_date");
            textView_AccountName.setText(intent5.getStringExtra("accountName"));
        }
        else if(action.equals("From LoginActivity")) {

            Intent intent_toCalendar = getIntent();
            textView_AccountName.setText(intent_toCalendar.getStringExtra("accountName").replaceAll(" ", "").split(":")[1]);

        }

        button_reservationinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(title!=null && theater!=null &&date!=null) {
                    Intent intent4 = new Intent(ChoiceActivity.this, Reserved_MovieInfo.class);
                    intent4.putExtra("A", "check_reservationinfo");
                    intent4.putExtra("title", title);
                    intent4.putExtra("theater", theater);
                    intent4.putExtra("date", date);
                    startActivity(intent4);
                }
                else{
                    Toast.makeText(getApplicationContext(),"예매정보가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
