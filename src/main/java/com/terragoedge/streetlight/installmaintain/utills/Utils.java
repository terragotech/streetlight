package com.terragoedge.streetlight.installmaintain.utills;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Utils {
    public static String getDate(long milliSec){
        SimpleDateFormat simpleDateFormat  = new SimpleDateFormat("yyyy-MM-dd");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("CST"));
        return simpleDateFormat.format(new Date((milliSec)));
    }

    public static String getDateTime(long milliSec){
        SimpleDateFormat simpleDateFormat  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("CST"));
        return simpleDateFormat.format(new Date((milliSec)));
    }

    public static String getDailyReportDateTime(long currentDateTime) {
        Date date = new Date(currentDateTime);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("CST"));
        return dateFormat.format(date);
    }


    public static String getDateTime() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        Date date = new Date(calendar.getTimeInMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMMyyyy");
        return dateFormat.format(date);
    }

    public static String getGeoPdfDateTime() {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        Date date = new Date(calendar.getTimeInMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        return dateFormat.format(date);
    }
}
