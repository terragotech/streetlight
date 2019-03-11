package com.terragoedge.slvinterface;

import com.terragoedge.slvinterface.service.*;
import com.terragoedge.slvinterface.utils.PropertiesReader;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

public class SlvInterfaceApp {

    public static void main(String[] args) {
        try {
            SlvInterfaceService slvInterfaceService = new SlvInterfaceService();
            while (true) {
                slvInterfaceService.start();
                processedDailyReport(slvInterfaceService);
                Thread.sleep(60000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void processedDailyReport(SlvInterfaceService slvInterfaceService) {
        try {
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            int hoursOfDay = calendar.get(Calendar.HOUR_OF_DAY);
            System.out.println("hoursOfDay:" + hoursOfDay);
            String time = PropertiesReader.getProperties().getProperty("jps.report.time");
            int scheduledHours = Integer.parseInt(time);
            if (hoursOfDay >= scheduledHours && hoursOfDay < (scheduledHours + 1)) {
                File file = new File("./pid");
                if (!file.exists()) {
                    System.out.println("File is not present.");
                    slvInterfaceService.startReport();
                } else {
                    System.out.println("File is present.");
                }

            } else {
                File file = new File("./pid");
                if (file.exists()) {
                    System.out.println("File deleted.");
                    file.delete();
                }
            }
            Thread.sleep(600000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}