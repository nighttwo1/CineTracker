package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.EventLog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.myapplication.arrayList.eventStrings;

public class CGV_theater extends AppCompatActivity {

    final String url_runningtime="http://www.cgv.co.kr/common/showtimes/iframeTheater.aspx?areacode=02&theatercode=";
    final String url_theater="http://cgvadimg.cjsystems.co.kr/WebApp/TheaterV4/TheaterRegionList.aspx?regioncode=02";

    TextView textView_title, textView_empty;
    ListView listView;
    Spinner spinner;

    String title=null;
    String titles_temp=null;
    String[] hall_type=null;
    static String theater_code=null;

    private List<String> Data_schedule;

    private List<String> Data;
    //영화관 hall
    private List<String> theater_hall;
    //adapter2는 spinner를 위해
    private ArrayAdapter<String> adapter, adapter2;

    //영화관 code
    ArrayList<String> store_theater_url_index=new ArrayList<>();
    //영화관 이름
    ArrayList<String> store_theater_name=new ArrayList<>();


    //calendar에서 선택한 날짜의 일정의 시간을 받아오기 위한 arraylist
    ArrayList<String> Event_selected_date=new ArrayList<>();

    //선택한 날짜 정보 받아오기 위한 string
    static String Month="";
    static String DayofMonth="";

    static int p=0;

    //k값은 url에서 최대로 보여지는 상영날짜보다 더 이후의 날짜를 선택했을 때 1로 설정한다.
    //k=0일 경우 textView의 Text를 "선택하신 영화의 상영정보가 존재하지 않습니다."로 변경.
    //k==1일 경우 textView의 Text를 "선택하신 날짜의 상영정보를 제공하지 않습니다."로 변경.
    //나머지는 textView가 "영화관을 선택해주세요" 표시.
    static int k=3;


    static String accountname="";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cgv_theater);

        textView_title=findViewById(R.id.textView_title);
        textView_empty=findViewById(R.id.textView_empty);
        spinner=findViewById(R.id.spinner);

        theater_hall=new ArrayList<>();

        Data_schedule=new ArrayList<>();

        Data=new ArrayList<>();
        adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Data){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view=super.getView(position, convertView, parent);

                TextView tv = (TextView) view.findViewById(android.R.id.text1);
                tv.setTextColor(Color.WHITE);
                tv.setTypeface(null, Typeface.BOLD);
                tv.setBackgroundColor(getResources().getColor(R.color.gray_logo));
                return view;
            }
        };
        listView=(ListView)findViewById(R.id.listView);
        listView.setAdapter(adapter);

        adapter2=new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, store_theater_name){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view=super.getView(position, convertView, parent);

                TextView textView=(TextView)view.findViewById(android.R.id.text1);
                //textView.setTextColor(getResources().getColor(R.color.orange));
                textView.setTextColor(Color.WHITE);
                textView.setTextSize(18);
                textView.setTypeface(null, Typeface.BOLD);
                return view;
            }
        };
        spinner.setAdapter(adapter2);

        Intent intent2=getIntent();
        textView_title.setText(intent2.getStringExtra("title"));
        Month=intent2.getStringExtra("Selected Month");
        DayofMonth=intent2.getStringExtra("Selected DayofMonth");
        eventStrings=(ArrayList<String>)intent2.getSerializableExtra("task");
        accountname=intent2.getStringExtra("accountName");

        Event_selected_date.clear();
        for(int i=0;i<eventStrings.size();i++){
            String event_starttime=eventStrings.get(i).substring(eventStrings.get(i).indexOf("(")+1, eventStrings.get(i).indexOf(")"));
            String event_endtime=eventStrings.get(i).substring(eventStrings.get(i).indexOf("~")+3, eventStrings.get(i).indexOf("~")+19);

            Event_selected_date.add(event_starttime+"/"+event_endtime);
            Log.d(event_starttime, "event starttime");
            Log.d(event_endtime, "event endtime");

        }

        title=textView_title.getText().toString();

        //store_theater_name.add("영화관을 선택하세요");
        CGV_theater.JsoupAsyncTask jsoupAsyncTask = new CGV_theater.JsoupAsyncTask();
        jsoupAsyncTask.execute();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //if (position == 0)
                //{
                //    Toast.makeText(getApplicationContext(), "영화관을 선택해주세요", Toast.LENGTH_SHORT).show();
                //}
                //else {
                    theater_code = store_theater_url_index.get(position);
                    Log.d(theater_code, "theater code");

                //}
                CGV_theater.JsoupAsyncTask1 jsoupAsyncTask1 = new CGV_theater.JsoupAsyncTask1();
                jsoupAsyncTask1.execute();


                Log.d(String.valueOf(k), "k값");
                //선택한 날짜의 상영정보를 url에서 제공하지 않음.
                if(k==1) {
                    textView_empty.setText("선택하신 날짜의 상영정보를 제공하지 않습니다.");
                    listView.setEmptyView(textView_empty);
                }
                //선택한 날짜의 현재시간에 상영하는 영화가 없음.
                else if(k==0){
                    textView_empty.setText("선택하신 영화의 상영정보가 존재하지 않습니다.");
                    listView.setEmptyView(textView_empty);
                }
                else{

                    listView.setEmptyView(textView_empty);
                }
            }
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(getApplicationContext(), "영화관을 선택해주세요", Toast.LENGTH_LONG).show();
                adapter.notifyDataSetChanged();
            }

        });

        listView.setEmptyView(textView_empty);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                for(int i=0;i<position;i++) {
                    if (Data.get(position - i).contains("||")) {
                        p=position-i;
                        break;
                    }
                }

                if(Data.get(position).contains("준비중...")){
                    Toast.makeText(getApplicationContext(), "상영관이 아직 배정되지 않았습니다.\n다른 시간대를 선택해주세요.", Toast.LENGTH_LONG).show();
                }
                else if(!Data.get(position).contains("||")) {
                    //dialog
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CGV_theater.this);
                    alertDialogBuilder.setTitle("티켓 예매");
                    alertDialogBuilder.setMessage("\n\t\t\t예매하시겠습니까?")
                            .setCancelable(false)
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //Toast.makeText(getApplicationContext(), "예매되었습니다", Toast.LENGTH_SHORT).show();
                                    //intent
                                    Intent intent3 = new Intent(CGV_theater.this, Reserved_MovieInfo.class);
                                    intent3.putExtra("A", "reservation_made");
                                    intent3.putExtra("title", textView_title.getText().toString());
                                    intent3.putExtra("theater", spinner.getSelectedItem().toString());
                                    intent3.putExtra("running time", Data.get(position).substring(6));
                                    //영화관 상세정보(예를 들어 1관 3층)
                                    intent3.putExtra("hall", Data.get(p));
                                    //날짜 정보(월, 일)
                                    intent3.putExtra("Selected Month", Month);
                                    intent3.putExtra("Selected DayofMonth", DayofMonth);

                                    intent3.putExtra("accountName", accountname);
                                    startActivity(intent3);

                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
                else {
                }
            }
        });
    }

    private class JsoupAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            int i = 0;

            Document document_theater = null;

            try {
                document_theater = Jsoup.connect(url_theater).get();

                Elements ElementDataSize_theater = document_theater.select("ul li");

                Log.d(ElementDataSize_theater.select("a strong").text(), "영화관확인");
                for (Element e : ElementDataSize_theater) {
                    if(e.select("a").attr("href").contains("TheaterDetail.aspx?tc="))
                    {
                        store_theater_name.add(e.select("a strong").text());
                        Log.d(e.select("a strong").text(), "영화관");

                        String url_index=e.select("a").attr("href").substring(e.select("a").attr("href").lastIndexOf("=")+1);
                        store_theater_url_index.add(url_index);
                        Log.d(url_index, "영화관 url index");
                    }
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            adapter2.notifyDataSetChanged();
            // Adapter.notifyDataSetChanged();
        }
    }

    private class JsoupAsyncTask1 extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            Data.clear();
            theater_hall.clear();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            Document document = null;

            try {
                Log.d(Month, "month_str");
                Log.d(DayofMonth, "dayofMonth_str");

                document = Jsoup.connect(url_runningtime+theater_code+"&date=2019"+Month+DayofMonth).get();

                //cgv에서 상영시간표는 약 3일후까지만 지원된다.
                Elements availableElement=document.select("div.item-wrap ul li div a strong");
                String availableSearchDate=availableElement.text().substring(availableElement.text().lastIndexOf(" ")+1);
                Log.d(availableSearchDate, "지원 날짜");
                if(Integer.parseInt(DayofMonth)<=Integer.parseInt(availableSearchDate)) {

                    Elements mElementDataSize = document.select("div.sect-showtimes ul li div.col-times");
                    int mElementSize = mElementDataSize.size();

                    ///Elements img_url=document.select("ul[class=lst_detail_t1").select("li").select("li div[class=thumb] a img");
                    /// String str_img=img_url.attr("src");
                    // Elements titles=document.select("ul[class=lst_detail_t1]").select("li").select("li dt[class=tit] a");
                    /// Elements content=document.select("ul[class=lst_detail_t1]").select("li").select("dt[class=tit_t2]").next().first().select("a");

                    //Log.d(mElementDataSize.select("a strong").text(), "title 확인");
                    //Log.d(mElementDataSize.select("a strong").get(1).text(), "title 확인");

                    for (Element e : mElementDataSize) {
                        Elements Element_title = e.select("a strong");

                        Log.d(Element_title.text(), "e.text 프린트");


                        //CGV의 영화정보에서는 영화제목에 "-" 또는 "- "로 표기
                        //네이버 영화정보에서는 영화제목에 ": "로 표기
                        //예를 들어 CGV에서는 "엑스맨-다크 피닉스"로 표기되어있는 반면
                        //네이버 영화정보에서는 "엑스맨: 다크 피닉스"로 표기되어있음.
                        //그래서 equal 함수를 쓰기위해 변환해줌.
                        if (Element_title.text().contains("- ")) {
                            titles_temp = Element_title.text().replace("- ", ": ");
                        } else if (Element_title.text().contains("-")) {
                            titles_temp = Element_title.text().replace("-", ": ");
                        }


                        if ((title.equals(Element_title.text())) || title.equals(titles_temp)) {
                            int position = 0;
                            Log.d("Done", "if문확인");
                            for (Element e1 : e.select("div.info-timetable")) {
                                for (Element e3 : e.select("div.info-hall ul")) {
                                    String hall_replace = e3.select("li").text();
                                    hall_replace = hall_replace.replaceFirst(" ", " || ");
                                    hall_replace = hall_replace.substring(0, hall_replace.indexOf("총") - 1) + " || " + hall_replace.substring(hall_replace.indexOf("총"));
                                    theater_hall.add(hall_replace);
                                }

                                Data.add(theater_hall.get(position));
                                //Data.add(hall_type[j]);
                                Log.d("For", "For문확인");
                                for (Element e2 : e1.select("ul li")) {
                                    String closed = e2.select("span").text();
                                    String running_time = e2.select("a em").text();

                                    //Element playendtime=e2.select("a[data-playendtime");
                                    String playendtime = e2.select("a[data-playendtime]").toString();

                                    if(playendtime.contains("playendtime")) {
                                        String length = playendtime.length() + "";
                                        String tmp1 = null, tmp2 = null;

                                        try {
                                            int index = playendtime.indexOf("data-playendtime");
                                            tmp1 = playendtime.substring(index + 18, index + 20);
                                            tmp2 = playendtime.substring(index + 20, index + 22);
                                            Log.d(tmp1, "tmp");
                                            Log.d(tmp2, "tmp2");
                                        } catch (StringIndexOutOfBoundsException e12) {
                                            e12.printStackTrace();
                                        }
                                        Log.d(length, "length");
                                        //Data.add(playendtime);
                                        //Log.d(playendtime, "끝나는시간");
                                        Log.d(closed, "마감");

                                        if (!closed.equals("마감")) {
                                            //<saved>//Data.add(running_time);
                                            Data.add("      " + running_time + "~" + tmp1 + ":" + tmp2);
                                            Log.d(running_time, "runningtime 확인");
                                        }
                                    }
                                    else{
                                        Data.add("      " + running_time + "~" + "준비중...");
                                    }

                                }
                                position++;
                            }
                        }
                    }
                    k=0;
                }
                else{
                    k=1;
                }
            }catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(Data.size()>0) {
                for (int i = 0; i < Event_selected_date.size(); i++) {
                    String Event_selected_date_starttime=Event_selected_date.get(i).split("/")[0].split(" ")[1];
                    String Event_selected_date_endtime=Event_selected_date.get(i).split("/")[1].split(" ")[1];

                    Log.d(Event_selected_date_starttime, "Event_selected_date_starttime");
                    Log.d(Event_selected_date_endtime, "Event_selected_date_endtime");

                    SimpleDateFormat dateFormat=new SimpleDateFormat("HH:mm");
                    java.util.Locale.getDefault();
                    try {
                        Date Event_selected_date_starttime_Date=dateFormat.parse(Event_selected_date_starttime);
                        Date Event_selected_date_endtime_Date=dateFormat.parse(Event_selected_date_endtime);

                        for (int j = 0; j < Data.size(); j++) {
                            if(Data.get(j).contains("준비중...")){

                            }
                            else if (!Data.get(j).contains("||")) {
                                Log.d(Data.get(j), "데이터 정보");
                                String Data_starttime = Data.get(j).substring(Data.get(j).indexOf("~") - 5, Data.get(j).indexOf("~"));
                                String Data_endtime = Data.get(j).substring(Data.get(j).indexOf("~") + 1);

                                Log.d(Data_starttime, "Data_starttime");
                                Log.d(Data_endtime, "Data_endtime");

                                try {
                                    Date Data_starttime_Date=dateFormat.parse(Data_starttime);
                                    Date Data_endtime_Date=dateFormat.parse(Data_endtime);

                                    //영화시간과 calendar일정의 시간 비교!!
                                    //case1
                                    if((Event_selected_date_starttime_Date.getTime()<Data_starttime_Date.getTime()) && (Event_selected_date_endtime_Date.getTime()>=Data_starttime_Date.getTime()) && (Event_selected_date_endtime_Date.getTime())<=Data_endtime_Date.getTime()){
                                        Data_schedule.add(Data.get(j));
                                    }
                                    //case2
                                    else if((Event_selected_date_starttime_Date.getTime()>=Data_starttime_Date.getTime()) && (Event_selected_date_endtime_Date.getTime()<=Data_endtime_Date.getTime())){
                                        Data_schedule.add(Data.get(j));
                                    }
                                    //case3
                                    else if((Event_selected_date_starttime_Date.getTime()<=Data_endtime_Date.getTime()) && (Event_selected_date_starttime_Date.getTime()>=Data_starttime_Date.getTime()) && (Event_selected_date_endtime_Date.getTime()>Data_endtime_Date.getTime())){
                                        Data_schedule.add(Data.get(j));
                                    }
                                    //case4
                                    else if((Event_selected_date_starttime_Date.getTime()<Data_starttime_Date.getTime()) && (Event_selected_date_endtime_Date.getTime()>Data_endtime_Date.getTime())){
                                        Data_schedule.add(Data.get(j));
                                    }

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                        Log.d("------------------", "--------------");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    for(int k=0;k<Data_schedule.size();k++){
                        for(int l=0;l<Data.size();l++){
                            if(Data_schedule.get(k).equals(Data.get(l))){
                                Data.remove(l);
                                break;
                            }
                        }
                    }


                }
            }

            adapter.notifyDataSetChanged();
            super.onPostExecute(aVoid);
        }
    }



}