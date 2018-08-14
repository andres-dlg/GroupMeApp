package com.andresdlg.groupmeapp.Utils;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter {

    public static String formatDate(long timeInMillis){
        Date date = new Date(timeInMillis);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormater = new SimpleDateFormat("dd-MM-yyyy HH:mm")  ;
        //@SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormater = new SimpleDateFormat("dd/MM/yyyy")  ;
        return simpleDateFormater.format(date);
    }

}
