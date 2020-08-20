package com.terragoedge.slvinterface.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.terragoedge.slvinterface.dao.tables.SlvSyncDetail;
import com.terragoedge.slvinterface.exception.*;
import com.terragoedge.slvinterface.maintenanceworkflow.MaintenanceWorkflowService;
import com.terragoedge.slvinterface.maintenanceworkflow.model.DataDiffResponse;
import com.terragoedge.slvinterface.model.*;
import com.terragoedge.slvinterface.utils.PropertiesReader;
import com.terragoedge.slvinterface.utils.Utils;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.wololo.geojson.Feature;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.jts2geojson.GeoJSONReader;

import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SlvInterfaceService extends AbstractSlvService {
    Properties properties = null;
    final Logger logger = Logger.getLogger(SlvInterfaceService.class);
    private SlvService slvService;
    private String installFormtemplateGuid = null;
    private String newFixtureFormtemplateGuid = null;
    private JsonParser jsonParser;
    MaintenanceWorkflowService maintenanceWorkflowService;
    private PromotedDataService promotedDataService;

    public SlvInterfaceService() {
        super();
        jsonParser = new JsonParser();
        slvService = new SlvService();
        this.properties = PropertiesReader.getProperties();
        maintenanceWorkflowService = new MaintenanceWorkflowService();
        promotedDataService = new PromotedDataService();
    }

    public void start() {
        System.out.println("Start method called");
        // Already Processed NoteGuids
        //  List<String> noteGuids = slvInterfaceDAO.getNoteGuids();
        WorkFlowFormId installworkFlowFormId = null;
        WorkFlowFormId newFixtureworkFlowFormId = null;
        WorkFlowFormId maintenanceInstallFormId = null;
        try {
               installworkFlowFormId = getConfigData("./resources/installWorkflow.json");
               newFixtureworkFlowFormId = getConfigData("./resources/newFixtureWorkflow.json");
               maintenanceInstallFormId = getConfigData("./resources/maintenanceInstallWorkflow.json");
        }catch (Exception e){
            e.printStackTrace();
            logger.error("Error while reading configuration file for form ids");
        }
        if(installworkFlowFormId == null || newFixtureworkFlowFormId == null){
            logger.error("Config data is null.So skipping this process.");
        }
        installFormtemplateGuid = properties.getProperty("streetlight.edge.install.formtemplateguid");
        newFixtureFormtemplateGuid = properties.getProperty("streetlight.edge.new_workflow.formtemplateguid");
        String url = properties.getProperty("streetlight.edge.url.main");

        url = url + properties.getProperty("streetlight.edge.url.notes.get");
        logger.info("GetNotesUrl :" + url);
        // Get List of noteid
        long maxSyncTime = connectionDAO.getMaxSyncTime();
        logger.info("max SyncTime: "+maxSyncTime);

        String edgeToken = getEdgeToken();
        if (edgeToken == null) {
            logger.error("Edge Invalid UserName and Password.");
            return;
        }

//        List<String> noteGuidsList = new ArrayList<>();
        String resync = properties.getProperty("com.slv.resync");
        List<String> noteGuidsList = new ArrayList<>();
        if(resync.equals("true")){
            String reSyncData = getResyncData("./resources/input.txt");
            if(reSyncData == null){
                logger.error("Resync data  is null");
            }else{
                String[] resyncItems = reSyncData.split(",");
                noteGuidsList = Arrays.asList(resyncItems);
            }
        }else {
            noteGuidsList = getNoteGuids(maxSyncTime,edgeToken);
//            noteGuidsList = connectionDAO.getEdgeNoteGuid(installFormtemplateGuid, newFixtureFormtemplateGuid, maxSyncTime);
        }
        /*List<String> noteGuidsList = new ArrayList<>();
        noteGuidsList.clear();
        noteGuidsList.add(properties.getProperty("noteguid"));
        System.out.println("Processed NoteList: " + noteGuidsList);*/
        //end
        for (String edgenoteGuid : noteGuidsList) {
            try {
                if (!isAlreadyProcessed(edgenoteGuid)) {
                    String restUrl = url + edgenoteGuid;

                    String accessToken = getEdgeToken();
                    System.out.println("AccessToken is :" + accessToken);
                    logger.info("AccessToken is :" + accessToken);
                    if (accessToken == null) {
                        logger.error("Edge Invalid UserName and Password.");
                    }else {
                        ResponseEntity<String> responseEntity = slvRestService.getRequest(restUrl, false, accessToken);
                        if (responseEntity.getStatusCode().is2xxSuccessful()) {
                            String notesData = responseEntity.getBody();
                            doProcess(notesData,edgenoteGuid,installworkFlowFormId,newFixtureworkFlowFormId,maintenanceInstallFormId);
                        }else{
                            logger.info("getting edge note from rest call is failed noteguid: "+edgenoteGuid);
                        }
                    }
                }else{
                    logger.info("this guid"+edgenoteGuid+" present in processed items");
                }
            } catch (Exception e) {
                logger.error("Error", e);
            }
        }
        logger.info("Process End :");
    }


    private void doProcess(String notesData,String edgenoteGuid ,WorkFlowFormId installworkFlowFormId ,
            WorkFlowFormId newFixtureworkFlowFormId ,
            WorkFlowFormId maintenanceInstallFormId ){
        EdgeNote edgeNote = gson.fromJson(notesData, EdgeNote.class);

        String skippingUserStr = properties.getProperty("com.edge.skipping.users");
        String[] skippingUserArr = skippingUserStr.split(",",-1);
        List<String> skippingUsers = Arrays.asList(skippingUserArr);
        if(skippingUsers.contains(edgeNote.getCreatedBy())){
            logger.error("This note skipped due to it's created by skipping user");
            return;
        }

        logger.info("Processed Note title size :" + gson.toJson(edgeNote));
        logger.info("ProcessNoteGuid is :" + edgenoteGuid);
        logger.info("ProcessNoteTitle is :" + edgeNote.getTitle());

        try {
            maintenanceWorkflowService.processMaintenanceWorkflow(edgeNote,maintenanceInstallFormId);
            return;
        }catch (NoDataChangeException e){
            logger.info("No Data change in Maintenance Workflow, so continue the old process...");
        }catch (SkipNoteException e){
            return;
        }


        List<String> formTemplateGuids = getProcessingFormTemplateGuids(edgeNote);
        if(formTemplateGuids.size() == 0){
            logger.error("There is no processing form attached to this note. So skipping."+edgeNote.getNoteGuid());
        }else if(formTemplateGuids.size() > 1){
            logger.error("There are more processing form attached to this note. So skipping."+edgeNote.getNoteGuid());
        }else {
            logger.error("There is only one processing form attached to this note. So continuing process."+edgeNote.getNoteGuid());
            String formTemplateGuid = formTemplateGuids.get(0);
            String config = null;
            if (formTemplateGuid.equals(installFormtemplateGuid)){
                config = properties.getProperty("streetlight.edge.install.checkrevisiondata.config");
            }else{
                config = properties.getProperty("streetlight.edge.newfixture.checkrevisiondata.config");
            }
            try{
                DataDiffResponse dataDiffResponse = maintenanceWorkflowService.compareRevisionData(edgeNote.getNoteGuid(),config);
                if (dataDiffResponse != null) {
                    processEdgeNote(edgeNote, formTemplateGuid, false, formTemplateGuid.equals(installFormtemplateGuid) ? installworkFlowFormId : newFixtureworkFlowFormId);
                }
            }catch (Exception e){
                logger.error("Error process install form: ",e);
            }
        }
    }

    public void startReport() {
        logger.info("start report process");
        slvService.startReport();
        logger.info("report process end");

    }

    public boolean isAlreadyProcessed(String noteguid) {
        SlvSyncDetail slvSyncDetail = connectionDAO.getSlvSyncDetails(noteguid);
        return (slvSyncDetail == null) ? false : true;
    }

    private void processEdgeNote(EdgeNote edgeNote, String formTemplateGuid, boolean isResync, WorkFlowFormId installWorkflowFormId) {
        try {
            // Check whether this note is already processed or not.
            List<FormData> formDataList = new ArrayList<>();
            try {
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
                    logger.info("Given formtemplates present in this note.: " + edgeNote.getTitle());
                    JPSWorkflowModel jpsWorkflowModel = processWorkFlowForm(formDatasList, edgeNote,installWorkflowFormId);
                    logger.info("JspWorkmodel json :" + gson.toJson(jpsWorkflowModel));
                    if(jpsWorkflowModel.getInstallStatus().equals("CONVERTED")){//CONVERTED
                        slvService.processSlv(jpsWorkflowModel, edgeNote);
                        promotedDataService.updatePromotedData(jpsWorkflowModel,edgeNote);
                    }else{
                        logger.error("Install status is not CONVERTED. So skipping this note: "+edgeNote.getNoteGuid());
                    }
                } else {
                    System.out.println("Wrong formtemplate");
                    logger.info("Wrong formtemplates Present");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error while processing this note: "+edgeNote.getNoteGuid());
            logger.error("Error while processing this note: "+e.getMessage());
        }
    }

    //streetlight.controller.str.id
    //




    @Override
    public void buildFixtureStreetLightData(String data, List<Object> paramsList, EdgeNote edgeNote) throws InValidBarCodeException {

    }



    private WorkFlowFormId getConfigData(String path){
        try {
            File file = new File(path);
            String jsonIds = IOUtils.toString(new FileReader(file));
            return gson.fromJson(jsonIds, WorkFlowFormId.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private String getResyncData(String path){
        try {
            File file = new File(path);
            String data = IOUtils.toString(new FileReader(file));
            return data;
        }catch (Exception e){
            e.printStackTrace();
            logger.error("Error while getting resync data: "+e.getMessage());
        }
        return null;
    }

    private List<String> getProcessingFormTemplateGuids(EdgeNote edgeNote){
        List<String> formTemplateGuids = new ArrayList<>();
        List<FormData> formDatas = edgeNote.getFormData();
        if(formDatas != null){
            for(FormData formData : formDatas){
                String formTemplateGuid = formData.getFormTemplateGuid();
                if(formTemplateGuid.equals(properties.getProperty("streetlight.edge.install.formtemplateguid")) || formTemplateGuid.equals(properties.getProperty("streetlight.edge.new_workflow.formtemplateguid"))){
                    formTemplateGuids.add(formData.getFormTemplateGuid());
                }
            }
        }
        return formTemplateGuids;
    }

    private List<String> getNoteGuids(long lastSyncTime,String accessToken){
        List<String> noteguids = new ArrayList<>();
        String edgeSlvUrl =  PropertiesReader.getProperties().getProperty("streetlight.edge.slvserver.url");
        edgeSlvUrl = edgeSlvUrl+"/notesGuid?withRevision=true&lastSyncTime=";

        if(lastSyncTime > 0){
            edgeSlvUrl = edgeSlvUrl + lastSyncTime;

        }else{
            lastSyncTime = System.currentTimeMillis() - (10 * 60000);
            edgeSlvUrl = edgeSlvUrl + lastSyncTime;
        }



        // Get NoteList from edgeserver
        ResponseEntity<String> edgeSlvServerResponse = slvRestService.getRequest(edgeSlvUrl, false, accessToken);

        if (edgeSlvServerResponse.getStatusCode().is2xxSuccessful()) {

            // Get Response String
            String notesGuids = edgeSlvServerResponse.getBody();
            System.out.println(notesGuids);

            JsonArray noteGuidsJsonArray = (JsonArray) jsonParser.parse(notesGuids);
            if(noteGuidsJsonArray != null &&  !noteGuidsJsonArray.isJsonNull()){
                for(JsonElement noteGuidJson : noteGuidsJsonArray){
                    String noteGuid = noteGuidJson.getAsString();
                    noteguids.add(noteGuid);
                }
            }

        } else {
            logger.error("Unable to get message from EdgeServer. Response Code is :" + edgeSlvServerResponse.getStatusCode());
        }
        return noteguids;
    }
}
