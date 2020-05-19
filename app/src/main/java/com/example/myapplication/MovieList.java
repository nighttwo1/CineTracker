package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.example.myapplication.arrayList.eventStrings;

public class MovieList extends AppCompatActivity {
    final String url="https://movie.naver.com/movie/running/current.nhn";
    private String htmlContentInStringFormat="";
    private List<String> Data;
    private ArrayAdapter<String> Adapter;
    Button button;
    TextView textView_empty;

    //선택한 날짜 정보 받아오기 위한 string
    static String Month="";
    static String DayofMonth="";

    //영화 상세정보의 주소의 일부
    ArrayList<String> store_summary_url_index=new ArrayList<>();
    //영화 상세정보의 줄거리 내용 저장
    ArrayList<String> store_summary=new ArrayList<>();


    static ArrayList<String> arrayList=new ArrayList<>();

    CustomAdapter adapter;
    private ArrayList<ItemObject_movieinfo> list=new ArrayList<>();

    static String accountname=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movielist);

        //Calendar_selectdate으로부터 선택한 월, 일의 정보 받아옴.
        Intent intent7=getIntent();
        Month=intent7.getStringExtra("Selected Month");
        DayofMonth=intent7.getStringExtra("Selected DayofMonth");
        eventStrings=(ArrayList<String>)intent7.getSerializableExtra("task");
        accountname=intent7.getStringExtra("accountName");

        textView_empty=findViewById(R.id.textView_empty);

        ListView listView=(ListView)findViewById(com.example.myapplication.R.id.listView);
        adapter=new CustomAdapter(this, com.example.myapplication.R.layout.item_layout, list);
        listView.setAdapter(adapter);

        listView.setEmptyView(textView_empty);

        //Data_Ready();
        // setAdapter();
        //   onListView();

        MovieList.JsoupAsyncTask jsoupAsyncTask = new MovieList.JsoupAsyncTask();
        jsoupAsyncTask.execute();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(Month, "MovieList Month");

                Intent intent1=new Intent(MovieList.this, Detailed_movieinfo.class);
                intent1.putExtra("poster", list.get(position).getByteArray());
                intent1.putExtra("title", list.get(position).getTitle());
                intent1.putExtra("genre", list.get(position).getGenre());
                intent1.putExtra("content", list.get(position).getContent());
                intent1.putExtra("actor", list.get(position).getActor());
                intent1.putExtra("summary", store_summary.get(position));
                intent1.putExtra("Selected Month MovieList->Detailed_movieinfo", Month);
                intent1.putExtra("Selected DayofMonth MovieList->Detailed_movieinfo", DayofMonth);
                intent1.putExtra("task", eventStrings);
                intent1.putExtra("accountName", accountname);
                startActivity(intent1);

            }
        });
    }
    private class JsoupAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            int i=0;

            // GetMovieImage movieImage=new GetMovieImage();
            Document document= null;
            Document document_summary=null;

            //arraylist->list
            //String[] strData=store_summary_url_index.toArray(new String[store_summary_url_index.size()]);

            try {
                document = Jsoup.connect(url).get();

                Elements mElementDataSize = document.select("ul[class=lst_detail_t1]").select("li");
                int mElementSize = mElementDataSize.size();

                ///Elements img_url=document.select("ul[class=lst_detail_t1").select("li").select("li div[class=thumb] a img");
                /// String str_img=img_url.attr("src");
                // Elements titles=document.select("ul[class=lst_detail_t1]").select("li").select("li dt[class=tit] a");
                /// Elements content=document.select("ul[class=lst_detail_t1]").select("li").select("dt[class=tit_t2]").next().first().select("a");

                for(Element e:mElementDataSize){

                    //url->bitmap->byte로 전달하기 위해
                    String img_url=e.select("li div[class=thumb] a img").attr("src");
                    String titles=e.select("li dt[class=tit] a").text();
                    String genre=e.select("dt[class=tit_t1]").next().last().text();
                    String content=e.select("dt[class=tit_t2]").next().first().select("a").text();
                    String actor=e.select("dt[class=tit_t3]").next().text();

                    ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
                    getBitmap(img_url).compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    byte[] byteArray=byteArrayOutputStream.toByteArray();
                    list.add(new ItemObject_movieinfo(img_url, titles, genre, content, actor, byteArray));

                    ///////////////////////////////////////////
                    String summary_url=e.select("li div[class=thumb] a").attr("href");
                    String summary_url_index=summary_url.substring(summary_url.lastIndexOf("=")+1);
                    store_summary_url_index.add(summary_url_index);
                    Log.d(summary_url_index, "store");

                    //      Data.add(e.text().trim());
                }

                for(int j=0;j<store_summary_url_index.size();j++){
                    document_summary= Jsoup.connect("https://movie.naver.com/movie/bi/mi/basic.nhn?code="+store_summary_url_index.get(j)).get();
                    Log.d(String.valueOf(store_summary_url_index.get(0)), "summary");

                    Elements mElementDataSize2=document_summary.select("p[class=con_tx]");

                    //for(Element e:mElementDataSize2){
                    String summary=mElementDataSize2.text();
                    store_summary.add(summary);
                    Log.d(summary, "summary");
                }
                //textView.setText(document.title());
                Log.d("debug:", "List"+mElementDataSize);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            adapter.notifyDataSetChanged();
            // Adapter.notifyDataSetChanged();
        }
    }
    private void Data_Ready(){
        Data=new ArrayList<>();
    }
    private void setAdapter(){
        Adapter=new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Data);
    }
    private void onListView(){
        ListView listView=(ListView)findViewById(com.example.myapplication.R.id.listView);
        listView.setAdapter(Adapter);
    }
    private Bitmap getBitmap(String url){
        URL imgUrl=null;
        HttpURLConnection connection=null;
        InputStream inputStream=null;
        Bitmap retBitmap=null;

        try{
            imgUrl=new URL(url);
            connection=(HttpURLConnection)imgUrl.openConnection();
            connection.setDoInput(true);
            connection.connect();
            inputStream=connection.getInputStream();
            retBitmap= BitmapFactory.decodeStream(inputStream);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }finally {
            if(connection!=null){
                connection.disconnect();
            }
            return retBitmap;
        }
    }
}
