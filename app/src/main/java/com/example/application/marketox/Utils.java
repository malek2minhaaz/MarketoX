package com.example.application.marketox;

import android.content.Context;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

public class Utils {

    public static  void toast(Context context,String message){
        Toast.makeText(context,message, Toast.LENGTH_SHORT).show();


    }

    public static  long getTimestamp(){
        return System.currentTimeMillis();


    }


    public static String formatTimestampDateTime(long timestamp){
        Calendar calendar=Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(timestamp);
        String date= android.text.format.DateFormat.format("dd/MM/yyyy",calendar).toString();
        return date;


    }
}
