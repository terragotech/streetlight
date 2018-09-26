package com.terragoedge.automation.service;

import com.terragoedge.automation.Dao.EdgeReportDAO;
import com.terragoedge.automation.Dao.InventoryDAO;
import com.terragoedge.automation.enumeration.ReportType;
import com.terragoedge.automation.model.MacValidationEntity;
import com.terragoedge.automation.model.MacValidationModel;
import com.terragoedge.slvinterface.utils.PropertiesReader;

import java.io.File;
import java.util.List;

public class FinalReportService extends AbstractReportService {
    private EdgeReportDAO edgeReportDAO;

    public FinalReportService() {
        edgeReportDAO = EdgeReportDAO.INSTANCE;
    }

    public void startProcess() {
        String source = PropertiesReader.getProperties().getProperty("edge.edgereport.sourcepath");
        File sourceFolder = new File(source);
        File[] files = sourceFolder.listFiles();
        for (File file : files) {
            String validationFilePath = file.getPath();
            System.out.println("process File :"+validationFilePath);
            List<MacValidationModel> macValidationModelList = getCsvToBean(validationFilePath, ReportType.MAC_VALIDATION);
            System.out.println("process size :"+macValidationModelList.size());
            for (MacValidationModel macValidationModel : macValidationModelList) {
                if(!macValidationModel.getFixtureid().equals("fixtureid") && !macValidationModel.getMacaddress().equals("macaddress")) {
                    MacValidationEntity macValidationEntity = new MacValidationEntity(macValidationModel);
                    if (macValidationEntity != null) {
                        MacValidationEntity macValidationEntity1 = edgeReportDAO.getMacValidationEntity(macValidationEntity.getFixture_id(), macValidationEntity.getModified_date());
                        if (macValidationEntity1 == null) {
                            edgeReportDAO.createMacValidationEntity(macValidationEntity);
                        } else {
                            System.out.println("Already Exist" + macValidationEntity.getFixture_id());
                        }
                    }
                }
            }

        }
    }
}
