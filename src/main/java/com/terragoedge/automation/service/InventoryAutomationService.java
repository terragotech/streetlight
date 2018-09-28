package com.terragoedge.automation.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.automation.Constant;
import com.terragoedge.automation.Dao.InstallDAO;
import com.terragoedge.automation.Dao.InventoryDAO;
import com.terragoedge.automation.enumeration.FormTemplateType;
import com.terragoedge.automation.enumeration.ReportType;
import com.terragoedge.automation.model.CustodyResultModel;
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
import com.terragoedge.slvinterface.utils.PropertiesReader;
import com.terragoedge.slvinterface.utils.Utils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.*;

public class InventoryAutomationService extends AbstractInventoryService {
    private InventoryDAO inventoryDAO;
    private InstallDAO installDAO;
    private Map<String, String> usersMap = new HashMap<>();

    public InventoryAutomationService() {
        inventoryDAO = InventoryDAO.INSTANCE;
        installDAO = new InstallDAO();
    }


    public void start() {
        usersMap = getAllUsers();
        String inventoryHandlingFormGuid = "0b513f6e-452e-4397-84db-18d9deac61d7";
        String nodeFormGuid = "4010b3b0-14ef-4762-bdf7-e03306c852d3";
        String path = "D:/Report/inventorycustodyexceptionreport.csv";
        // List<String> macAddressList = new ArrayList<>();
        //  macAddressList.add("00135005007F1C2D");

        List<CustodyResultModel> custodyResultModelList = getCsvToEntity(path);
        System.out.println("Total Records : " + custodyResultModelList.size());
        List<InventoryResult> inventoryResultList = new ArrayList<>();
        for (CustodyResultModel custodyResultModel : custodyResultModelList) {
            InventoryResult inventoryResult = null;
            if (custodyResultModel.getWorkflow().equals("Load for Assignment")) {
                inventoryResult = processNodeForm(inventoryHandlingFormGuid, nodeFormGuid, custodyResultModel.getMacaddress());
            } else if (custodyResultModel.getWorkflow().equals("Return to Stock")) {
                inventoryResult = processLoadForAssignment(custodyResultModel);
            }
            System.out.println("Processed mac: " + custodyResultModel.getMacaddress());
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

    public String getInventoryHandlingPalletNumber(EdgeFormEntity edgeFormEntity) {
        String formDef = edgeFormEntity.getFormDef();
        List<EdgeFormData> edgeFormDataList = getFormDef(formDef);
        String[] scanLicPlateValues = getRepeatableFormValue(edgeFormDataList, 18, -1, -1);
        String scanPlateValues = StringUtils.join(scanLicPlateValues, "|");
        return scanPlateValues;
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
                edgeFormInfo.setPalletNumber(palletNumber);
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
        inventoryResult.setPalletNumber(edgeFormInfo.getPalletNumber());
        if (edgeFormEntity != null) {
            String formDef = edgeFormEntity.getFormDef();
            List<EdgeFormData> edgeFormDataList = getFormDef(formDef);
            String inventoryPalletList = getInventoryHandlingPalletNumber(edgeFormEntity);
            inventoryResult.setInventoryHandlingPalletList(inventoryPalletList);
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

    public InventoryResult processLoadForAssignment(CustodyResultModel custodyResultModel) {
        InventoryResult inventoryResult = new InventoryResult();
        inventoryResult.setTitle(custodyResultModel.getMacaddress());
        String loadForAssignmentGuid = PropertiesReader.getProperties().getProperty("comed.edgeform.load_of_assignment_guid");
        String userKeywords = PropertiesReader.getProperties().getProperty("comed.install.userkeyword");
        List<String> keywordsList = new ArrayList<String>(Arrays.asList(userKeywords.split("\\|")));

        String noteGuid = installDAO.getInventoryHandlingEntity(loadForAssignmentGuid, custodyResultModel.getMacaddress());
        if (noteGuid != null) {
            EdgeNoteView edgeNoteView = installDAO.getEdgeNoteView(noteGuid);
            EdgeFormEntity edgeFormEntity = installDAO.getFormDef(edgeNoteView.getNoteId());
            if (edgeFormEntity != null) {
                List<EdgeFormData> edgeFormDataList = getFormDef(edgeFormEntity.getFormDef());
                int loadingFromReportingId = Integer.parseInt(PropertiesReader.getProperties().getProperty("comed.edgeform.loading_from_reporting.id"));
                int toInstaller = Integer.parseInt(PropertiesReader.getProperties().getProperty("comed.edgeform.to_installer.id"));
                int macAddress = Integer.parseInt(PropertiesReader.getProperties().getProperty("comed.edgeform.macaddress.id"));
                String loadingFromReporting = Utils.getFormValue(edgeFormDataList, loadingFromReportingId);
                inventoryResult.setNoteGuid(edgeNoteView.getNoteGuid());
                String toInstallerName = null;
                for (EdgeFormData edgeFormData : edgeFormDataList) {
                    if (edgeFormData.getId() == toInstaller) {
                        toInstallerName = getFormValue(edgeFormData.getValue());
                        if (keywordsList.contains(toInstallerName)) {
                            toInstallerName = usersMap.get(edgeNoteView.getCreatedBy());
                        }
                    }
                }
                updateInventoryResult(loadingFromReporting, toInstallerName, inventoryResult);
            }
        }
        return inventoryResult;
    }

    public void updateInventoryResult(String toInstallername, String loadingFrom, InventoryResult inventoryResult) {
        inventoryResult.setReportFrom(loadingFrom);
        inventoryResult.setToInstaller(toInstallername);
    }
}
