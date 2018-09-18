package com.terragoedge.automation.service;

import com.opencsv.CSVReader;
import com.terragoedge.automation.enumeration.ReportType;
import com.terragoedge.automation.model.MacValidationModel;
import com.terragoedge.automation.model.ReplaceModel;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AbstractReportService {
    public AbstractReportService() {

    }

    public List getCsvToBean(String path, ReportType reportType) {
        try {
            CSVReader reader = new CSVReader(new FileReader(path), ',');
            List<String[]> records = reader.readAll();
            List<Object> reportList = getMacValidationModel(records, reportType);
            return reportList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList();
    }

    public List getMacValidationModel(List<String[]> records, ReportType reportType) {
        Iterator<String[]> iterator = records.iterator();
        switch (reportType) {
            case MAC_VALIDATION:
                System.out.println("MACValidationCalled");
                List<MacValidationModel> macValidationModels = new ArrayList<>();
                while (iterator.hasNext()) {
                    String[] record = iterator.next();
                    MacValidationModel macValidationModel = new MacValidationModel();
                    macValidationModel.setMacaddress(record[0]);
                    macValidationModel.setFixtureid(record[1]);
                    macValidationModel.setMunicipality(record[2]);
                    macValidationModel.setModifieddate(record[3]);
                    macValidationModel.setUser(record[4]);
                    macValidationModel.setAssigneduser(record[5]);
                    macValidationModels.add(macValidationModel);
                }
                return macValidationModels;
            case REPLACE:
                System.out.println("ReplaceCalled");
                List<ReplaceModel> replaceModelList = new ArrayList<>();
                while (iterator.hasNext()) {
                    String[] record = iterator.next();
                    ReplaceModel replaceModel = new ReplaceModel();
                    replaceModel.setFixtureid(record[0]);
                    replaceModel.setMunicipality(record[1]);
                    replaceModel.setWorkflow(record[2]);
                    replaceModel.setInstalledmacaddress(record[3]);
                    replaceModel.setExpectedmacaddress(record[4]);
                    replaceModel.setReplacedmacaddress(record[5]);
                    replaceModel.setModifieddate(record[6]);
                    replaceModel.setUser(record[7]);
                    replaceModelList.add(replaceModel);
                }
                return replaceModelList;
            default:
                break;
        }
        return new ArrayList();
    }
}
