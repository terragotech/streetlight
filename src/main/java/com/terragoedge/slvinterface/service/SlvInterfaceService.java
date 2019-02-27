package com.terragoedge.slvinterface.service;

import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.slvinterface.dao.tables.SlvDevice;
import com.terragoedge.slvinterface.dao.tables.SlvSyncDetail;
import com.terragoedge.slvinterface.enumeration.Status;
import com.terragoedge.slvinterface.exception.*;
import com.terragoedge.slvinterface.model.*;
import com.terragoedge.slvinterface.utils.PropertiesReader;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.*;

import static com.terragoedge.slvinterface.utils.Utils.dateFormat;

public class SlvInterfaceService extends AbstractSlvService {
    Properties properties = null;
    final Logger logger = Logger.getLogger(SlvInterfaceService.class);
    private SlvService slvService;

    public SlvInterfaceService() {
        super();
        slvService = new SlvService();
        this.properties = PropertiesReader.getProperties();
    }

    public void start() {
        System.out.println("Start method called");
        String accessToken = getEdgeToken();
        System.out.println("AccessToken is :" + accessToken);
        logger.info("AccessToken is :" + accessToken);
        if (accessToken == null) {
            logger.error("Edge Invalid UserName and Password.");
            return;
        }

        // Resync
        /*String dataReSync = PropertiesReader.getProperties().getProperty("streetlight.edge.data.resync");
        if (dataReSync != null && dataReSync.trim().equals("true")) {
            logger.info("ReSync Process Starts.");
            logger.info("ReSync Process Ends.");
            System.exit(0);
            return;
        }*/
        // Already Processed NoteGuids
        List<String> noteGuids =connectionDAO.getProcessedItems();
      //  List<String> noteGuids = slvInterfaceDAO.getNoteGuids();
        if (noteGuids == null) {
            logger.error("Error while getting already process note list.");
            return;
        }
        String formTemplateGuid = properties.getProperty("streetlight.edge.formtemplateguid");
        String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");

        url = url + PropertiesReader.getProperties().getProperty("streetlight.edge.url.notes.get");
        logger.info("GetNotesUrl :" + url);
        // Get List of noteid
        List<String> noteGuidsList = connectionDAO.getEdgeNoteGuid(formTemplateGuid);
        System.out.println("noteList: " + noteGuidsList);
        //end
        for (String edgenoteGuid : noteGuidsList) {
            try {
                if (!noteGuids.contains(edgenoteGuid)) {
                    String restUrl = url + edgenoteGuid;
                    ResponseEntity<String> responseEntity = slvRestService.getRequest(restUrl, false, accessToken);
                    if (responseEntity.getStatusCode().is2xxSuccessful()) {
                        String notesData = responseEntity.getBody();
                        List<EdgeNote> edgeNoteList = new ArrayList<>();
                        EdgeNote edgeNote = gson.fromJson(notesData, EdgeNote.class);
                        edgeNoteList.add(edgeNote);

                        for (EdgeNote edgenote : edgeNoteList) {
                            logger.info("ProcessNoteTitle is :" + edgenote.getTitle());
                            processEdgeNote(edgenote, noteGuids, formTemplateGuid, false);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error", e);
            }
        }
        logger.info("Process End :");
    }

    private void processEdgeNote(EdgeNote edgeNote, List<String> noteGuids, String formTemplateGuid, boolean isResync) {
        System.out.println("processEdgeNote :" + edgeNote.getTitle());
        try {
            // Check whether this note is already processed or not.
            if (!noteGuids.contains(edgeNote.getNoteGuid())) {
                List<FormData> formDataList = new ArrayList<>();
                try {
                    List<Object> paramsList = new ArrayList<Object>();
                    List<FormData> formDatasList = edgeNote.getFormData();
                    Map<String, FormData> formDataMaps = new HashMap<String, FormData>();
                    boolean isFormTemplatePresent = false;
                    for (FormData formData : formDatasList) {
                        formDataMaps.put(formData.getFormTemplateGuid(), formData);
                        if (formData.getFormTemplateGuid().equals(formTemplateGuid)) {
                            isFormTemplatePresent = true;
                            formDataList.add(formData);
                        }
                    }

                    if (isFormTemplatePresent) {
                        FormData formData = formDataMaps.get(formTemplateGuid);
                        List<EdgeFormData> edgeFormDataList = formData.getFormDef();
                        JPSWorkflowModel jpsWorkflowModel = processWorkFlowForm(formDatasList);
                        if(edgeNote.getEdgeNotebook()!=null){
                            jpsWorkflowModel.setNotebookName(edgeNote.getEdgeNotebook().getNotebookName());
                        }
                        slvService.processSlv(jpsWorkflowModel,edgeNote);
                    } else {
                        System.out.println("Wrong formtemplate");
                        logger.info("Wrong formtemplates Present");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //streetlight.controller.str.id
    //
    public JPSWorkflowModel processWorkFlowForm(List<FormData> formDataList) {
        JPSWorkflowModel jpsWorkflowModel = new JPSWorkflowModel();
        String categoryStrId = properties.getProperty("streetlight.categorystr.id");
        String controllerStrId = properties.getProperty("streetlight.controller.str.id");
        String geozonePath = properties.getProperty("streetlight.slv.geozonepath");
        jpsWorkflowModel.setControllerStrId(controllerStrId);
        jpsWorkflowModel.setProvider_name("JPS");
        jpsWorkflowModel.setGeozonePath(geozonePath);
        jpsWorkflowModel.setLowvoltagethreshold(216);
        jpsWorkflowModel.setHighvoltagethreshold(253);
        jpsWorkflowModel.setCategoryStrId(categoryStrId);
        jpsWorkflowModel.setLocationtype("LOCATION_TYPE_PREMISE");
        jpsWorkflowModel.setModel("TB784561877!lightNode01:lightNodeFunction6");
        for (FormData formData : formDataList) {
            List<EdgeFormData> edgeFormDataList = formData.getFormDef();
            for (EdgeFormData edgeFormData : edgeFormDataList) {
                if (edgeFormData != null) {
                    switch (edgeFormData.getLabel()) {
                        case "feedername":
                            if (nullCheck(edgeFormData.getValue()))
                                jpsWorkflowModel.setStreetdescription(edgeFormData.getValue());
                            break;
                        case "Street Name":
                            if (nullCheck(edgeFormData.getValue()))
                                jpsWorkflowModel.setAddress1(edgeFormData.getValue());
                            break;
                        case "PARISH":
                            if (nullCheck(edgeFormData.getValue()))
                                jpsWorkflowModel.setCity(edgeFormData.getValue());
                            break;
                        case "New Pole Number":
                            if (nullCheck(edgeFormData.getValue())) {
                                jpsWorkflowModel.setIdOnController(edgeFormData.getValue());
                                jpsWorkflowModel.setName(edgeFormData.getValue());
                            }
                            break;
                        case "Retrofit Status":
                            if (nullCheck(edgeFormData.getValue()))
                                jpsWorkflowModel.setInstallStatus(edgeFormData.getValue());
                            break;
                        case "lamptype":
                            if (nullCheck(edgeFormData.getValue()))
                                jpsWorkflowModel.setLampType(edgeFormData.getValue());
                            break;
                        case "Old Pole Number":
                            if (nullCheck(edgeFormData.getValue()))
                                jpsWorkflowModel.setOldPoleNumber(edgeFormData.getValue());
                            break;
                        case "Pole Type":
                            if (nullCheck(edgeFormData.getValue()))
                                jpsWorkflowModel.setPole_type(edgeFormData.getValue());
                            break;
                        case "MAC Address":
                            if (nullCheck(edgeFormData.getValue()))
                                if (edgeFormData.getValue() != null && edgeFormData.getValue().startsWith("00135"))
                                    jpsWorkflowModel.setMacAddress(edgeFormData.getValue());
                            break;
                        case "Observed Mast Arm Length":
                            if (nullCheck(edgeFormData.getValue()))
                                jpsWorkflowModel.setFixing_type(edgeFormData.getValue());
                            break;
                        case "Mast Arm Length - Other":
                            if (nullCheck(edgeFormData.getValue()))
                                jpsWorkflowModel.setOtherFixtureType(edgeFormData.getValue());
                            break;
                        case "Conversion Date":
                            if (nullCheck(edgeFormData.getValue()))
                                jpsWorkflowModel.setInstall_date(edgeFormData.getValue());
                            break;
                        case "Feed":
                            if (nullCheck(edgeFormData.getValue()))
                                jpsWorkflowModel.setNetwork_type(edgeFormData.getValue());
                            break;
                        case "Pole Shape":
                            if (nullCheck(edgeFormData.getValue()))
                                jpsWorkflowModel.setPole_shape(edgeFormData.getValue());
                            break;
                        case "Condition":
                            if (nullCheck(edgeFormData.getValue()))
                                jpsWorkflowModel.setPole_type(edgeFormData.getValue());
                            break;
                        case "facilityID":
                            if (nullCheck(edgeFormData.getValue()))
                                jpsWorkflowModel.setLocation_zipcode(edgeFormData.getValue());
                            break;
                    }
                }
            }
        }
        return jpsWorkflowModel;
    }

    private boolean nullCheck(String data) {
        return (data != null && data.length() > 0 && !data.contains("null")) ? true : false;
    }

    @Override
    public void buildFixtureStreetLightData(String data, List<Object> paramsList, EdgeNote edgeNote) throws InValidBarCodeException {

    }
}
