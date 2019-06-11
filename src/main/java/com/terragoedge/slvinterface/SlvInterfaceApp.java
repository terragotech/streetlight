package com.terragoedge.slvinterface;

import com.terragoedge.slvinterface.service.*;
import com.terragoedge.slvinterface.utils.PropertiesReader;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

public class SlvInterfaceApp {

    private static boolean isReportSent = false;

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
            if (hoursOfDay == scheduledHours) {
                if(!isReportSent){
                    slvInterfaceService.startReport();
                    isReportSent = true;
                }
            } else {
                isReportSent = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}