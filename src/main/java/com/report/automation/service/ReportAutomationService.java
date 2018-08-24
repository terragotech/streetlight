package com.report.automation.service;

import com.report.automation.model.WeeklyReportModel;
import com.report.automation.service.AbstractReportService;
import com.terragoedge.slvinterface.utils.PropertiesReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ReportAutomationService extends AbstractReportService {

    public ReportAutomationService() {

    }

    public void run() {
        String enabledWeeklyReport = PropertiesReader.getProperties().getProperty("edge.reports.weekly");
        if (enabledWeeklyReport != null && Boolean.parseBoolean(enabledWeeklyReport)) {
            String weeklyReportsPath = PropertiesReader.getProperties().getProperty("edge.reports.reportpath");
            List<WeeklyReportModel> weeklyReportModelList = getCsvToBean(weeklyReportsPath);
          /*  List<WeeklyReportModel> finalList = removeDuplicateMacAddress(weeklyReportModelList);
            weeklyReportModelList.removeAll(finalList);
            System.out.println("macAddress" + finalList);*/
            List<WeeklyReportModel> finalList = new ArrayList<>();
            for (WeeklyReportModel weeklyReportModel : weeklyReportModelList) {
                WeeklyReportModel weeklyReportTemp = new WeeklyReportModel();
                weeklyReportTemp.setMacAddress(weeklyReportModel.getMacAddress());
                weeklyReportTemp.setFixture_number(weeklyReportModel.getFixture_number());
                int repeatSize = Collections.frequency(weeklyReportModelList, weeklyReportTemp);
                if (repeatSize > 1) {
                    weeklyReportModel.setRepeatedSize(repeatSize);
                    finalList.add(weeklyReportModel);
                    System.out.println(weeklyReportModel.getFixture_number() + " - " + repeatSize);
                }
            }
            writeCsvFile(finalList, getFilename());
        }

    }

    private void writeCsvFile(List<WeeklyReportModel> weeklyReportModelList, String fileName) {
        FileOutputStream fileOutputStream = null;
        fileName ="test";
        try {
            String weeklyReportsOutPath = PropertiesReader.getProperties().getProperty("edge.reports.reportoutpath");
            String path = weeklyReportsOutPath + fileName+".txt";
            // fileOutputStream = new FileOutputStream("D:/Report" + fileName);
           File file =new File(path);
           if(!file.exists()){
               file.createNewFile();
           }
            fileOutputStream = new FileOutputStream(path);
            for (WeeklyReportModel weeklyReportModel : weeklyReportModelList) {
                String newLine = System.getProperty("line.separator");
                String data =weeklyReportModel.getFixture_number()+" - "+weeklyReportModel.getMacAddress() +" - "+weeklyReportModel.getRepeatedSize()+"\n";
                fileOutputStream.write(data.getBytes());
                fileOutputStream.write(newLine.getBytes());
                fileOutputStream.flush();
            }
            System.out.println("Successfully generated CSV file");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("file writting problem" + e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getFilename() {
        long millisec = System.currentTimeMillis();
        Date date = new Date(millisec);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd_MMM_yyyy_HH:mm:ss");
        return dateFormat.format(date);
    }

}
