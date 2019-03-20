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
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.wololo.geojson.Feature;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.jts2geojson.GeoJSONReader;

import java.io.BufferedReader;
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

        String formTemplateGuid = properties.getProperty("streetlight.edge.formtemplateguid");
        String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");

        url = url + PropertiesReader.getProperties().getProperty("streetlight.edge.url.notes.get");
        logger.info("GetNotesUrl :" + url);
        // Get List of noteid
      //  List<String> noteGuidsList = connectionDAO.getEdgeNoteGuid(formTemplateGuid);
        List<String> noteGuidsList = new ArrayList<>();
        noteGuidsList.clear();
        noteGuidsList.add(properties.getProperty("noteguid"));
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
                        processEdgeNote(edgeNote, formTemplateGuid, false);
                    }
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

    private void processEdgeNote(EdgeNote edgeNote, String formTemplateGuid, boolean isResync) {
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
                    JPSWorkflowModel jpsWorkflowModel = processWorkFlowForm(formDatasList, edgeNote);
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
    public JPSWorkflowModel processWorkFlowForm(List<FormData> formDataList, EdgeNote edgeNote) {
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
            for (EdgeFormData edgeFormData : edgeFormDataList) {
                if (edgeFormData != null) {
                    switch (edgeFormData.getId()) {
                        case 73:
                            if (nullCheck(edgeFormData.getValue()))
                                jpsWorkflowModel.setStreetdescription(edgeFormData.getValue());
                            break;
                        case 185:
                            if (nullCheck(edgeFormData.getValue()))
                                jpsWorkflowModel.setAddress1(edgeFormData.getValue());
                            break;
                        case 78:
                            if (nullCheck(edgeFormData.getValue()))
                                jpsWorkflowModel.setCity(edgeFormData.getValue());
                            break;
                        case 200:
                            if (nullCheck(edgeFormData.getValue())) {
                                jpsWorkflowModel.setIdOnController(edgeFormData.getValue());
                                jpsWorkflowModel.setName(edgeFormData.getValue());
                                jpsWorkflowModel.setUtillocationid(edgeFormData.getValue());
                            }
                            break;
                        case 198:
                            if (nullCheck(edgeFormData.getValue()))
                                jpsWorkflowModel.setInstallStatus(edgeFormData.getValue());
                            break;
                        case 187:
                            if (nullCheck(edgeFormData.getValue())) {
                                String lampType = edgeFormData.getValue();
                                if (lampType.equals("Other")) {
                                    processLampType(edgeFormDataList, jpsWorkflowModel);
                                } else {
                                    Pattern pattern = Pattern.compile("\\d+");
                                    Matcher matcher = pattern.matcher(edgeFormData.getValue());
                                    if (matcher != null && matcher.find()) {
                                        jpsWorkflowModel.setPower(matcher.group(0).trim());
                                    }
                                    jpsWorkflowModel.setLampType(edgeFormData.getValue());
                                }

                            }

                            break;
                        case 199:
                            if (nullCheck(edgeFormData.getValue()))
                                jpsWorkflowModel.setOldPoleNumber(edgeFormData.getValue());
                            break;
                        case 142:
                            if (nullCheck(edgeFormData.getValue()))
                                jpsWorkflowModel.setPole_type(edgeFormData.getValue());
                            break;
                        case 194:
                            if (nullCheck(edgeFormData.getValue()))
                                if (edgeFormData.getValue() != null && edgeFormData.getValue().startsWith("00135"))
                                    jpsWorkflowModel.setMacAddress(edgeFormData.getValue());
                            break;
                        case 189:
                            if (nullCheck(edgeFormData.getValue()))
                                jpsWorkflowModel.setFixing_type(edgeFormData.getValue());
                            break;
                        case 190:
                            if (nullCheck(edgeFormData.getValue()))
                                jpsWorkflowModel.setOtherFixtureType(edgeFormData.getValue());
                            break;
                        case 186:
                            if (nullCheck(edgeFormData.getValue())) {
                                jpsWorkflowModel.setInstall_date(Utils.installDateFormat(Long.valueOf(edgeFormData.getValue())));
                            }
                            break;
                        case 144:
                            if (nullCheck(edgeFormData.getValue()))
                                jpsWorkflowModel.setNetwork_type(edgeFormData.getValue());
                            break;
                        case 201:
                            if (nullCheck(edgeFormData.getValue()))
                                jpsWorkflowModel.setPole_shape(edgeFormData.getValue());
                            break;
                        case 140:
                            if (nullCheck(edgeFormData.getValue()))
                                jpsWorkflowModel.setPole_status(edgeFormData.getValue());
                            break;

                        case 203:
                            if (nullCheck(edgeFormData.getValue()))
                                jpsWorkflowModel.setLocation_zipcode(edgeFormData.getValue());
                            break;
                    }
                }
            }
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
}
