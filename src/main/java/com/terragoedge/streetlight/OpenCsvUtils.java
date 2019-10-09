package com.terragoedge.streetlight;

import com.opencsv.CSVWriter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.FileWriter;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class OpenCsvUtils {
    public static void csvWriterAll(List<String[]> stringArray, String path) throws Exception {
        CSVWriter writer = new CSVWriter(new FileWriter(path));
        writer.writeAll(stringArray);
        writer.close();
    }
    public static String getFormatedDateTime(long millisecs){
        Date date = new Date(millisecs);
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("CST"));
        String dateFormatted = formatter.format(date);
        return dateFormatted;
    }
    public static String getCsvFileName(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
        return simpleDateFormat.format(new Date());
    }
}
