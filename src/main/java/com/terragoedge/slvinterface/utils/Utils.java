package com.terragoedge.slvinterface.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    public static String dateFormat(Long dateTime) {
        Date date = new Date(Long.valueOf(dateTime));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dff = dateFormat.format(date);
        return dff;
    }
}
