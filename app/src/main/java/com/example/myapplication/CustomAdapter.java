package com.example.myapplication;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter {
    Activity activity;
    int layout;
    private ArrayList<ItemObject_movieinfo> arr;


    public CustomAdapter(Activity activity, int layout, ArrayList<ItemObject_movieinfo> arr){
        super(activity, layout, arr);

        this.activity=activity;
        this.layout=layout;
        this.arr=arr;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater=activity.getLayoutInflater();
        convertView=layoutInflater.inflate(layout, null);

        ImageView imageView=(ImageView)convertView.findViewById(R.id.image_Poster);
        TextView textView1=(TextView)convertView.findViewById(R.id.textView_title);
        TextView textView2=(TextView)convertView.findViewById(R.id.textView_genre);
        TextView textView3=(TextView)convertView.findViewById(R.id.textView_description);
        TextView textView4=(TextView)convertView.findViewById(R.id.textView_actor);

        //byte->bitmap해서 imageview에 저장
        if(arr.get(position).getByteArray()==null){
            imageView.setImageDrawable(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.ic_android_black_24dp, null));
        }
        else{
            byte[] byteArr=arr.get(position).getByteArray();
            Bitmap img=byteArrayToBitmap(byteArr);
            imageView.setImageBitmap(img);
        }

        GlideApp.with(convertView).load(arr.get(position).getImg_url()).override(450).into(imageView);
        textView1.setText(arr.get(position).getTitle());
        textView2.setText(arr.get(position).getGenre());
        textView3.setText(arr.get(position).getContent());
        textView4.setText(arr.get(position).getActor());


        return convertView;
    }

    public Bitmap byteArrayToBitmap(byte[] byteArray) {
        Bitmap bitmap = null;
        bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        byteArray = null;
        return bitmap;
    }
}
