package com.example.myapplication;

public class ItemObject_reservationinfo {

    private String title;
    private String theater;
    private String date;

    public ItemObject_reservationinfo(String title, String theater, String date) {
        this.title=title;
        this.theater=theater;
        this.date =date;
    }


    public String getTitle() {
        return title;
    }


    public String getTheater() {
        return theater;
    }


    public String getDate(){ return date; }
}
