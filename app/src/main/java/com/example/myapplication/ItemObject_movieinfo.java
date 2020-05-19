package com.example.myapplication;

public class ItemObject_movieinfo {

    private String img_url;
    private String genre;
    private String title;
    private String content;
    private String actor;
    private byte[] byteArray;

    public ItemObject_movieinfo(String url, String title, String genre, String content, String actor , byte[] bytearray) {
        this.img_url=url;
        this.title=title;
        this.genre =genre;
        this.content=content;
        this.actor=actor;
        this.byteArray=bytearray;
    }

    public String getImg_url() {
        return img_url;
    }


    public String getTitle() {
        return title;
    }


    public String getGenre(){ return genre; }


    public String getContent() {
        return content;
    }


    public String getActor() {
        return actor;
    }


    public byte[] getByteArray() {return byteArray;}
}