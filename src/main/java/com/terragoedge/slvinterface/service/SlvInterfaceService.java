package com.terragoedge.slvinterface.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.terragoedge.slvinterface.dao.tables.SlvSyncDetail;
import com.terragoedge.slvinterface.exception.*;
import com.terragoedge.slvinterface.model.*;
import com.terragoedge.slvinterface.utils.PropertiesReader;
import com.terragoedge.slvinterface.utils.Utils;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
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

    public SlvInterfaceService() {
        super();
        jsonParser = new JsonParser();
        slvService = new SlvService();
        this.properties = PropertiesReader.getProperties();
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
        noteGuidsList.add(properties.getProperty("noteguid"));*/
        System.out.println("Processed NoteList: " + noteGuidsList);
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
                            EdgeNote edgeNote = gson.fromJson(notesData, EdgeNote.class);
                            logger.info("Processed Note title size :" + gson.toJson(edgeNote));
                            logger.info("ProcessNoteGuid is :" + edgenoteGuid);
                            logger.info("ProcessNoteTitle is :" + edgeNote.getTitle());
                            List<String> formTemplateGuids = getProcessingFormTemplateGuids(edgeNote);
                            if(formTemplateGuids.size() == 0){
                                logger.error("There is no processing form attached to this note. So skipping."+edgeNote.getNoteGuid());
                            }else if(formTemplateGuids.size() > 1){
                                logger.error("There are more processing form attached to this note. So skipping."+edgeNote.getNoteGuid());
                            }else {
                                logger.error("There is only one processing form attached to this note. So continuing process."+edgeNote.getNoteGuid());
                                String formTemplateGuid = formTemplateGuids.get(0);
                                processEdgeNote(edgeNote, formTemplateGuid, false, formTemplateGuid.equals(installFormtemplateGuid) ? installworkFlowFormId : newFixtureworkFlowFormId);
                            }
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
    public JPSWorkflowModel processWorkFlowForm(List<FormData> formDataList, EdgeNote edgeNote, WorkFlowFormId installWorkflowFormId) {
        JPSWorkflowModel jpsWorkflowModel = new JPSWorkflowModel();
        if (edgeNote.getEdgeNotebook() != null) {
            jpsWorkflowModel.setNotebookName(edgeNote.getEdgeNotebook().getNotebookName());
            jpsWorkflowModel.setDimmingGroupName(edgeNote.getEdgeNotebook().getNotebookName());
        }
        String categoryStrId = properties.getProperty("streetlight.categorystr.id");
        String controllerStrId = properties.getProperty("streetlight.controller.str.id");

        String nodeTypeStrId = properties.getProperty("streetlight.slv.equipment.type");
        Feature feature = (Feature) GeoJSONFactory.create(edgeNote.getGeometry());
        // parse Geometry from Feature
        GeoJSONReader reader = new GeoJSONReader();
        Geometry geom = reader.read(feature.getGeometry());
        if (edgeNote.getGeometry() != null) {
            jpsWorkflowModel.setLat(String.valueOf(geom.getCoordinate().y));
            jpsWorkflowModel.setLng(String.valueOf(geom.getCoordinate().x));
        } else {
            logger.info("There is no location given note :" + edgeNote.getTitle());
        }
        jpsWorkflowModel.setControllerStrId(controllerStrId);
        jpsWorkflowModel.setEquipmentType(nodeTypeStrId);
        jpsWorkflowModel.setProvider_name(properties.getProperty("jps.provider.name"));
        jpsWorkflowModel.setLowvoltagethreshold(Integer.valueOf(properties.getProperty("jps.low.voltage.thershold")));
        jpsWorkflowModel.setHighvoltagethreshold(Integer.valueOf(properties.getProperty("jps.high.voltage.thershold")));
        jpsWorkflowModel.setCategoryStrId(categoryStrId);
        jpsWorkflowModel.setLocationtype(properties.getProperty("jps.location.type"));
        jpsWorkflowModel.setModel(properties.getProperty("jps.model"));
        for (FormData formData : formDataList) {
            List<EdgeFormData> edgeFormDataList = formData.getFormDef();

            String feederName = getFormValue(edgeFormDataList,installWorkflowFormId.getFeederName());
            if (nullCheck(feederName))
                jpsWorkflowModel.setStreetdescription(feederName);

            String streetName = getFormValue(edgeFormDataList,installWorkflowFormId.getStreetName());
            if (nullCheck(streetName))
                jpsWorkflowModel.setAddress1(streetName);

            String parrish = getFormValue(edgeFormDataList,installWorkflowFormId.getParrish());
            if (nullCheck(parrish))
                jpsWorkflowModel.setCity(parrish);

            String division = getFormValue(edgeFormDataList,installWorkflowFormId.getDivision());
            if (nullCheck(division))
                jpsWorkflowModel.setDivision(division);

            String newPoleNumber = getFormValue(edgeFormDataList,installWorkflowFormId.getNewPoleNumber());
            if (nullCheck(newPoleNumber)) {
                jpsWorkflowModel.setIdOnController(newPoleNumber);
                jpsWorkflowModel.setName(newPoleNumber);
                jpsWorkflowModel.setUtillocationid(newPoleNumber);
            }

            String retrofitStatus = getFormValue(edgeFormDataList,installWorkflowFormId.getRetrofitStatus());
            if (nullCheck(retrofitStatus))
                jpsWorkflowModel.setInstallStatus(retrofitStatus);

            String newLampWattage = getFormValue(edgeFormDataList,installWorkflowFormId.getNewLampWatage());
            if (nullCheck(newLampWattage)) {
                String lampType = newLampWattage;
                if (lampType.equals("Other")) {
                    processLampType(edgeFormDataList, jpsWorkflowModel);
                } else {
                    Pattern pattern = Pattern.compile("\\d+");
                    Matcher matcher = pattern.matcher(newLampWattage);
                    if (matcher != null && matcher.find()) {
                        jpsWorkflowModel.setPower(matcher.group(0).trim());
                    }
                    jpsWorkflowModel.setLampType(newLampWattage);
                }
            }

            String oldPoleNumber = getFormValue(edgeFormDataList,installWorkflowFormId.getOldPoleNumber());
            if (nullCheck(oldPoleNumber))
                jpsWorkflowModel.setOldPoleNumber(oldPoleNumber);

            String poleType = getFormValue(edgeFormDataList,installWorkflowFormId.getPoleType());
            if (nullCheck(poleType))
                jpsWorkflowModel.setPole_type(poleType);

            String macaddress = getFormValue(edgeFormDataList,installWorkflowFormId.getMacAddress());
            if (nullCheck(macaddress))
                if (macaddress != null && macaddress.startsWith("00135"))
                    jpsWorkflowModel.setMacAddress(macaddress);

            String mastArmLength = getFormValue(edgeFormDataList,installWorkflowFormId.getMastArmLength());
            if (nullCheck(mastArmLength))
                jpsWorkflowModel.setFixing_type(mastArmLength);

            String mastArmLengthOther = getFormValue(edgeFormDataList,installWorkflowFormId.getMastArmLengthOther());
            if (nullCheck(mastArmLengthOther))
                jpsWorkflowModel.setOtherFixtureType(mastArmLengthOther);

            String conversionDate = getFormValue(edgeFormDataList,installWorkflowFormId.getConversionDate());
            if (nullCheck(conversionDate)) {
                jpsWorkflowModel.setInstall_date(Utils.installDateFormat(Long.valueOf(conversionDate)));
            }

            String feed = getFormValue(edgeFormDataList,installWorkflowFormId.getFeed());
            if (nullCheck(feed))
                jpsWorkflowModel.setNetwork_type(feed);

            String poleShape = getFormValue(edgeFormDataList,installWorkflowFormId.getPoleShape());
            if (nullCheck(poleShape))
                jpsWorkflowModel.setPole_shape(poleShape);

            String condition = getFormValue(edgeFormDataList,installWorkflowFormId.getCondition());
            if (nullCheck(condition))
                jpsWorkflowModel.setPole_status(condition);

            String poleNumber = getFormValue(edgeFormDataList,installWorkflowFormId.getPoleNumber());
            if (nullCheck(poleNumber))
                jpsWorkflowModel.setLocation_zipcode(poleNumber);
        }
        String geozonePaths = "/" + jpsWorkflowModel.getNotebookName() + "/" + jpsWorkflowModel.getAddress1();
        jpsWorkflowModel.setGeozonePath(geozonePaths);
        return jpsWorkflowModel;
    }

    public void processLampType(List<EdgeFormData> edgeFormDataList, JPSWorkflowModel jpsWorkflowModel) {
        String otherLampId = properties.getProperty("jps.edge.otherlamptype");//201
        String otherWattage = properties.getProperty("jps.edge.otherwattage");//188
        String otherNewLampModel = valueById(edgeFormDataList, Integer.parseInt(otherLampId));
        String lampWattage = valueById(edgeFormDataList, Integer.parseInt(otherWattage));
        jpsWorkflowModel.setLampType(otherNewLampModel);
        if(lampWattage != null){
            jpsWorkflowModel.setPower(lampWattage);
        }

    }

    private boolean nullCheck(String data) {
        return (data != null && data.length() > 0 && !data.contains("null")) ? true : false;
    }

    @Override
    public void buildFixtureStreetLightData(String data, List<Object> paramsList, EdgeNote edgeNote) throws InValidBarCodeException {

    }

    private String getFormValue(List<EdgeFormData> edgeFormDatas,int id){
        EdgeFormData edgeFormData = new EdgeFormData();
        edgeFormData.setId(id);
        int pos = edgeFormDatas.indexOf(edgeFormData);
        if(pos > -1){
            return edgeFormDatas.get(pos).getValue();
        }
        return "";
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
        edgeSlvUrl = edgeSlvUrl+"/notesGuid?lastSyncTime=";

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

    private void processMaintenanceInstallForm(List<FormData> formDatasList,EdgeNote edgeNote,WorkFlowFormId installWorkflowFormId){
        try {
            // List<FormData> formDatasList should have maintenance forms only
            JPSWorkflowModel jpsWorkflowModel = processWorkFlowForm(formDatasList, edgeNote, installWorkflowFormId);
            jpsWorkflowModel.setInstallStatus("CONVERTED");
            slvService.processSlv(jpsWorkflowModel, edgeNote);
        }catch (Exception e){
            logger.error("Error in processMaintenanceInstallForm : ",e);
        }
    }
}
