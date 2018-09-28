package com.terragoedge.slvinterface.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.automation.Constant;
import com.terragoedge.automation.Dao.InventoryDAO;
import com.terragoedge.automation.enumeration.ReportType;
import com.terragoedge.automation.model.EdgeFormInfo;
import com.terragoedge.automation.model.InventoryResult;
import com.terragoedge.automation.model.MacValidationModel;
import com.terragoedge.automation.service.AbstractInventoryService;
import com.terragoedge.automation.service.AbstractReportService;
import com.terragoedge.slvinterface.entity.EdgeFormEntity;
import com.terragoedge.slvinterface.entity.EdgeNoteView;
import com.terragoedge.slvinterface.exception.NoValueException;
import com.terragoedge.slvinterface.model.EdgeFormData;
import com.terragoedge.slvinterface.model.EdgeNote;
import com.terragoedge.slvinterface.utils.Utils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class InventoryAutomationService extends AbstractInventoryService implements Callable<InventoryResult> {
    private InventoryDAO inventoryDAO;
    String inventoryHandlingFormGuid;
    String nodeFormGuid;
    String macAddress;

    public InventoryAutomationService() {

    }

    public InventoryAutomationService(String inventoryHandlingFormGuid, String nodeFormGuid, String macAddress) {
        inventoryDAO = InventoryDAO.INSTANCE;
        this.inventoryHandlingFormGuid=inventoryHandlingFormGuid;
        this.nodeFormGuid=nodeFormGuid;
        this.macAddress=macAddress;
    }

    @Override
    public InventoryResult call() {
        System.out.println("start processing "+ macAddress);
        EdgeFormInfo edgeFormInfo = null;
        String inventoryHandlingNoteGuid = inventoryDAO.getInventoryHandlingEntity(inventoryHandlingFormGuid, macAddress);
        if (inventoryHandlingNoteGuid == null) {
            String noteFormGuid = inventoryDAO.getInventoryHandlingEntity(nodeFormGuid, macAddress);
            if (noteFormGuid != null) {
                EdgeNoteView edgeNoteView = inventoryDAO.getEdgeNoteView(noteFormGuid);
                String palletNumber = getPalletNumber(noteFormGuid, edgeNoteView);
                edgeFormInfo = processInventoryHandlingform(inventoryHandlingFormGuid, palletNumber);
            }
        } else {
            edgeFormInfo = processInventoryHandlingform(nodeFormGuid, macAddress);
        }
        edgeFormInfo.setTitle(macAddress);
        return processInventoryNote(edgeFormInfo);
    }

    public void start() {
        String inventoryHandlingFormGuid = "0b513f6e-452e-4397-84db-18d9deac61d7";
        String nodeFormGuid = "4010b3b0-14ef-4762-bdf7-e03306c852d3";
        String path = "D:/Report/inventorydata.csv";
        List<String> macAddressList = new ArrayList<>();
        macAddressList.add("00135005007F1C2D");

        // List<String> macAddressList = getCsvToEntity(path);
        System.out.println("Total Records : " + macAddressList.size());
        int i = 0;
        List<InventoryResult> inventoryResultList = new ArrayList<>();
        for (String macAddress : macAddressList) {
            i++;
            System.out.println("Processed mac: " + macAddress);
            System.out.println("Total processed : " + i);
            InventoryResult inventoryResult = processNodeForm(inventoryHandlingFormGuid, nodeFormGuid, macAddress);
            inventoryResultList.add(inventoryResult);
        }
        String outputFile = "D:/Report/inventorydata_verifiedData.csv";
        Utils.writeInventoryData(inventoryResultList, outputFile);
    }


    public EdgeFormInfo processInventoryHandlingform(String formTemplateGuid, String macAddress) {
        EdgeFormInfo edgeFormInfo = new EdgeFormInfo();
        String noteGuid = inventoryDAO.getInventoryHandlingEntity(formTemplateGuid, macAddress);
        if (noteGuid != null) {
            EdgeNoteView edgeNoteView = inventoryDAO.getEdgeNoteView(noteGuid);
            EdgeFormEntity edgeFormEntity = inventoryDAO.getFormDef(formTemplateGuid, noteGuid);
            edgeFormInfo.setEdgeFormEntity(edgeFormEntity);
            edgeFormInfo.setEdgeNoteView(edgeNoteView);
            edgeFormInfo.setNoteGuid(edgeNoteView.getNoteGuid());
        }
        return edgeFormInfo;
    }

    public InventoryResult processNodeForm(String inventoryHandlingFormGuid, String nodeFormGuid, String macAddress) {
        EdgeFormInfo edgeFormInfo = null;
        String inventoryHandlingNoteGuid = inventoryDAO.getInventoryHandlingEntity(inventoryHandlingFormGuid, macAddress);
        if (inventoryHandlingNoteGuid == null) {
            String noteFormGuid = inventoryDAO.getInventoryHandlingEntity(nodeFormGuid, macAddress);
            if (noteFormGuid != null) {
                EdgeNoteView edgeNoteView = inventoryDAO.getEdgeNoteView(noteFormGuid);
                String palletNumber = getPalletNumber(noteFormGuid, edgeNoteView);
                edgeFormInfo = processInventoryHandlingform(inventoryHandlingFormGuid, palletNumber);
            }
        } else {
            edgeFormInfo = processInventoryHandlingform(nodeFormGuid, macAddress);
        }
        edgeFormInfo.setTitle(macAddress);
        return processInventoryNote(edgeFormInfo);
    }

    public InventoryResult processInventoryNote(EdgeFormInfo edgeFormInfo) {
        EdgeFormEntity edgeFormEntity = edgeFormInfo.getEdgeFormEntity();
        InventoryResult inventoryResult = new InventoryResult();
        inventoryResult.setTitle(edgeFormInfo.getTitle());
        inventoryResult.setNoteGuid(edgeFormInfo.getNoteGuid());
        if (edgeFormEntity != null) {
            String formDef = edgeFormEntity.getFormDef();
            List<EdgeFormData> edgeFormDataList = getFormDef(formDef);
            try {
                inventoryResult.setScanning(Utils.valueById(edgeFormDataList, Constant.scanning));
                inventoryResult.setPalletWorkflow(Utils.valueById(edgeFormDataList, Constant.palletWorkflow));
                inventoryResult.setAction(Utils.valueById(edgeFormDataList, Constant.action));
                inventoryResult.setAssignedToUser(Utils.valueById(edgeFormDataList, Constant.assignedToUser));
                inventoryResult.setDeliveryLocation(Utils.valueById(edgeFormDataList, Constant.deliveryLocation));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return inventoryResult;
    }
}
