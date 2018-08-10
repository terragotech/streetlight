package com.terragoedge.slvinterface.service;

import com.google.gson.Gson;
import com.terragoedge.slvinterface.dao.ConnectionDAO;
import com.terragoedge.slvinterface.entity.EdgeFormEntity;
import com.terragoedge.slvinterface.entity.EdgeNoteView;
import com.terragoedge.slvinterface.entity.EdgeNotebookEntity;
import com.terragoedge.slvinterface.model.CsvReportModel;
import com.terragoedge.slvinterface.model.EdgeFormData;
import com.terragoedge.slvinterface.model.FormData;
import com.terragoedge.slvinterface.utils.Constants;
import com.terragoedge.slvinterface.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.supercsv.cellprocessor.FmtDate;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AmerescoReportService extends AbstractService {
    private Gson gson = null;
    private ConnectionDAO connectionDAO = null;

    public AmerescoReportService() {
        gson = new Gson();
        connectionDAO = ConnectionDAO.INSTANCE;
    }

    public void start() {
        String formTemplateGuid = "c8acc150-6228-4a27-bc7e-0fabea0e2b93";
        // List<String> noteGuidList = connectionDAO.getEdgeNoteGuid(formTemplateGuid);
        List<String> noteGuidList = readNoteGuidFromCSV();
        List<CsvReportModel> csvReportModelList = new ArrayList<>();
        //   noteGuidList.clear();
         // noteGuidList.add("cc8a1fd1-9649-4594-ab51-4a461f83dbbd");
        for (String noteGuid : noteGuidList) {
            EdgeNoteView edgeNoteView = connectionDAO.getEdgeNoteView(noteGuid);
            if (edgeNoteView != null) {
                List<EdgeFormEntity> edgeFormEntityList = connectionDAO.getEdgeFormEntities(edgeNoteView.getNoteId(), formTemplateGuid);
                if(edgeFormEntityList.size()==0){
                    System.out.println("guid : "+noteGuid);
                }
                for (EdgeFormEntity edgeFormEntity : edgeFormEntityList) {
                    CsvReportModel csvReportModel = new CsvReportModel(edgeNoteView);
                    csvReportModelList.add(csvReportModel);
                    FormData formData = new FormData(edgeFormEntity);
                    setNotebookName(edgeNoteView.getNotebookid(), csvReportModel);
                    setFormDataInfo(formData, csvReportModel);
                    processCDOTComponent(formData, edgeNoteView, csvReportModel);
                }
            }
        }
        try {
            Utils.writeCSVData(csvReportModelList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processCDOTComponent(FormData formData, EdgeNoteView edgeNoteView, CsvReportModel csvReportModel) {
        List<EdgeFormData> edgeFormDataList = formData.getFormDef();
        EdgeFormData edgeFormData = new EdgeFormData();
        edgeFormData.setId(24);
        int position = edgeFormDataList.indexOf(edgeFormData);
        if (position != -1) {
            EdgeFormData edgeFormData1 = edgeFormDataList.get(position);
           // if (edgeFormData1.getValue() != null && edgeFormData1.getValue().equals("Unable to Repair(CDOT Issue)")) {
                System.out.println("Process Form : " + edgeFormData1.getValue() + "" + edgeNoteView.getTitle());
                csvReportModel.setSelectedRepair(edgeFormData1.getValue());
                processEdgeNote(edgeFormDataList, csvReportModel);
           // }
        }
    }

    public void processEdgeNote(List<EdgeFormData> edgeFormDataList, CsvReportModel csvReportModel) {
        for (EdgeFormData edgeFormData : edgeFormDataList) {
            if (edgeFormData.getLabel() == null) {
                continue;
            }
            if (edgeFormData.getLabel().equals("Existing Fixture Information")) {
                csvReportModel.setExistingFixtureInformation(edgeFormData.getValue());
            } else if (edgeFormData.getLabel().equals("SL")) {
                csvReportModel.setSL(edgeFormData.getValue());
            } else if (edgeFormData.getLabel().equals("Controller Str Id")) {
                csvReportModel.setControllerStrId(edgeFormData.getValue());
            } else if (edgeFormData.getLabel().equals("Address")) {
                csvReportModel.setAddress(edgeFormData.getValue());
            } else if (edgeFormData.getLabel().equals("GeoZoneId")) {
                csvReportModel.setGeoZoneId(edgeFormData.getValue());
            } else if (edgeFormData.getLabel().equals("Child GeoZoneId")) {
                csvReportModel.setGhildGeoZoneId(edgeFormData.getValue());
            } else if (edgeFormData.getLabel().equals("Atlas Physical Page")) {
                csvReportModel.setAtlasPhysicalPage(edgeFormData.getValue());
            } else if (edgeFormData.getLabel().equals("Fixture Color")) {
                csvReportModel.setFixtureColor(edgeFormData.getValue());
            } else if (edgeFormData.getLabel().equals("CDOT Lamp Type")) {
                csvReportModel.setcDOTLampType(edgeFormData.getValue());
            } else if (edgeFormData.getLabel().equals("Color Code")) {
                csvReportModel.setColorCode(edgeFormData.getValue());
            } else if (edgeFormData.getLabel().equals("Fixture Code")) {
                csvReportModel.setFixtureCode(edgeFormData.getValue());
            } else if (edgeFormData.getLabel().equals("Mast arm angle")) {
                csvReportModel.setMastArmAngle(edgeFormData.getValue());
            } else if (edgeFormData.getLabel().equals("No of mast arms")) {
                csvReportModel.setMastArmsCount(edgeFormData.getValue());
            } else if (edgeFormData.getLabel().equals("Mast arm length")) {
                csvReportModel.setMastArmLength(edgeFormData.getValue());
            } else if (edgeFormData.getLabel().equals("Proposed context")) {
                csvReportModel.setProposedContext(edgeFormData.getValue());
            } else if (edgeFormData.getLabel().equals("Action")) {
                csvReportModel.setAction(edgeFormData.getValue());
            } else if (edgeFormData.getLabel().equals("Node MAC address")) {
                csvReportModel.setNodeMACAddress(edgeFormData.getValue());
            } else if (edgeFormData.getLabel().equals("Fixture QR Scan")) {
                csvReportModel.setFixtureQRScan1(edgeFormData.getValue());
            } else if (edgeFormData.getLabel().equals("Install status")) {
                csvReportModel.setInstallStatus(edgeFormData.getValue());
            } else if (edgeFormData.getLabel().equals("Skipped Fixture Reason")) {
                csvReportModel.setSkippedFixtureReason(edgeFormData.getValue());
            } else if (edgeFormData.getLabel().equals("Skipped Reason")) {
                csvReportModel.setSkippedReason(edgeFormData.getValue());
            } else if (edgeFormData.getLabel().equals("Repairs & Outages")) {
                csvReportModel.setRepairsAndOutages(edgeFormData.getValue());
            } else if (edgeFormData.getLabel().equals("Existing Node MAC Address") && edgeFormData.getId() == 36) {
                csvReportModel.setExistingNodeMACAddress1(edgeFormData.getValue());
            } else if (edgeFormData.getLabel().equals("New Node MAC Address") && edgeFormData.getId() == 26) {
                csvReportModel.setNewNodeMACAddress1(edgeFormData.getValue());
            } else if (edgeFormData.getLabel().equals("Existing Node MAC Address") || edgeFormData.getId() == 29) {
                csvReportModel.setExistingNodeMACAddress1(edgeFormData.getValue());
            } else if (edgeFormData.getLabel().equals("New Node MAC Address") || edgeFormData.getId() == 30) {
                csvReportModel.setNewNodeMACAddress2(edgeFormData.getValue());
            } else if (edgeFormData.getLabel().equals("Old Fixture QR Scan")) {
                csvReportModel.setOldFixtureQRScan(edgeFormData.getValue());
            } else if (edgeFormData.getLabel().equals("New Fixture QR Scan")) {
                csvReportModel.setNewFixtureQRScan(edgeFormData.getValue());
            } else if (edgeFormData.getLabel().equals("Reason for Replacement")) {
                csvReportModel.setReasonForReplacement(edgeFormData.getValue());
            }
        }
    }

    public void setNotebookName(String notebookId, CsvReportModel csvReportModel) {
        EdgeNotebookEntity edgeNotebookEntity = connectionDAO.getEdgeNotebookEntity(notebookId);
        if (edgeNotebookEntity != null) {
            csvReportModel.setNoteBookName(edgeNotebookEntity.getNotebookName());
        }
    }

    public void setFormDataInfo(FormData formData, CsvReportModel csvReportModel) {
        csvReportModel.setFormGuid(formData.getFormGuid());
        csvReportModel.setFormTemplateGuid(formData.getFormTemplateGuid());
        csvReportModel.setCategory(formData.getCategory());
        csvReportModel.setName(formData.getName());
        csvReportModel.setCreatedDate2(String.valueOf(formData.getCreatedDate()));
    }

    public List<String> readNoteGuidFromCSV() {
        List<String> guidList = new ArrayList<>();
        try {
            String data = null;
            BufferedReader fis = new BufferedReader(new FileReader("D:/Carton/onefiveeight.csv"));
            while ((data = fis.readLine()) != null) {
                try {
                    guidList.add(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return guidList;
    }
}
