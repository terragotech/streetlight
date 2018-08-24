package com.report.automation.service;

import com.opencsv.CSVReader;
import com.report.automation.model.WeeklyReportModel;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AbstractReportService {
    public AbstractReportService() {

    }

    public List<WeeklyReportModel> getCsvToBean(String path) {
        List<WeeklyReportModel> weeklyReportModelList = new ArrayList<WeeklyReportModel>();
        try {
            CSVReader reader = new CSVReader(new FileReader(path), ',');
            List<String[]> records = reader.readAll();
            Iterator<String[]> iterator = records.iterator();

            while (iterator.hasNext()) {
                String[] record = iterator.next();
                WeeklyReportModel weeklyReportModel = new WeeklyReportModel();
                weeklyReportModel.setFixture_number(record[1]);
                weeklyReportModel.setMacAddress(record[17]);
                weeklyReportModel.setInstall_date(record[36]);
                weeklyReportModelList.add(weeklyReportModel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return weeklyReportModelList;
    }

    public static List<WeeklyReportModel> removeDuplicateMacAddress(List<WeeklyReportModel> weeklyReportModelList) {
        List<WeeklyReportModel> uniqueMacAddressList = new ArrayList<>();

        for (WeeklyReportModel weeklyReportModel : weeklyReportModelList) {
            WeeklyReportModel temp = new WeeklyReportModel();
            temp.setFixture_number("");
            temp.setMacAddress(weeklyReportModel.getMacAddress());
            if (!uniqueMacAddressList.contains(temp)) {
                uniqueMacAddressList.add(weeklyReportModel);
            }
        }
        return uniqueMacAddressList;
    }
}
