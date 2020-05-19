package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import static com.example.myapplication.arrayList.eventStrings;

public class Detailed_movieinfo extends AppCompatActivity {

    static ArrayList<String> arrayList=new ArrayList<>();

    Button button_book;

    private int img;

    //선택한 날짜 정보 받아오기 위한 string
    static String Month="";
    static String DayofMonth="";

    static String accountname="";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_info);


        ImageView imageView_poster= (ImageView) findViewById(R.id.image_Poster);
        final TextView textView_title=(TextView)findViewById(R.id.textView_title);
        TextView textView_genre=(TextView)findViewById(R.id.textView_genre);
        TextView textView_content=(TextView)findViewById(R.id.textView_description);
        TextView textView_actor=(TextView)findViewById(R.id.textView_actor);
        TextView textView_summary=(TextView)findViewById(R.id.textView_summary);

        Intent intent1=getIntent();
        if(intent1.getByteArrayExtra("poster")==null){
            imageView_poster.setImageDrawable(ResourcesCompat.getDrawable(this.getResources(), R.drawable.ic_launcher_background, null));
        }
        else {
            byte[] byteArray = intent1.getByteArrayExtra("poster");
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            //Bitmap bitmap1=Bitmap.createScaledBitmap(bitmap, 280, 280, true);
            imageView_poster.setImageBitmap(bitmap);
        }
        textView_title.setText(intent1.getStringExtra("title"));
        textView_genre.setText(intent1.getStringExtra("genre"));
        textView_content.setText(intent1.getStringExtra("content"));
        textView_actor.setText(intent1.getStringExtra("actor"));
        textView_summary.setText(intent1.getStringExtra("summary"));
        Month=intent1.getStringExtra("Selected Month MovieList->Detailed_movieinfo");
        DayofMonth=intent1.getStringExtra("Selected DayofMonth MovieList->Detailed_movieinfo");
        eventStrings=(ArrayList<String>)intent1.getSerializableExtra("task");
        accountname=intent1.getStringExtra("accountName");

        Log.d(Month, "Detailed Month");

        button_book=findViewById(R.id.book);

        button_book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent2=new Intent(Detailed_movieinfo.this, CGV_theater.class);
                intent2.putExtra("title", textView_title.getText().toString());

                intent2.putExtra("Selected Month", Month);
                intent2.putExtra("Selected DayofMonth", DayofMonth);
                intent2.putExtra("task", eventStrings);
                intent2.putExtra("accountName", accountname);

                startActivity(intent2);
            }
        });
    }
}
