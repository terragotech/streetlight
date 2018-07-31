package com.terragoedge.slvinterface.service;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.slvinterface.dao.ConnectionDAO;
import com.terragoedge.slvinterface.dao.tables.SlvDevice;
import com.terragoedge.slvinterface.dao.SLVInterfaceDAO;
import com.terragoedge.slvinterface.dao.tables.SlvSyncDetails;
import com.terragoedge.slvinterface.enumeration.EdgeComponentType;
import com.terragoedge.slvinterface.enumeration.SLVProcess;
import com.terragoedge.slvinterface.enumeration.Status;
import com.terragoedge.slvinterface.exception.*;
import com.terragoedge.slvinterface.json.slvInterface.Action;
import com.terragoedge.slvinterface.json.slvInterface.ConfigurationJson;
import com.terragoedge.slvinterface.json.slvInterface.Id;
import com.terragoedge.slvinterface.model.EdgeFormData;
import com.terragoedge.slvinterface.model.EdgeNote;
import com.terragoedge.slvinterface.model.FormData;
import com.terragoedge.slvinterface.utils.PropertiesReader;
import com.terragoedge.slvinterface.utils.ResourceDetails;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import static com.terragoedge.slvinterface.utils.Utils.dateFormat;

public class SlvInterfaceService extends AbstractSlvService {
    Properties properties = null;
    Gson gson = null;
    JsonParser jsonParser = null;
    final Logger logger = Logger.getLogger(SlvInterfaceService.class);
    private SlvRestService slvRestService = null;
    private List<ConfigurationJson> configurationJsonList = null;
    private ConnectionDAO connectionDAO = null;
    private SLVInterfaceDAO slvInterfaceDAO = null;

    public SlvInterfaceService() {
        this.properties = PropertiesReader.getProperties();
        this.gson = new Gson();
        this.jsonParser = new JsonParser();
        slvRestService = new SlvRestService();
        connectionDAO = ConnectionDAO.INSTANCE;
        slvInterfaceDAO = new SLVInterfaceDAO();
    }

    public void start() {
        // Get Configuration JSON
        configurationJsonList = getConfigJson();
        String accessToken = getEdgeToken();
        logger.info("AccessToken is :" + accessToken);
        if (accessToken == null) {
            logger.error("Edge Invalid UserName and Password.");
            return;
        }

        List<String> noteGuids = slvInterfaceDAO.getNoteGuids();
        if (noteGuids == null) {
            logger.error("Error while getting already process note list.");
            return;
        }
        String formTemplateGuid = properties.getProperty("streetlight.edge.formtemplateguid");
        String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");

        url = url + PropertiesReader.getProperties().getProperty("streetlight.edge.url.notes.get");
        String systemDate = PropertiesReader.getProperties().getProperty("streetlight.edge.customdate");
        String controllerStrIdValue = properties.getProperty("streetlight.controller.str.id");
        String geozoneId = properties.getProperty("streetlight.slv.geozoneid");

        logger.info("GetNotesUrl :" + url);
        List<String> noteGuidsList = connectionDAO.getEdgeNoteGuid(formTemplateGuid);
       // noteGuidsList.clear();
      //  noteGuidsList.add("52187364-150d-490f-9c10-031d4fcf5a62");*/
        for (String edgenoteGuid : noteGuidsList) {
            if (!noteGuids.contains(edgenoteGuid)) {
                String restUrl = url + edgenoteGuid;
                ResponseEntity<String> responseEntity = slvRestService.getRequest(restUrl, false, accessToken);
                logger.info("notes response :" + url);
                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    String notesData = responseEntity.getBody();
                    logger.info("notes response from server :" + notesData);
                    System.out.println(notesData);
                    // Convert notes Json to List of notes object
                    Type listType = new TypeToken<ArrayList<EdgeNote>>() {
                    }.getType();
                    List<EdgeNote> edgeNoteList = new ArrayList<>();
                    EdgeNote edgeNote = gson.fromJson(notesData, EdgeNote.class);
                    edgeNoteList.add(edgeNote);
                    //  List<EdgeNote> edgeNoteList = gson.fromJson(notesData, listType);
                    for (EdgeNote edgenote : edgeNoteList) {
                        logger.info("ProcessNoteTitle is :" + edgenote.getTitle());
                        processEdgeNote(edgenote, noteGuids, formTemplateGuid, geozoneId, controllerStrIdValue);
                    }
                }
            }
        }
        // Get data from server.
        logger.info("Process End :");

    }


    private void processEdgeNote(EdgeNote edgeNote, List<String> noteGuids, String formTemplateGuid, String geozoneId, String controllerStrid) {
        try {
            // Check whether this note is already processed or not.
            if (!noteGuids.contains(edgeNote.getNoteGuid())) {
                SlvSyncDetails slvSyncDetailsError = new SlvSyncDetails();
                try {
                    slvSyncDetailsError.setNoteGuid(edgeNote.getNoteGuid());
                    slvSyncDetailsError.setNoteName(edgeNote.getTitle());
                    slvSyncDetailsError.setNoteCreatedBy(edgeNote.getCreatedBy());
                    slvSyncDetailsError.setNoteCreatedDateTime(edgeNote.getCreatedDateTime());
                    List<Object> paramsList = new ArrayList<Object>();
                    List<FormData> formDatasList = edgeNote.getFormData();
                    Map<String, FormData> formDataMaps = new HashMap<String, FormData>();
                    boolean isFormTemplatePresent = false;
                    for (FormData formData : formDatasList) {
                        formDataMaps.put(formData.getFormTemplateGuid(), formData);
                        if (formData.getFormTemplateGuid().equals(formTemplateGuid)) {
                            isFormTemplatePresent = true;
                        }
                    }
                    // Check Note has correct form template or not. If not present no need to process.
                    if (!isFormTemplatePresent) {
                        slvSyncDetailsError.setErrorDetails("Form Template [" + formTemplateGuid + "] is not present in this note.");
                    } else {
                        processSingleForm(formDataMaps.get(formTemplateGuid), edgeNote, slvSyncDetailsError, paramsList, configurationJsonList, geozoneId, controllerStrid);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // TODO
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Based on configuration corresponding process going to take place.
     *
     * @param formData
     * @param edgeNote
     * @param slvSyncDetail
     * @param paramsList
     * @param configurationJsonList
     */
    public void processSingleForm(FormData formData, EdgeNote edgeNote, SlvSyncDetails slvSyncDetail, List<Object> paramsList, List<ConfigurationJson> configurationJsonList, String geozoneId, String controllerStrIdValue) {
        try {
            List<EdgeFormData> edgeFormDataList = formData.getFormDef();
            for (ConfigurationJson configurationJson : configurationJsonList) {
                List<Action> actionList = configurationJson.getAction();
                if (checkActionType(edgeFormDataList, actionList)) {
                    switch (configurationJson.getType()) {
                        case NEW_DEVICE:
                            String macAddress = validateMACAddress(configurationJson, formData.getFormDef(), edgeNote,paramsList);
                            slvSyncDetail.setMacAddress(macAddress);
                            slvSyncDetail.setSelectedAction(SLVProcess.NEW_DEVICE.toString());
                            boolean isDeviceExist = isAvailableDevice(edgeNote.getTitle());
                            if (!isDeviceExist) {
                                createDevice(edgeNote, slvSyncDetail, geozoneId);
                            }
                            processSetDevice(edgeFormDataList, configurationJson, edgeNote, paramsList, slvSyncDetail, controllerStrIdValue);
                          //  replaceOLC(controllerStrIdValue, edgeNote.getTitle(), slvSyncDetail.getMacAddress());
                            break;
                        case UPDATE_DEVICE:
                            slvSyncDetail.setSelectedAction(SLVProcess.UPDATE_DEVICE.toString());
                            processSetDevice(edgeFormDataList, configurationJson, edgeNote, paramsList, slvSyncDetail, controllerStrIdValue);
                            break;
                        case REPLACE_DEVICE:
                            slvSyncDetail.setSelectedAction(SLVProcess.REPLACE_DEVICE.toString());
                            processReplaceDevice(formData, configurationJson, edgeNote, paramsList, slvSyncDetail, controllerStrIdValue);
                            break;

                    }
                }
            }

        } catch (ReplaceOLCFailedException | NoValueException | QRCodeAlreadyUsedException e) {
            slvSyncDetail.setErrorDetails(e.getMessage());
            slvSyncDetail.setStatus(Status.Failure.toString());
        } catch (DeviceUpdationFailedException | DeviceCreationFailedException e) {
            slvSyncDetail.setStatus(Status.Failure.toString());
        }
        slvSyncDetail.setProcessedDateTime(new Date().getTime());
        connectionDAO.saveSlvSyncDetails(slvSyncDetail);
    }

    public boolean isAvailableDevice(String idOnController) {
        SlvDevice slvDevice = connectionDAO.getSlvDevices(idOnController);
        return slvDevice != null;
    }

    public void createDevice(EdgeNote edgeNote, SlvSyncDetails slvSyncDetails, String geoZoneId) throws DeviceCreationFailedException {
        if (geoZoneId != null) {
            ResponseEntity<String> responseEntity = createDevice(edgeNote, geoZoneId);
            String status = responseEntity.getStatusCode().toString();
            String responseBody = responseEntity.getBody();
            if ((status.equalsIgnoreCase("200") || status.equalsIgnoreCase("ok"))
                    && !responseBody.contains("<status>ERROR</status>")) {
                logger.info("Device Created Successfully, NoteId:" + edgeNote.getNoteGuid() + "-"
                        + edgeNote.getTitle());
                slvSyncDetails.setDeviceCreationStatus(Status.Success.toString());
                createSLVDevice(edgeNote.getTitle());
            } else {
                logger.info("Device Created Failure, NoteId:" + edgeNote.getNoteGuid() + "-"
                        + edgeNote.getTitle());
                slvSyncDetails.setDeviceCreationStatus(Status.Failure.toString());
                slvSyncDetails.setErrorDetails(status);
                throw new DeviceCreationFailedException(edgeNote.getNoteGuid() + "-" + edgeNote.getTitle());
            }
        } else {
            slvSyncDetails.setDeviceCreationStatus(Status.Failure.toString());
            slvSyncDetails.setErrorDetails("GeoZone should not be empty.");
            throw new DeviceCreationFailedException("GeoZone should not be empty.");
        }
    }

    public void createSLVDevice(String title) {
        SlvDevice slvDevice = new SlvDevice();
        slvDevice.setDeviceId(title);
        slvDevice.setProcessedDateTime(new Date().getTime());
        connectionDAO.saveSlvDevices(slvDevice);
    }

    public void processReplaceDevice(FormData formData, ConfigurationJson configurationJson, EdgeNote edgeNote, List<Object> paramsList, SlvSyncDetails slvSyncDetails, String controllerStrIdValue) throws NoValueException, QRCodeAlreadyUsedException, ReplaceOLCFailedException, DeviceUpdationFailedException {
        List<EdgeFormData> edgeFormDatas = formData.getFormDef();
        String macAddress = validateMACAddress(configurationJson, edgeFormDatas, edgeNote,paramsList);
        slvSyncDetails.setMacAddress(macAddress);

        validateExistingMACAddress(configurationJson, edgeFormDatas, edgeNote, paramsList, slvSyncDetails);
        replaceOLC(controllerStrIdValue, edgeNote.getTitle(), "");
        processSetDevice(edgeFormDatas, configurationJson, edgeNote, paramsList, slvSyncDetails, controllerStrIdValue);
        replaceOLC(controllerStrIdValue, edgeNote.getTitle(), macAddress);

    }

    // replaceOLC(controllerStrIdValue, edgeNote.getTitle(), newNodeMacAddress);
    public void processSetDevice(List<EdgeFormData> edgeFormDataList, ConfigurationJson configurationJson, EdgeNote edgeNote, List<Object> paramsList, SlvSyncDetails slvSyncDetails, String controllerStrIdValue) throws NoValueException, DeviceUpdationFailedException {
        //setValues and Empty ReplaceOLC
        List<Id> idList = configurationJson.getIds();
        // Process Fixture value
        Id fixureID = getIDByType(idList, EdgeComponentType.FIXTURE.toString());
        if (fixureID != null) {
            processFixtureScan(edgeFormDataList, fixureID, edgeNote, paramsList, slvSyncDetails);
        }
        paramsList.add("idOnController=" + edgeNote.getTitle());
        paramsList.add("controllerStrId=" + controllerStrIdValue);
        addOtherParams(edgeNote, paramsList);
        setDeviceValues(paramsList, slvSyncDetails);

    }

    public void processFixtureScan(List<EdgeFormData> edgeFormDataList, Id fixureID, EdgeNote edgeNote, List<Object> paramsList, SlvSyncDetails slvSyncDetails) throws NoValueException {
        try {
            String fixtureScan = valueById(edgeFormDataList, fixureID.getId());
            if (fixtureScan.startsWith("00") && slvSyncDetails.getMacAddress() != null && !slvSyncDetails.getMacAddress().startsWith("00")) {
                String temp = slvSyncDetails.getMacAddress();
                slvSyncDetails.setMacAddress(fixtureScan);
                fixtureScan = temp;
            }
            //addStreetLightData("luminaire.installdate", dateFormat(edgeNote.getCreatedDateTime()), paramsList);
            //addStreetLightData("install.date", dateFormat(edgeNote.getCreatedDateTime()), paramsList);                                                                                                // TODO
            buildFixtureStreetLightData(fixtureScan, paramsList, edgeNote);
        } catch (NoValueException e) {
            if (fixureID.isRequired()) {
                throw new NoValueException(e.getMessage());
            }
        } catch (InValidBarCodeException e) {
            slvSyncDetails.setErrorDetails("Warn: InValide Bar Code " + e.getMessage());
        }


    }


    private void validateExistingMACAddress(ConfigurationJson configurationJson, List<EdgeFormData> edgeFormDataList, EdgeNote edgeNote, List<Object> paramsList, SlvSyncDetails slvSyncDetails) throws NoValueException {
        List<Id> idList = configurationJson.getIds();
        Id existingMacAddressId = getIDByType(idList, EdgeComponentType.EXISTING_MAC.toString());
        if (existingMacAddressId != null) {
            try {
                String existingMacAddress = valueById(edgeFormDataList, existingMacAddressId.getId());
                String validMacAddressResult = validateMACAddress(existingMacAddress, edgeNote.getTitle(), null);
                validMacAddressResult = validMacAddressResult + " replaced on " + dateFormat(edgeNote.getCreatedDateTime());
                addStreetLightData("comment", validMacAddressResult, paramsList);
            } catch (NoValueException e) {
                if (existingMacAddressId.isRequired()) {
                    throw new NoValueException(e.getMessage());
                }
            } catch (QRCodeNotMatchedException e) {
                slvSyncDetails.setErrorDetails("Warn: QR Code not matched with Existing QR Value." + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    public boolean checkActionType(List<EdgeFormData> edgeFormData, List<Action> actionList) {
        String actionValue = null;
        for (Action action : actionList) {
            try {
                actionValue = valueById(edgeFormData, action.getId());
                if (actionValue.equals(action.getValue())) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
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

    public List<ConfigurationJson> getConfigJson() {
        JsonParser jsonParser = new JsonParser();
        try (FileReader reader = new FileReader(ResourceDetails.CONFIG_JSON_PATH)) {
            String configjson = jsonParser.parse(reader).toString();
            Type listType = new TypeToken<ArrayList<ConfigurationJson>>() {
            }.getType();
            List<ConfigurationJson> configurationJsons = gson.fromJson(configjson, listType);
            return configurationJsons;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();

    }


    private String validateMACAddress(ConfigurationJson configurationJson, List<EdgeFormData> edgeFormDataList, EdgeNote edgeNote,List<Object> paramsList) throws NoValueException, QRCodeAlreadyUsedException {
        List<Id> idList = configurationJson.getIds();
        Id macID = getIDByType(idList, EdgeComponentType.MAC.toString());
        if (macID != null) {
            try {
                String newNodeMacAddress = valueById(edgeFormDataList, macID.getId());
                checkMacAddressExists(newNodeMacAddress, edgeNote.getTitle());
                addStreetLightData("MacAddress", newNodeMacAddress, paramsList);
                return newNodeMacAddress;
            } catch (NoValueException e) {
                if (macID.isRequired()) {
                    throw new NoValueException("MAC Address is Empty: " + edgeNote.getTitle());
                }
            }
        }
        return null;

    }


}
