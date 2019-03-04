package com.terragoedge.streetlight.installmaintain.utills;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    public static String getDate(long milliSec){
        SimpleDateFormat simpleDateFormat  = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(new Date((milliSec)));
    }

    public static String getDateTime(long milliSec){
        SimpleDateFormat simpleDateFormat  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(new Date((milliSec)));
    }
}
