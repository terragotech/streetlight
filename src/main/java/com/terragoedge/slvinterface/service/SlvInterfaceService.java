package com.terragoedge.slvinterface.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.slvinterface.dao.tables.SlvSyncDetails;
import com.terragoedge.slvinterface.enumeration.Status;
import com.terragoedge.slvinterface.exception.DeviceCreationFailedException;
import com.terragoedge.slvinterface.exception.NoValueException;
import com.terragoedge.slvinterface.exception.QRCodeNotMatchedException;
import com.terragoedge.slvinterface.model.EdgeFormData;
import com.terragoedge.slvinterface.model.EdgeNote;
import com.terragoedge.slvinterface.model.FormData;
import com.terragoedge.slvinterface.utils.MessageConstants;
import com.terragoedge.slvinterface.utils.PropertiesReader;
import org.apache.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

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
        List<String> noteGuids = streetlightDao.getNoteIds();
        String accessToken = getEdgeToken();
        logger.info("AccessToken is :" +accessToken);
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
//            String yesterday = getYesterdayDate();
            //     url = url + "modifiedAfter=" + yesterday;
        }
        url ="https://kingcity.terragoedge.com//rest/notes/37c3453a-c630-4efa-9711-00bffc928001";
        logger.info("GetNotesUrl :" + url);
        System.out.println("Url"+url);
        ResponseEntity<String> responseEntity = slvRestService.getRequest(url);
        logger.info("notes response :" + url);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String notesData = responseEntity.getBody();
            logger.info("notes response from server :" + notesData);
            System.out.println(notesData);
            // Convert notes Json to List of notes object
            Type listType = new TypeToken<ArrayList<EdgeNote>>() {
            }.getType();
            List<EdgeNote> edgeNoteList=new ArrayList<>();
            EdgeNote edgeNote = gson.fromJson(notesData,EdgeNote.class);
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
                        logger.info("processedNoteTitle : " +edgenote.getTitle()+"-"+edgenote.getNoteGuid());
                        checkFormNoteProcess(formDataMaps, edgenote, kingCitySyncModel, paramsList, formTemplateGuid, controllerStrIdValue);
                        kingCityDao.insertProcessedNotes(kingCitySyncModel);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
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
            logger.info("FormTemplateActionValue : "+ action);
            if (action.equals("New Streetlight")) {
                newStreetLightData(controllerStrIdValue, fixtureFromDef, paramsList, kingCitySyncModel,
                        edgeNote);
            } else if (action.equals("Update Streetlight")) {
                updateStreetLight(controllerStrIdValue, fixtureFromDef, paramsList, kingCitySyncModel,
                        edgeNote);
            } else if (action.equals("Remove Streetlight")) {
                removeStreetLightData(controllerStrIdValue, fixtureFromDef, paramsList, kingCitySyncModel,
                        edgeNote);
            }
        } catch (NoValueException e) {
            kingCitySyncModel.setErrorDetails(MessageConstants.ACTION_NO_VAL);
            kingCitySyncModel.setStatus(MessageConstants.ERROR);
            return;
        }
    }

    private String getEdgeToken() {
        String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
        String userName = properties.getProperty("streetlight.edge.username");
        String password = properties.getProperty("streetlight.edge.password");
        url = url + "/oauth/token?grant_type=password&username=" + userName + "&password=" + password
                + "&client_id=edgerestapp";
        ResponseEntity<String> responseEntity = slvRestService.getRequest(url);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            JsonObject jsonObject = (JsonObject) jsonParser.parse(responseEntity.getBody());
            return jsonObject.get("access_token").getAsString();
        }
        return null;
    }

    public void newStreetLightData(String controllerStrIdValue, List<EdgeFormData> edgeFormDatas, List<Object> paramsList, SlvSyncDetails kingCitySyncModel,
                                   EdgeNote edgenote) {
        String geoZoneId = PropertiesReader.getProperties().getProperty("streetlight.kingcity.url.geozoneid");
        if (geoZoneId != null) {
            try{
                ResponseEntity<String> responseEntity = createDevice(edgenote, geoZoneId);
                String status = responseEntity.getStatusCode().toString();
                String responseBody = responseEntity.getBody();
                if ((status.equalsIgnoreCase("200") || status.equalsIgnoreCase("ok"))
                        && !responseBody.contains("<status>ERROR</status>")) {
                    logger.info("Device Created Successfully, NoteId:" + edgenote.getNoteGuid() + "-"
                            + edgenote.getTitle());
                    kingCitySyncModel.setStatus(Status.Success.toString());
                    kingCityDao.insertDeviceId(edgenote.getTitle());
                } else {
                    try {
                        logger.info("Device Created Failure, NoteId:" + edgenote.getNoteGuid() + "-"
                                + edgenote.getTitle());
                        kingCitySyncModel.setStatus(Status.Failure.toString());
                        kingCitySyncModel.setErrorDetails(status);
                        throw new DeviceCreationFailedException(edgenote.getNoteGuid() + "-" + edgenote.getTitle());
                    } catch (DeviceCreationFailedException e) {
                        logger.info("Device creation DeviceCreationFailedException",e);
                        e.printStackTrace();
                    }
                }
            }
            catch (Exception e){
                logger.info("Device Created Exception",e);
                kingCitySyncModel.setStatus(MessageConstants.ERROR);
                kingCitySyncModel.setErrorDetails(MessageConstants.DEVICE_CREATION_EXCEPTION);
                return;
            }

            String newNodeMacAddress = null;
            // Get New Node MAC Address value
            try {
                newNodeMacAddress = valueById(edgeFormDatas,Integer.parseInt(properties.getProperty("streetlight.kingcity.url.new_mac_address")));
                kingCitySyncModel.setMacAddress(newNodeMacAddress);
                logger.info("newNodeMacAddress : "+newNodeMacAddress);
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
                logger.info("checkMacAddressExists error : ",e);
                return;
            }
            try {
                paramsList.add("idOnController=" + edgenote.getTitle());
                addOtherParams(edgenote, paramsList);
                int errorCode = setDeviceValues(paramsList);
                if (errorCode != 0) {
                    logger.info("setDeviceValue  error code: "+errorCode);
                    kingCitySyncModel.setErrorDetails(MessageConstants.ERROR_UPDATE_DEVICE_VAL);
                    kingCitySyncModel.setStatus(MessageConstants.ERROR);
                    return;
                } else {
                    // replace OlC
                    //replaceOLC(controllerStrIdValue, edgenote.getTitle(), newNodeMacAddress);// insert mac address
                    kingCitySyncModel.setStatus(MessageConstants.SUCCESS);
                }
            } catch (Exception e) {
                logger.info("setDeviceValues error : ",e);
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
            logger.info("Existing NodeMacAddress "+existingNodeMacAddress);
        } catch (NoValueException e) {
            e.printStackTrace();
            logger.info("Existing NodeMacAddressException ",e);
            loggingModel.setErrorDetails(MessageConstants.OLD_MAC_ADDRESS_NOT_AVAILABLE);
            loggingModel.setStatus(MessageConstants.ERROR);
            return;
        }

        // Get New Node MAC Address value
        try {
            newNodeMacAddress = valueById(edgeFormDatas,Integer.parseInt(properties.getProperty("streetlight.kingcity.url.new_mac_address")));
            loggingModel.setMacAddress(newNodeMacAddress);
            logger.info("New NodeMacAddress "+newNodeMacAddress);
        } catch (NoValueException e) {
            e.printStackTrace();
            logger.info("New NodeMacAddressException ",e);
            loggingModel.setErrorDetails(MessageConstants.NEW_MAC_ADDRESS_NOT_AVAILABLE);
            loggingModel.setStatus(MessageConstants.ERROR);
            return;
        }
//validate mac address
        try {
            validateMACAddress(existingNodeMacAddress, edgeNote.getTitle(), geoZoneId);
        } catch (QRCodeNotMatchedException e1) {
            logger.info("Validate macAddress Exception",e1);
            loggingModel.setErrorDetails(MessageConstants.REPLACE_MAC_NOT_MATCH);
            loggingModel.setStatus(MessageConstants.ERROR);
            return;
        }
        //check mac addrees exist
        try {
            checkMacAddressExists(newNodeMacAddress, edgeNote.getTitle());
        } catch (Exception e) {
            logger.info("checkMacAddressExists  Exception",e);
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
                logger.info("ReplaceOlcCalled :"+edgeNote.getTitle()+" - "+newNodeMacAddress);
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

}
