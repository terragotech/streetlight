package com.terragoedge.slvinterface.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.sun.xml.internal.bind.v2.TODO;
import com.terragoedge.slvinterface.dao.tables.SlvSyncDetails;
import com.terragoedge.slvinterface.enumeration.EdgeComponentType;
import com.terragoedge.slvinterface.enumeration.SLVProcess;
import com.terragoedge.slvinterface.enumeration.Status;
import com.terragoedge.slvinterface.exception.DeviceCreationFailedException;
import com.terragoedge.slvinterface.exception.NoValueException;
import com.terragoedge.slvinterface.exception.QRCodeAlreadyUsedException;
import com.terragoedge.slvinterface.exception.QRCodeNotMatchedException;
import com.terragoedge.slvinterface.json.slvInterface.Action;
import com.terragoedge.slvinterface.json.slvInterface.ConfigurationJson;
import com.terragoedge.slvinterface.json.slvInterface.Id;
import com.terragoedge.slvinterface.model.EdgeFormData;
import com.terragoedge.slvinterface.model.EdgeNote;
import com.terragoedge.slvinterface.model.FormData;
import com.terragoedge.slvinterface.utils.MessageConstants;
import com.terragoedge.slvinterface.utils.PropertiesReader;
import org.apache.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

public class SlvInterfaceService extends AbstractSlvService {
    Properties properties = null;
    Gson gson = null;
    JsonParser jsonParser = null;
    final Logger logger = Logger.getLogger(SlvInterfaceService.class);
    private EdgeService edgeService = null;
    private SlvRestService slvRestService = null;
    private List<ConfigurationJson> configurationJsonList = null;

    public SlvInterfaceService() {
        this.properties = PropertiesReader.getProperties();
        this.gson = new Gson();
        this.jsonParser = new JsonParser();
        edgeService = new EdgeService();
        slvRestService = new SlvRestService();
    }

    public void start() {
        System.out.println("Started");
        logger.info("Process Started");
        configurationJsonList = getConfigJson();
        // TODO getNoteguids
        List<String> noteGuids = new ArrayList<>();
        //  List<String> noteGuids = streetlightDao.getNoteIds();
        String accessToken = getEdgeToken();
        logger.info("AccessToken is :" + accessToken);
        if (accessToken == null) {
            logger.error("Edge Invalid UserName and Password.");
            return;
        }
        List<EdgeNote> edgenoteList = null;
        String formTemplateGuid = properties.getProperty("streetlight.kingcity.streetlight_installation_formtemplate_guid");
        String controllerStrIdValue = properties.getProperty("streetlight.edge.form.controllerStrIdValue");
        String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");

        url = url + PropertiesReader.getProperties().getProperty("streetlight.edge.url.notes.get");
        String systemDate = PropertiesReader.getProperties().getProperty("streetlight.edge.customdate");

        if (systemDate == null || systemDate.equals("false")) {
            String yesterday = getYesterdayDate();
            url = url + "modifiedAfter=" + yesterday;
        }
        logger.info("GetNotesUrl :" + url);
        url = "https://amerescousa.terragoedge.com/edgeServer//rest/notes/68fc6ade-f043-4e3f-8e5c-ecab20bc2b63";
        System.out.println("Url" + url);
        ResponseEntity<String> responseEntity = slvRestService.getRequest(url, false, accessToken);
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
                try {
                    if (!noteGuids.contains(edgenote.getNoteGuid())) {
                        SlvSyncDetails kingCitySyncModel = new SlvSyncDetails();
                        kingCitySyncModel.setNoteGuid(edgenote.getNoteGuid());
                        kingCitySyncModel.setNoteName(edgenote.getTitle());
                        List<Object> paramsList = new ArrayList<Object>();
                        List<FormData> formDatasList = edgenote.getFormData();
                        Map<String, FormData> formDataMaps = new HashMap<String, FormData>();
                        for (FormData formData : formDatasList) {
                            formDataMaps.put(formData.getFormTemplateGuid(), formData);
                        }
                        logger.info("processedNoteTitle : " + edgenote.getTitle() + "-" + edgenote.getNoteGuid());
                        // checkFormNoteProcess(formDataMaps, edgenote, kingCitySyncModel, paramsList, formTemplateGuid, controllerStrIdValue);
                        processSingleForm(formDataMaps.get(formTemplateGuid), edgeNote, kingCitySyncModel, paramsList, configurationJsonList);
                        //   kingCityDao.insertProcessedNotes(kingCitySyncModel);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void processSingleForm(FormData formData, EdgeNote edgeNote, SlvSyncDetails slvSyncDetail, List<Object> paramsList, List<ConfigurationJson> configurationJsonList) {
        List<EdgeFormData> edgeFormDataList = formData.getFormDef();
        for (ConfigurationJson configurationJson : configurationJsonList) {
            List<Action> actionList = configurationJson.getAction();
            if (checkActionType(edgeFormDataList, actionList)) {
                switch (SLVProcess.valueOf(configurationJson.getType())) {
                    case NEW_DEVICE:
                        List<Id> idList = configurationJson.getIds();
                        //TODO create
                        break;
                    case UPDATE_DEVICE:
                        processUpdateDevice(edgeFormDataList, configurationJson, edgeNote, paramsList, slvSyncDetail);
                        break;
                    case REPLACE_DEVICE:
                        //TODO replacedevice
                        break;
                    case UPDATE_REPLACE:
                        //TODO updatereplace
                        break;
                }
            }
        }
    }

    public void processUpdateDevice(List<EdgeFormData> edgeFormDataList, ConfigurationJson configurationJson, EdgeNote edgeNote, List<Object> paramsList, SlvSyncDetails slvSyncDetails) {
        List<Id> idList = configurationJson.getIds();
        String existingNodeMacAddress = null;
        String newNodeMacAddress = null;
        String fixtureScan = null;
        String geoZoneId = properties.getProperty("streetlight.url.geozoneid");
        String controllerStrIdValue = null;  //TODO controllerStarValue
        // Get Existing Node MAC Address value
        Id existingMacID = getIDByType(idList, EdgeComponentType.MAC.toString());
        if (existingMacID != null) {
            try {
                existingNodeMacAddress = valueById(edgeFormDataList, existingMacID.getId());
                slvSyncDetails.setMacAddress(existingNodeMacAddress);
                logger.info("Existing NodeMacAddress " + existingNodeMacAddress);
                if (existingMacID.isRequired()) {
                    validateMACAddress(existingNodeMacAddress, edgeNote.getTitle(), geoZoneId);
                }
            } catch (QRCodeNotMatchedException e1) {
                logger.info("Validate macAddress Exception", e1);
                slvSyncDetails.setErrorDetails(MessageConstants.REPLACE_MAC_NOT_MATCH);
                slvSyncDetails.setStatus(MessageConstants.ERROR);
                return;
            } catch (Exception e) {
                slvSyncDetails.setErrorDetails(MessageConstants.OLD_MAC_ADDRESS_NOT_AVAILABLE);
                slvSyncDetails.setStatus(MessageConstants.ERROR);
                logger.info("Existing macAddress Exception", e);
            }
        }
        Id newMacID = getIDByType(idList, EdgeComponentType.MAC.toString());
        if (newMacID != null) {
            try {
                newNodeMacAddress = valueById(edgeFormDataList, newMacID.getId());
                logger.info("New NodeMacAddress " + newNodeMacAddress);
                if (existingMacID.isRequired()) {
                    checkMacAddressExists(newNodeMacAddress, edgeNote.getTitle());
                }
            } catch (QRCodeAlreadyUsedException e1) {
                logger.info("Validate macAddress Exception", e1);
                slvSyncDetails.setErrorDetails(MessageConstants.REPLACE_MAC_NOT_MATCH);
                slvSyncDetails.setStatus(MessageConstants.ERROR);
                return;
            } catch (Exception e) {
                slvSyncDetails.setErrorDetails(MessageConstants.NEW_MAC_ADDRESS_NOT_AVAILABLE);
                slvSyncDetails.setStatus(MessageConstants.ERROR);
                logger.info("NewNode macAddress Exception", e);
            }
        }
        Id fixureID = getIDByType(idList, EdgeComponentType.FIXTURE.toString());
        if (fixureID != null) {
            try {
                fixtureScan = valueById(edgeFormDataList, fixureID.getId());
                if (fixtureScan != null && !fixtureScan.isEmpty())
                    buildFixtureStreetLightData(fixtureScan, paramsList, edgeNote);
            } catch (Exception e) {
                slvSyncDetails.setErrorDetails(MessageConstants.FIXTURE_CODE_NOT_AVAILABLE);
                slvSyncDetails.setStatus(MessageConstants.ERROR);
                logger.info("Fixture Value Exception", e);
            }
        }
        //check mac addrees exist
        try {
            paramsList.add("idOnController=" + edgeNote.getTitle());
            addOtherParams(edgeNote, paramsList);
            int errorCode = setDeviceValues(paramsList);
            if (errorCode != 0) {
                slvSyncDetails.setErrorDetails(MessageConstants.ERROR_UPDATE_DEVICE_VAL);
                slvSyncDetails.setStatus(MessageConstants.ERROR);
                return;
            } else {
                // replace OlC
                replaceOLC(controllerStrIdValue, edgeNote.getTitle(), newNodeMacAddress);// insert mac address
                logger.info("ReplaceOlcCalled :" + edgeNote.getTitle() + " - " + newNodeMacAddress);
                slvSyncDetails.setStatus(MessageConstants.SUCCESS);
            }
        } catch (Exception e) {
            slvSyncDetails.setErrorDetails(e.getMessage());
            slvSyncDetails.setStatus(MessageConstants.ERROR);
            e.printStackTrace();
        }
    }

    public boolean checkActionType(List<EdgeFormData> edgeFormData, List<Action> actionList) {
        String actionValue = null;
        for (Action action : actionList) {
            try {
                actionValue = valueById(edgeFormData, action.getId());
                if (actionValue.equals(action.getValue())) {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public void checkFormNoteProcess(Map<String, FormData> formDataMaps, EdgeNote edgeNote, SlvSyncDetails kingCitySyncModel, List<Object> paramsList, String formTemplateGuid, String controllerStrIdValue) {
        kingCitySyncModel.setNoteGuid(edgeNote.getNoteGuid());
        kingCitySyncModel.setNoteName(edgeNote.getTitle());
        kingCitySyncModel.setNoteCreatedDateTime(edgeNote.getCreatedDateTime());
        FormData formData = formDataMaps.get(formTemplateGuid);
        // Process Fixture Form data
        List<EdgeFormData> fixtureFromDef = formData.getFormDef();
        String attributes = null;
        String action = null;
        try {
            action = valueById(fixtureFromDef, Integer.parseInt(properties.getProperty("streetlight.edge.action")));
            logger.info("FormTemplateActionValue : " + action);
            if (action.equals("New Streetlight")) {
                newStreetLightData(controllerStrIdValue, fixtureFromDef, paramsList, kingCitySyncModel,
                        edgeNote);
            } else if (action.equals("Update Streetlight")) {
                updateStreetLight(controllerStrIdValue, fixtureFromDef, paramsList, kingCitySyncModel,
                        edgeNote);
            } else if (action.equals("Remove Streetlight")) {
                // removeStreetLightData(controllerStrIdValue, fixtureFromDef, paramsList, kingCitySyncModel,edgeNote);
            }
        } catch (NoValueException e) {
            kingCitySyncModel.setErrorDetails(MessageConstants.ACTION_NO_VAL);
            kingCitySyncModel.setStatus(MessageConstants.ERROR);
            return;
        }
    }


    public void newStreetLightData(String controllerStrIdValue, List<EdgeFormData> edgeFormDatas, List<Object> paramsList, SlvSyncDetails kingCitySyncModel,
                                   EdgeNote edgenote) {
        String geoZoneId = PropertiesReader.getProperties().getProperty("streetlight.kingcity.url.geozoneid");
        if (geoZoneId != null) {
            try {
                ResponseEntity<String> responseEntity = createDevice(edgenote, geoZoneId);
                String status = responseEntity.getStatusCode().toString();
                String responseBody = responseEntity.getBody();
                if ((status.equalsIgnoreCase("200") || status.equalsIgnoreCase("ok"))
                        && !responseBody.contains("<status>ERROR</status>")) {
                    logger.info("Device Created Successfully, NoteId:" + edgenote.getNoteGuid() + "-"
                            + edgenote.getTitle());
                    kingCitySyncModel.setStatus(Status.Success.toString());
                    // kingCityDao.insertDeviceId(edgenote.getTitle());
                } else {
                    try {
                        logger.info("Device Created Failure, NoteId:" + edgenote.getNoteGuid() + "-"
                                + edgenote.getTitle());
                        kingCitySyncModel.setStatus(Status.Failure.toString());
                        kingCitySyncModel.setErrorDetails(status);
                        throw new DeviceCreationFailedException(edgenote.getNoteGuid() + "-" + edgenote.getTitle());
                    } catch (DeviceCreationFailedException e) {
                        logger.info("Device creation DeviceCreationFailedException", e);
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                logger.info("Device Created Exception", e);
                kingCitySyncModel.setStatus(MessageConstants.ERROR);
                kingCitySyncModel.setErrorDetails(MessageConstants.DEVICE_CREATION_EXCEPTION);
                return;
            }

            String newNodeMacAddress = null;
            // Get New Node MAC Address value
            try {
                newNodeMacAddress = valueById(edgeFormDatas, Integer.parseInt(properties.getProperty("streetlight.kingcity.url.new_mac_address")));
                kingCitySyncModel.setMacAddress(newNodeMacAddress);
                logger.info("newNodeMacAddress : " + newNodeMacAddress);
            } catch (NoValueException e) {
                kingCitySyncModel.setStatus(MessageConstants.ERROR);
                kingCitySyncModel.setErrorDetails(MessageConstants.NEW_MAC_ADDRESS_NOT_AVAILABLE);
                return;
            }

            //check mac addrees exist
            try {
                checkMacAddressExists(newNodeMacAddress, edgenote.getTitle());
            } catch (Exception e) {
                kingCitySyncModel.setStatus(MessageConstants.ERROR);
                kingCitySyncModel.setErrorDetails(MessageConstants.MAC_ALREADY_USED + "- " + e.getMessage());
                logger.info("checkMacAddressExists error : ", e);
                return;
            }
            try {
                paramsList.add("idOnController=" + edgenote.getTitle());
                addOtherParams(edgenote, paramsList);
                int errorCode = setDeviceValues(paramsList);
                if (errorCode != 0) {
                    logger.info("setDeviceValue  error code: " + errorCode);
                    kingCitySyncModel.setErrorDetails(MessageConstants.ERROR_UPDATE_DEVICE_VAL);
                    kingCitySyncModel.setStatus(MessageConstants.ERROR);
                    return;
                } else {
                    // replace OlC
                    //replaceOLC(controllerStrIdValue, edgenote.getTitle(), newNodeMacAddress);// insert mac address
                    kingCitySyncModel.setStatus(MessageConstants.SUCCESS);
                }
            } catch (Exception e) {
                logger.info("setDeviceValues error : ", e);
            }
            clearAndUpdateDeviceData(edgenote.getTitle(), controllerStrIdValue);

        }
    }

    public void updateStreetLight(String controllerStrIdValue, List<EdgeFormData> edgeFormDatas, List<Object> paramsList, SlvSyncDetails loggingModel,
                                  EdgeNote edgeNote) {
        String existingNodeMacAddress = null;
        String newNodeMacAddress = null;
        String geoZoneId = properties.getProperty("streetlight.kingcity.url.geozoneid");
        // Get Existing Node MAC Address value
        try {
            existingNodeMacAddress = valueById(edgeFormDatas,
                    Integer.parseInt(properties.getProperty("streetlight.kingcity.url.existing_mac_address")));
            loggingModel.setMacAddress(existingNodeMacAddress);
            logger.info("Existing NodeMacAddress " + existingNodeMacAddress);
        } catch (NoValueException e) {
            e.printStackTrace();
            logger.info("Existing NodeMacAddressException ", e);
            loggingModel.setErrorDetails(MessageConstants.OLD_MAC_ADDRESS_NOT_AVAILABLE);
            loggingModel.setStatus(MessageConstants.ERROR);
            return;
        }

        // Get New Node MAC Address value
        try {
            newNodeMacAddress = valueById(edgeFormDatas, Integer.parseInt(properties.getProperty("streetlight.kingcity.url.new_mac_address")));
            loggingModel.setMacAddress(newNodeMacAddress);
            logger.info("New NodeMacAddress " + newNodeMacAddress);
        } catch (NoValueException e) {
            e.printStackTrace();
            logger.info("New NodeMacAddressException ", e);
            loggingModel.setErrorDetails(MessageConstants.NEW_MAC_ADDRESS_NOT_AVAILABLE);
            loggingModel.setStatus(MessageConstants.ERROR);
            return;
        }
//validate mac address
        try {
            validateMACAddress(existingNodeMacAddress, edgeNote.getTitle(), geoZoneId);
        } catch (QRCodeNotMatchedException e1) {
            logger.info("Validate macAddress Exception", e1);
            loggingModel.setErrorDetails(MessageConstants.REPLACE_MAC_NOT_MATCH);
            loggingModel.setStatus(MessageConstants.ERROR);
            return;
        }
        //check mac addrees exist
        try {
            checkMacAddressExists(newNodeMacAddress, edgeNote.getTitle());
        } catch (Exception e) {
            logger.info("checkMacAddressExists  Exception", e);
            loggingModel.setErrorDetails(MessageConstants.MAC_ALREADY_USED + "- " + e.getMessage());
            loggingModel.setStatus(MessageConstants.ERROR);
            return;
        }
        try {
            paramsList.add("idOnController=" + edgeNote.getTitle());
            addOtherParams(edgeNote, paramsList);
            int errorCode = setDeviceValues(paramsList);
            if (errorCode != 0) {
                loggingModel.setErrorDetails(MessageConstants.ERROR_UPDATE_DEVICE_VAL);
                loggingModel.setStatus(MessageConstants.ERROR);
                return;
            } else {
                // replace OlC
                replaceOLC(controllerStrIdValue, edgeNote.getTitle(), newNodeMacAddress);// insert mac address
                logger.info("ReplaceOlcCalled :" + edgeNote.getTitle() + " - " + newNodeMacAddress);
                loggingModel.setStatus(MessageConstants.SUCCESS);
            }
        } catch (Exception e) {
            loggingModel.setErrorDetails(e.getMessage());
            loggingModel.setStatus(MessageConstants.ERROR);
            e.printStackTrace();
        }
    }

    private String valueById(List<EdgeFormData> edgeFormDatas, int id) throws NoValueException {
        EdgeFormData edgeFormTemp = new EdgeFormData();
        edgeFormTemp.setId(id);

        int pos = edgeFormDatas.indexOf(edgeFormTemp);
        if (pos != -1) {
            EdgeFormData edgeFormData = edgeFormDatas.get(pos);
            String value = edgeFormData.getValue();
            if (value == null || value.trim().length() == 0) {
                throw new NoValueException("Value is Empty or null." + value);
            }
            return value;
        } else {
            throw new NoValueException(id + " is not found.");
        }
    }

    public List<ConfigurationJson> getConfigJson() {
        JsonParser jsonParser = new JsonParser();
        // String path = "./resources/config.json";
        String path = "./src/main/resources/config.json";
        try (FileReader reader = new FileReader(path)) {
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

    public String getYesterdayDate() {
        // 2017-11-01T13:00:00.000-00:00
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS-00:00");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return dateFormat.format(cal.getTime());
    }
}
