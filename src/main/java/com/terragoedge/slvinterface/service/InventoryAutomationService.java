package com.terragoedge.slvinterface.service;

import com.terragoedge.automation.Dao.InventoryDAO;
import com.terragoedge.automation.enumeration.ReportType;
import com.terragoedge.automation.model.MacValidationModel;
import com.terragoedge.automation.service.AbstractReportService;
import com.terragoedge.slvinterface.model.EdgeNote;

import java.util.ArrayList;
import java.util.List;

public class InventoryAutomationService extends AbstractReportService {
    private InventoryDAO inventoryDAO;
    public InventoryAutomationService(){
        inventoryDAO = InventoryDAO.INSTANCE;
    }

    public void start(){
        String formTemplateGuid = "0b513f6e-452e-4397-84db-18d9deac61d7";
        String path = "D:/data/inventory.csv";
        List<String> macAddressList=new ArrayList<>();
        macAddressList.add("00135005008020EB");
       // List<String> macAddressList = getCsvToEntity(path);
        for(String macAddress : macAddressList){
            List<String> noteGuids = inventoryDAO.getNoteGuid(formTemplateGuid,macAddress);
            System.out.println(noteGuids);
        }
    }
}
