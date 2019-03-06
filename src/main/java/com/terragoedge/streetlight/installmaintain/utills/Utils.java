package com.terragoedge.streetlight.installmaintain.utills;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.text.SimpleDateFormat;
import java.util.Date;
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
}
