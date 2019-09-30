package com.slvinterface.utils;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InBoundFileUtils {
    public static void  createFolderIfNotExisits(String folderName)
    {
        File f = new File(folderName);
        if(!f.isDirectory())
        {
            f.mkdirs();
        }
    }
    public static String generateTodayFolderName()
    {
        String result = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
        Date date = new Date();
        result = dateFormat.format(date);
        return result;
    }
    public static void createFile(String fileName)
    {
        try {
            File f = new File(fileName);
            f.createNewFile();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public static boolean doesFileExists(String fileName)
    {
        File f = new File(fileName);
        return f.exists();
    }
    public static void deleteFile(String fileName)
    {
        File f = new File(fileName);
        f.delete();
    }
}
