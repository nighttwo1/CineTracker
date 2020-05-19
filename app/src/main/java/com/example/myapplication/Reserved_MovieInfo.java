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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Reserved_MovieInfo extends AppCompatActivity {

    TextView textView_title, textView_theater, textView_date, textView6, textView10, textView12, textView_empty;
    Button button_home;
    String movie_date_tmp, movie_date;
    String date_tmp;
    String movie_date_refractor;
    static Date movie_time=null;
    String current_t=null;
    String current_t_refractor=null;
    String current_t_dayofmonth=null;

    //선택한 날짜 정보 받아오기 위한 string
    static String Month="";
    static String DayofMonth="";

    static String DayofMonth_temp="";

    static String accountname="";

    private ArrayList<ItemObject_reservationinfo> reserved_info=new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reserved_movieinfo);

        textView_title=findViewById(R.id.textView_title);
        textView_theater=findViewById(R.id.textView_theater);
        textView_date=findViewById(R.id.textView_date);
        button_home=findViewById(R.id.button_home);

        textView6=findViewById(R.id.textView6);
        textView10=findViewById(R.id.textView10);
        textView12=findViewById(R.id.textView12);
        textView_empty=findViewById(R.id.textView_empty);

        SimpleDateFormat dateFormat=new SimpleDateFormat("MM-dd HH:mm");
        current_t=dateFormat.format(new Date());

        String action=getIntent().getStringExtra("A");
        if(action.equals("check_reservationinfo")) {
            Intent intent4 = getIntent();
            textView_title.setText(intent4.getStringExtra("title"));
            textView_theater.setText(intent4.getStringExtra("theater"));
            textView_date.setText(intent4.getStringExtra("date"));

            movie_date_tmp=textView_date.getText().toString();
            movie_date=movie_date_tmp.substring(movie_date_tmp.indexOf("일")+2, movie_date_tmp.indexOf("~"));
            Log.d(movie_date, "movie_date split check");

            if(textView_theater.getText().toString().equals("1")){
                textView_title.setVisibility(View.GONE);
                textView_theater.setVisibility(View.GONE);
                textView_date.setVisibility(View.GONE);
                textView6.setVisibility(View.GONE);
                textView10.setVisibility(View.GONE);
                textView12.setVisibility(View.GONE);
                textView_empty.setVisibility(View.VISIBLE);
            }

            //Log.d(dateFormat.format(current_time), "현재 시간");
            //Log.d(movie_date, "영화시간");
            //Log.d(Long.toString((current_time.getTime()-movie_time.getTime())/(60*60*1000)), "차이");
        }
        else if(action.equals("reservation_made")){
            Intent intent3 = getIntent();

            Month=intent3.getStringExtra("Selected Month");
            DayofMonth=intent3.getStringExtra("Selected DayofMonth");
            accountname=intent3.getStringExtra("accountName");

            if(textView_theater.getText().toString()!=null) {

                textView_title.setText(intent3.getStringExtra("title"));

                String hall_temp = null;

                hall_temp = intent3.getStringExtra("hall").replaceAll(" ", "");
                hall_temp = hall_temp.substring((hall_temp.indexOf("관") - 1), (hall_temp.indexOf("총") - 2));

                textView_theater.setText(intent3.getStringExtra("theater") + "  " + hall_temp);
                textView_date.setText("2019년 "+Month+"월 "+DayofMonth+"일 "+intent3.getStringExtra("running time"));
            }
            else{
                Toast.makeText(getApplicationContext(), "이미 예매내역이 존재합니다.", Toast.LENGTH_LONG).show();
            }
        }

        if(movie_date!=null) {
            try {

                Date current_time=dateFormat.parse(current_t);

                current_t_dayofmonth=current_t.substring(3, 5);
                Log.d(current_t_dayofmonth, "현재 일자");

                current_t_refractor=current_t.split(":")[0];
                movie_time = dateFormat.parse(Month+"-"+DayofMonth+" "+movie_date);

                //24:05의 영화 정보가 있음. 이경우 현재 시간과의 차이에 문제점 발생
                String refrator_24="24";
                Log.d(movie_date.split(":")[0], "앞부분");

                if(current_t_dayofmonth.charAt(0)=='0'){
                    current_t_dayofmonth= String.valueOf(current_t_dayofmonth.charAt(1));
                }
                if(DayofMonth.charAt(0)=='0'){
                    DayofMonth_temp= String.valueOf(DayofMonth.charAt(1));
                }
                else{
                    DayofMonth_temp=DayofMonth;
                }

                Log.d(current_t_dayofmonth, "문자열->int_current");
                Log.d(DayofMonth_temp, "문자열->int_영화시");
                //날짜와 시간 비교해서 시간이 지난 영화 예매 정보 삭제
                if(Integer.parseInt(current_t_dayofmonth)> Integer.parseInt(DayofMonth_temp)){
                    textView_title.setVisibility(View.GONE);
                    textView_theater.setVisibility(View.GONE);
                    textView_date.setVisibility(View.GONE);
                    textView6.setVisibility(View.GONE);
                    textView10.setVisibility(View.GONE);
                    textView12.setVisibility(View.GONE);
                    textView_empty.setVisibility(View.VISIBLE);
                    textView_title.setText(null);
                    textView_theater.setText("1");
                    textView_date.setText("2");
                }
                else if(Integer.parseInt(current_t_dayofmonth)== Integer.parseInt(DayofMonth)){
                    if(movie_date.split(":")[0].equals(refrator_24)){
                        movie_date_refractor="00:"+movie_date.substring(movie_date.lastIndexOf(":")+1);
                        movie_time = dateFormat.parse(movie_date_refractor);
                        Log.d(movie_date, "refractor24");
                    }
                    else if(movie_date.split(":")[0].equals("25" )){
                        movie_date_refractor="01"+movie_date.substring(movie_date.lastIndexOf(":")+1);
                        movie_time = dateFormat.parse(movie_date_refractor);
                    }

                    long diff=current_time.getTime()-movie_time.getTime();
                    if (diff > 0) {
                        textView_title.setVisibility(View.GONE);
                        textView_theater.setVisibility(View.GONE);
                        textView_date.setVisibility(View.GONE);
                        textView6.setVisibility(View.GONE);
                        textView10.setVisibility(View.GONE);
                        textView12.setVisibility(View.GONE);
                        textView_empty.setVisibility(View.VISIBLE);

                        textView_title.setText(null);
                        textView_theater.setText("1");
                        textView_date.setText("2");
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        reserved_info.add(new ItemObject_reservationinfo(textView_title.getText().toString(), textView_theater.getText().toString(), textView_date.getText().toString()));

        for(int i=0;i<reserved_info.size();i++){
            if(reserved_info.get(i).getTitle()==null){
                reserved_info.remove(i);
            }
        }

        button_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent5=new Intent(Reserved_MovieInfo.this, ChoiceActivity.class);
                intent5.putExtra("A", "From Reserved MovieInfo");
                intent5.putExtra("reserved_info_title", textView_title.getText().toString());
                intent5.putExtra("reserved_info_theater", textView_theater.getText().toString());
                intent5.putExtra("reserved_info_date", textView_date.getText().toString());
                intent5.putExtra("accountName", accountname);
                startActivity(intent5);
            }
        });
    }
}
