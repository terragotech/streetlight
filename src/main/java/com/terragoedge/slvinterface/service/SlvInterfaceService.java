package com.terragoedge.slvinterface.service;

import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.slvinterface.dao.tables.SlvDevice;
import com.terragoedge.slvinterface.dao.tables.SlvSyncDetail;
import com.terragoedge.slvinterface.enumeration.Status;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        // Already Processed NoteGuids
        //  List<String> noteGuids = slvInterfaceDAO.getNoteGuids();
        InstallWorkflowFormId installWorkflowFormId = null;
        String formTemplateGuid = properties.getProperty("streetlight.edge.formtemplateguid");
        try {
            if (formTemplateGuid.equals("442ab0e1-ae10-4fb5-a8f5-d1638b0a1fb8")) {// install workflow
                File file = new File("./resources/installWorkflow.json");
                String jsonIds = IOUtils.toString(new FileReader(file));
                installWorkflowFormId = gson.fromJson(jsonIds, InstallWorkflowFormId.class);
            } else {// new fixture workflow
                File file = new File("./resources/newFixtureWorkflow.json");
                String jsonIds = IOUtils.toString(new FileReader(file));
                installWorkflowFormId = gson.fromJson(jsonIds, InstallWorkflowFormId.class);
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.error("Error while reading configuration file for form ids");
        }
        String url = properties.getProperty("streetlight.edge.url.main");

        url = url + properties.getProperty("streetlight.edge.url.notes.get");
        logger.info("GetNotesUrl :" + url);
        String notebookGuid = properties.getProperty("jps.processing.notebookguid");
        // Get List of noteid
        List<String> noteGuidsList = connectionDAO.getEdgeNoteGuid(formTemplateGuid,notebookGuid);
        /*List<String> noteGuidsList = new ArrayList<>();
        noteGuidsList.clear();
        noteGuidsList.add(properties.getProperty("noteguid"));*/
        System.out.println("Processed NoteList: " + noteGuidsList);
        //end
        for (String edgenoteGuid : noteGuidsList) {
            try {
                if (!isAlreadyProcessed(edgenoteGuid)) {
                    String restUrl = url + edgenoteGuid;
                    ResponseEntity<String> responseEntity = slvRestService.getRequest(restUrl, false, accessToken);
                    if (responseEntity.getStatusCode().is2xxSuccessful()) {
                        String notesData = responseEntity.getBody();
                        EdgeNote edgeNote = gson.fromJson(notesData, EdgeNote.class);
                        logger.info("Processed Note title size :" + gson.toJson(edgeNote));
                        logger.info("ProcessNoteGuid is :" + edgenoteGuid);
                        logger.info("ProcessNoteTitle is :" + edgeNote.getTitle());
                        processEdgeNote(edgeNote, formTemplateGuid, false,installWorkflowFormId);
                    }else{
                        logger.info("getting edge note from rest call is failed noteguid: "+edgenoteGuid);
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

    private void processEdgeNote(EdgeNote edgeNote, String formTemplateGuid, boolean isResync,InstallWorkflowFormId installWorkflowFormId) {
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
                    slvService.processSlv(jpsWorkflowModel, edgeNote);
                } else {
                    System.out.println("Wrong formtemplate");
                    logger.info("Wrong formtemplates Present");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //streetlight.controller.str.id
    //
    public JPSWorkflowModel processWorkFlowForm(List<FormData> formDataList, EdgeNote edgeNote,InstallWorkflowFormId installWorkflowFormId) {
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
}
