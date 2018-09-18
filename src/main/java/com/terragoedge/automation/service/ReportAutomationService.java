package com.terragoedge.automation.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.automation.Dao.InventoryDAO;
import com.terragoedge.automation.enumeration.ReportType;
import com.terragoedge.automation.enumeration.ServerType;
import com.terragoedge.automation.enumeration.Status;
import com.terragoedge.automation.model.MacValidationModel;
import com.terragoedge.automation.model.ReplaceModel;
import com.terragoedge.slvinterface.dao.ConnectionDAO;
import com.terragoedge.slvinterface.entity.EdgeFormEntity;
import com.terragoedge.slvinterface.entity.EdgeNoteView;
import com.terragoedge.slvinterface.entity.EdgeNotebookEntity;
import com.terragoedge.slvinterface.exception.NoValueException;
import com.terragoedge.slvinterface.model.EdgeFormData;
import com.terragoedge.slvinterface.model.EdgeNote;
import com.terragoedge.slvinterface.model.FormData;
import com.terragoedge.slvinterface.utils.PropertiesReader;
import com.terragoedge.slvinterface.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ReportAutomationService extends AbstractReportService {
    ConnectionDAO connectionDAO = null;
    InventoryDAO inventoryDAO = null;
    Gson gson = null;

    public ReportAutomationService() {
        connectionDAO = ConnectionDAO.INSTANCE;
        inventoryDAO = InventoryDAO.INSTANCE;
        gson = new Gson();
    }

    public void start() {
        System.out.println("started");
        boolean isEnabledMacValidation = Boolean.parseBoolean(PropertiesReader.getProperties().getProperty("edge.reports.weekly"));
        boolean isEnableReplacement = Boolean.parseBoolean(PropertiesReader.getProperties().getProperty("edge.reports.replacement"));
        if (isEnabledMacValidation) {
            System.out.println("startMacValidationProcess");
            // startMacValidationProcess();
        }
        if (isEnableReplacement) {
            System.out.println("startReplacementProcess");
            //  startReplacementProcess();
        }
    }

    public String startMacValidationProcess(File sourceFile) {
        String[] paths;
        //   test();
        String loadForAssignmentGuid = PropertiesReader.getProperties().getProperty("edge.reports.loadforassignentguid");
        String enabledWeeklyReport = PropertiesReader.getProperties().getProperty("edge.reports.weekly");
        if (enabledWeeklyReport != null && Boolean.parseBoolean(enabledWeeklyReport)) {
            // File sourceFile = new File("D:\\home\\mailscheduler\\csvfile\\MAC_Validation_Report_Install_Inventory\\09_07_2018");
            File[] files = sourceFile.listFiles();
            for (File file : files) {
                String csvFileName = file.getName();
                String fileNameWithoutExtn = Utils.getFileNameWithoutExtension(file);
                String weeklyReportsPath = file.getPath();
                if (true) {
                    // String weeklyReportsPath = PropertiesReader.getProperties().getProperty("edge.reports.reportpath");

                    List<MacValidationModel> macValidationModelList = getCsvToBean(weeklyReportsPath, ReportType.MAC_VALIDATION);
                    for (MacValidationModel macValidationModel : macValidationModelList) {
                        List<Integer> noteIds = connectionDAO.getEdgeNoteId(macValidationModel.getMacaddress(), macValidationModel, loadForAssignmentGuid);
                        System.out.println("Title  : " + macValidationModel.getFixtureid());
                        System.out.println("NoteIdSize  : " + noteIds);
                        if (noteIds.size() == 1) {
                            EdgeNoteView edgeNoteView = connectionDAO.getEdgeNoteViewById(noteIds);
                            if (edgeNoteView != null) {
                                updateReportStatus(ServerType.Install, macValidationModel, edgeNoteView, null);
                                //TODO
                                System.out.println("InventoryCalled");
                                EdgeNoteView edgeNoteViewList = inventoryDAO.getEdgeNoteViewByTitle(macValidationModel.getMacaddress());
                                if (edgeNoteViewList != null) {
                                    String notebookId = edgeNoteViewList.getNotebookid();
                                    EdgeNotebookEntity edgeNotebookEntity = inventoryDAO.getEdgeNotebookEntity(notebookId);
                                    //  System.out.println("NotebookName : " + edgeNotebookEntity.getNotebookName());
                                    updateReportStatus(ServerType.Inventory, macValidationModel, edgeNoteView, edgeNotebookEntity);
                                }
                            }
                        } else if (noteIds.size() > 1) {
                            macValidationModel.setInstallStatus(Status.MoreThan_One_FormsPresent.toString());
                        }
                    }
                    String outputFile = sourceFile + File.separator + fileNameWithoutExtn + "_verified.csv";
                    System.out.println("MacValidationPath : " + outputFile);
                    Utils.writeMacValidationData(macValidationModelList, outputFile);
                    System.out.println("noteval" + macValidationModelList.size());
                }
                // prints filename and directory name
                //  System.out.println(file1.getName());
            }

        }
        return "Success";
    }

    public void test() {
        EdgeNoteView edgeNoteView1 = inventoryDAO.getEdgeNoteView("73da59f7-c3a5-4afb-9f8a-7f5a6503df7f");
        System.out.println("notebookId" + edgeNoteView1.getNotebookid());
        EdgeNotebookEntity edgeNotebookEntity = inventoryDAO.getEdgeNotebookEntity(edgeNoteView1.getNotebookid());
        System.out.println("NotebookName : " + edgeNotebookEntity.getNotebookName());
        System.out.println("NotebookName : " + edgeNotebookEntity);

        if (edgeNoteView1 != null) {
            System.out.println("inventoryNote :" + edgeNoteView1.getTitle());
        }

    }

    private String getFilename() {
        long millisec = System.currentTimeMillis();
        Date date = new Date(millisec);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd_MMM_yyyy_HH:mm:ss");
        return dateFormat.format(date);
    }

    public void updateReportStatus(ServerType type, MacValidationModel macValidationModel, EdgeNoteView edgeNoteView, EdgeNotebookEntity edgeNotebookEntity) {
        switch (type) {
            case Install:
                if (macValidationModel.getFixtureid().equals(edgeNoteView.getTitle()) && edgeNoteView.getCreatedBy().equals(macValidationModel.getUser())) {
                    macValidationModel.setInstallStatus(Status.Success.toString());
                } else {
                    macValidationModel.setInstallStatus(Status.Failure.toString());
                }
                break;
            case Inventory:
                if (edgeNotebookEntity != null && macValidationModel.getAssigneduser().equals(edgeNotebookEntity.getNotebookName())) {
                    macValidationModel.setInventoryStatus(Status.Success.toString());
                } else {
                    macValidationModel.setInventoryStatus(Status.Failure.toString());
                }
                break;
        }
    }

    public String startReplacementProcess(File sourceFile) {
        String formTemplateGuid = PropertiesReader.getProperties().getProperty("edge.formtemplateGuid");
        String newMacAddressID = PropertiesReader.getProperties().getProperty("edge.newmacId");
        String existingMacAddressID = PropertiesReader.getProperties().getProperty("edge.existingmacId");
        File[] files = sourceFile.listFiles();
        for (File replacementFile : files) {
            String fileNameWithoutExtn = Utils.getFileNameWithoutExtension(replacementFile);
            String replaceInputPath = replacementFile.getPath();
            List<ReplaceModel> replaceModelList = getCsvToBean(replaceInputPath, ReportType.REPLACE);
            System.out.println("re" + replaceModelList.size());
            for (ReplaceModel replaceModel : replaceModelList) {
                EdgeNoteView edgeNoteView = connectionDAO.getEdgeNoteViewFromTitle(replaceModel.getFixtureid());
                if (edgeNoteView != null) {
                    List<EdgeFormEntity> edgeFormEntityList = connectionDAO.getEdgeNoteEntity(edgeNoteView.getNoteId(), "");
                    for (EdgeFormEntity edgeFormEntity : edgeFormEntityList) {
                        try {
                            if (edgeFormEntity.getFormTemplateGuid().equals(formTemplateGuid)) {
                                String formDef = edgeFormEntity.getFormDef();
                                Type listType = new TypeToken<ArrayList<EdgeFormData>>() {
                                }.getType();
                                List<EdgeFormData> edgeFormDataList = gson.fromJson(formDef, listType);
                                String newNodeMacAddress = valueById(edgeFormDataList, Integer.parseInt(newMacAddressID));
                                String existingMacAddress = valueById(edgeFormDataList, Integer.parseInt(existingMacAddressID));
                                System.out.println(existingMacAddress);
                                if (newNodeMacAddress != null && existingMacAddress != null && newNodeMacAddress.equals(existingMacAddress)) {
                                    replaceModel.setStatus(Status.Success.toString());
                                } else {
                                    replaceModel.setStatus(Status.Failure.toString());
                                }
                            }
                        } catch (NoValueException e) {
                            replaceModel.setStatus(Status.Failure.toString());
                        }
                    }

                }
                // test
            }
            String outputFilePath = sourceFile + File.separator + fileNameWithoutExtn + "_verified.csv";
            System.out.println("ReplacePath : " + outputFilePath);
            Utils.writeReplacementData(replaceModelList, outputFilePath);
            System.out.println("replacementPath : " + outputFilePath);
        }
        return "Success";
    }

    private String valueById(List<EdgeFormData> edgeFormDatas, int id) throws NoValueException {
        EdgeFormData edgeFormTemp = new EdgeFormData();
        edgeFormTemp.setId(id);

        int pos = edgeFormDatas.indexOf(edgeFormTemp);
        if (pos != -1) {
            EdgeFormData edgeFormData = edgeFormDatas.get(pos);
            String value = edgeFormData.getValue();
            if (value == null || value.trim().isEmpty()) {
                throw new NoValueException("Value is Empty or null." + value);
            }
            return value;
        } else {
            throw new NoValueException(id + " is not found.");
        }
    }

}
