package com.slvinterface.utils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateOperationUtils {
    public static String getDateBeforeDay(int days)
    {
        DateTimeFormatter format = DateTimeFormat.forPattern("MM-dd-yyyy");
        DateTime now = new DateTime();
        DateTime daysAgo = now.minusDays(days);
        return format.print(daysAgo);
    }
    public static String getCurDate()
    {
        DateTimeFormatter format = DateTimeFormat.forPattern("MM-dd-yyyy");
        DateTime now = new DateTime();
        return format.print(now);
    }
}
