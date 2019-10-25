package com.slvinterface.utils;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class FileOperationUtils {
    public static boolean doesFolderExists(String path){
        boolean status = false;
        File f = new File(path);
        if(f.exists())
        {
            status = true;
        }
        else
        {
            status = false;
        }
        return status;
    }
    public static String getCurrentDate(){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day  = calendar.get(Calendar.DATE);
        String strMonth = "";
        String strDay = "";
        month = month + 1;
        if(month < 10)
        {
            strMonth = "0" + Integer.toString(month);
        }
        else
        {
            strMonth = Integer.toString(month);
        }
        if(day < 10)
        {
            strDay = "0" + Integer.toString(day);
        }
        else
        {
            strDay = Integer.toString(day);
        }
        return Integer.toString(year) + "_" + strMonth + "_" + strDay;
    }
    public static String getTime()
    {
        String result = "";
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        String strHour = "";
        String strMin = "";
        String strSec = "";
        if(hour < 10)
        {
            strHour = "0" + Integer.toString(hour);
        }
        else
        {
            strHour = Integer.toString(hour);
        }
        if(minute < 10)
        {
            strMin = "0" + Integer.toString(minute);
        }
        else
        {
            strMin = Integer.toString(minute);
        }
        if(second < 10)
        {
            strSec = "0" + Integer.toString(second);
        }
        else
        {
            strSec = Integer.toString(second);
        }
        result = strHour + "_"+  strMin + "_" + strSec;
        return result;
    }
    public static void createFolder(String path)
    {
        File f = new File(path);
        f.mkdirs();
    }
    public static boolean createFile(String fileName)
    {
        boolean status = false;
        File f = new File(fileName);
        try {
            f.createNewFile();
            status = true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return status;
    }
}
