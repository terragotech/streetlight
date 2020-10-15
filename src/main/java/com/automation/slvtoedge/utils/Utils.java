package com.automation.slvtoedge.utils;


import org.joda.time.DateTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Utils {
    public static String getDateTime(long dateTime){
        dateTime = new Date().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
        return simpleDateFormat.format(new Date(dateTime));
    }
    public static String getDateTime(){
        long dateTime = new Date().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd");
        return simpleDateFormat.format(new Date(dateTime));
    }


    public static void createZipFile(String sourceFilePath,String outputPath){
        ZipOutputStream zipOut = null;
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outputPath);
            zipOut = new ZipOutputStream(fos);
            File fileToZip = new File(sourceFilePath);
            fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(zipOut != null){
                try {
                    zipOut.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            if(fis != null){
                try{
                    fis.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            if(fos != null){
                try{
                    fos.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
