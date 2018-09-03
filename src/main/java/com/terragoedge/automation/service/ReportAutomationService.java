package com.terragoedge.automation.service;

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
import com.terragoedge.slvinterface.model.EdgeNote;
import com.terragoedge.slvinterface.utils.PropertiesReader;
import com.terragoedge.slvinterface.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ReportAutomationService extends AbstractReportService {
    ConnectionDAO connectionDAO = null;
    InventoryDAO inventoryDAO = null;

    public ReportAutomationService() {
        connectionDAO = ConnectionDAO.INSTANCE;
        inventoryDAO = InventoryDAO.INSTANCE;
    }

    public void run() {
     //   test();
        String enabledWeeklyReport = PropertiesReader.getProperties().getProperty("edge.reports.weekly");
        if (enabledWeeklyReport != null && Boolean.parseBoolean(enabledWeeklyReport)) {
            String weeklyReportsPath = PropertiesReader.getProperties().getProperty("edge.reports.reportpath");
            List<MacValidationModel> macValidationModelList = getCsvToBean(weeklyReportsPath,ReportType.MAC_VALIDATION);
            for (MacValidationModel macValidationModel : macValidationModelList) {
                List<Integer> noteIds = connectionDAO.getEdgeNoteId(macValidationModel.getMacaddress());
                System.out.println("Title  : " + macValidationModel.getFixtureid());
                System.out.println("NoteIdSize  : " + noteIds.size());
                if (noteIds.size() > 0) {
                    EdgeNoteView edgeNoteView = connectionDAO.getEdgeNoteViewById(noteIds);
                    if (edgeNoteView != null) {
                        updateReportStatus(ServerType.Install, macValidationModel, edgeNoteView, null);
                        //TODO
                        System.out.println("InventoryCalled");
                        EdgeNoteView edgeNoteViewList = inventoryDAO.getEdgeNoteViewByTitle(macValidationModel.getMacaddress());

                       String notebookId = edgeNoteViewList.getNotebookid();
                        EdgeNotebookEntity edgeNotebookEntity = inventoryDAO.getEdgeNotebookEntity(notebookId);

                      //  System.out.println("NotebookName : " + edgeNotebookEntity.getNotebookName());
                        updateReportStatus(ServerType.Inventory, macValidationModel, edgeNoteView, edgeNotebookEntity);
                    }
                }
            }
               Utils.writeMacValidationData(macValidationModelList,getFilename());
            System.out.println("noteval" + macValidationModelList.size());
        }

    }

    public void test() {
        EdgeNoteView edgeNoteView1 = inventoryDAO.getEdgeNoteView("73da59f7-c3a5-4afb-9f8a-7f5a6503df7f");
        System.out.println("notebookId"+edgeNoteView1.getNotebookid());
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
    public void run1(){
        String replaceInputPath = PropertiesReader.getProperties().getProperty("edge.reports.reportpath");
        List<ReplaceModel> replaceModelList = getCsvToBean(replaceInputPath,ReportType.REPLACE);

    }

}
