package com.terragoedge.streetlight;

import com.terragoedge.streetlight.service.FailureReportService;
import com.terragoedge.streetlight.service.ReportFixService;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class StreetlightApp {

    public static int getHoursOfDay() {
        String scheduledHour = PropertiesReader.getProperties().getProperty("amerescousa.failure.scheduledhour");
        return Integer.parseInt(scheduledHour);
    }

    public static int getMinutesOfHours() {
        String scheduledHour = PropertiesReader.getProperties().getProperty("amerescousa.failure.scheduledminute");
        return Integer.parseInt(scheduledHour);
    }

    public static void main(String[] args) {
     /*   ReportFixService reportFixService = new ReportFixService();
        reportFixService.run();*/
        try {
            while (true) {
                try {
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    int hoursOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                    int minutesOfHour = calendar.get(Calendar.MINUTE);
                    System.out.println("hoursOfDay:" + hoursOfDay);
                    // 18 and 19
                    if (hoursOfDay >= getHoursOfDay() && hoursOfDay < (getHoursOfDay() + 1) && minutesOfHour >= getMinutesOfHours()) {
                        //if(hoursOfDay >= 10 && hoursOfDay < 13){
                        File file = new File("./data/pid");
                        if (!file.exists()) {
                            file.createNewFile();
                            System.out.println("File is not present.");
                            try {
                                FailureReportService failureReportService = new FailureReportService();
                                failureReportService.run();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("File is present.");
                        }

                    } else {
                        File file = new File("./data/pid");
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
