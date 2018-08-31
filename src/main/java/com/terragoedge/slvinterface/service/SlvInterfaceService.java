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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import static com.terragoedge.slvinterface.utils.Utils.dateFormat;

public abstract  class SlvInterfaceService extends AbstractSlvService {
    Properties properties = null;
    final Logger logger = Logger.getLogger(SlvInterfaceService.class);
    private List<ConfigurationJson> configurationJsonList = null;

    public SlvInterfaceService() {
        super();
        this.properties = PropertiesReader.getProperties();

    }


    public void test() {
        String formTemplateGuid = properties.getProperty("streetlight.edge.formtemplateguid");
        List<String> noteGuidsList = connectionDAO.getEdgeNoteGuid(formTemplateGuid);
        List<String> noteGuids = slvInterfaceDAO.getNoteGuids();
        for (String edgenoteGuid : noteGuidsList) {
            if (!noteGuids.contains(edgenoteGuid)) {
                SlvSyncDetails slvSyncDetails = new SlvSyncDetails();
                slvSyncDetails.setStatus(Status.Failure.toString());
                slvSyncDetails.setNoteGuid(edgenoteGuid);
                connectionDAO.saveSlvSyncDetails(slvSyncDetails);
            }
        }
    }


    public void start() {
        // Get Configuration JSON
        configurationJsonList = getConfigJson();
        try {
            loadDevices();
        } catch (Exception e) {
            logger.error("Unable to get device from SLV.", e);
            return;
        }

        String accessToken = getEdgeToken();
        logger.info("AccessToken is :" + accessToken);
        if (accessToken == null) {
            logger.error("Edge Invalid UserName and Password.");
            return;
        }
        String dataReSync = PropertiesReader.getProperties().getProperty("streetlight.edge.data.resync");
        if (dataReSync != null && dataReSync.trim().equals("true")) {
            logger.info("ReSync Process Starts.");
            reSync(accessToken);
            logger.info("ReSync Process Ends.");
            System.exit(0);
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
        String controllerStrIdValue = properties.getProperty("streetlight.controller.str.id");

        logger.info("GetNotesUrl :" + url);
        List<String> noteGuidsList = connectionDAO.getEdgeNoteGuid(formTemplateGuid);
        for (String edgenoteGuid : noteGuidsList) {
            try {
                if (!noteGuids.contains(edgenoteGuid)) {
                    String restUrl = url + edgenoteGuid;
                    ResponseEntity<String> responseEntity = slvRestService.getRequest(restUrl, false, accessToken);
                    logger.info("notes response :" + restUrl);
                    //Thread.sleep(10000);
                    if (responseEntity.getStatusCode().is2xxSuccessful()) {
                        String notesData = responseEntity.getBody();
                        logger.info("notes response from server :" + notesData);
                        System.out.println(notesData);

                        List<EdgeNote> edgeNoteList = new ArrayList<>();
                        EdgeNote edgeNote = gson.fromJson(notesData, EdgeNote.class);
                        edgeNoteList.add(edgeNote);

                        for (EdgeNote edgenote : edgeNoteList) {
                            logger.info("ProcessNoteTitle is :" + edgenote.getTitle());
                            String geozoneId = getGeoZoneValue(edgenote.getTitle());
                            processEdgeNote(edgenote, noteGuids, formTemplateGuid, geozoneId, controllerStrIdValue);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error", e);
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
                            logger.info(edgeNote.getTitle() + " is going to Create.");
                            String macAddress = validateMACAddress(configurationJson, formData.getFormDef(), edgeNote, paramsList);
                            slvSyncDetail.setMacAddress(macAddress);
                            slvSyncDetail.setSelectedAction(SLVProcess.NEW_DEVICE.toString());
                            boolean isDeviceExist = isAvailableDevice(edgeNote.getTitle());
                            if (!isDeviceExist) {
                                createDevice(edgeNote, slvSyncDetail, geozoneId);
                            }
                            processSetDevice(edgeFormDataList, configurationJson, edgeNote, paramsList, slvSyncDetail, controllerStrIdValue);
                           // replaceOLC(controllerStrIdValue, edgeNote.getTitle(), slvSyncDetail.getMacAddress());
                            break;
                        case UPDATE_DEVICE:
                            logger.info(edgeNote.getTitle() + " is going to Replace.");
                            slvSyncDetail.setSelectedAction(SLVProcess.UPDATE_DEVICE.toString());
                            processSetDevice(edgeFormDataList, configurationJson, edgeNote, paramsList, slvSyncDetail, controllerStrIdValue);
                            break;
                        case REPLACE_DEVICE:
                            logger.info(edgeNote.getTitle() + " is going to Remove.");
                            slvSyncDetail.setSelectedAction(SLVProcess.REPLACE_DEVICE.toString());
                            processReplaceDevice(formData, configurationJson, edgeNote, paramsList, slvSyncDetail, controllerStrIdValue, geozoneId);
                            break;

                        case REMOVE:
                            replaceOLC(controllerStrIdValue, edgeNote.getTitle(), "");
                            break;

                    }
                }
            }

        } catch (ReplaceOLCFailedException | NoValueException | QRCodeAlreadyUsedException e) {
            slvSyncDetail.setErrorDetails(e.getMessage());
            slvSyncDetail.setStatus(Status.Failure.toString());
        } catch (DeviceUpdationFailedException | DeviceCreationFailedException e) {
            slvSyncDetail.setStatus(Status.Failure.toString());
        } catch (MacAddressProcessedException macException) {
            slvSyncDetail.setErrorDetails(macException.getMessage());
            slvSyncDetail.setStatus(Status.Failure.toString());
        }
        slvSyncDetail.setProcessedDateTime(new Date().getTime());
        connectionDAO.updateSlvDevice(slvSyncDetail.getNoteName(), slvSyncDetail.getMacAddress());
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
        slvDevice.setDeviceName(title);
        slvDevice.setProcessedDateTime(new Date().getTime());
        connectionDAO.saveSlvDevices(slvDevice);
    }

    public void processReplaceDevice(FormData formData, ConfigurationJson configurationJson, EdgeNote edgeNote, List<Object> paramsList, SlvSyncDetails slvSyncDetails, String controllerStrIdValue, String geozoneId) throws NoValueException, QRCodeAlreadyUsedException, ReplaceOLCFailedException, DeviceUpdationFailedException, MacAddressProcessedException {
        List<EdgeFormData> edgeFormDatas = formData.getFormDef();
        String macAddress = validateMACAddress(configurationJson, edgeFormDatas, edgeNote, paramsList);
        slvSyncDetails.setMacAddress(macAddress);

        validateExistingMACAddress(configurationJson, edgeFormDatas, edgeNote, paramsList, slvSyncDetails, geozoneId);
        replaceOLC(controllerStrIdValue, edgeNote.getTitle(), "");
        processSetDevice(edgeFormDatas, configurationJson, edgeNote, paramsList, slvSyncDetails, controllerStrIdValue);
        replaceOLC(controllerStrIdValue, edgeNote.getTitle(), macAddress);

    }



    public void processFixtureScan(List<EdgeFormData> edgeFormDataList, Id fixureID, EdgeNote edgeNote, List<Object> paramsList, SlvSyncDetails slvSyncDetails) throws NoValueException {
        try {
            String fixtureScan = valueById(edgeFormDataList, fixureID.getId());
            if (fixtureScan.startsWith("00") && slvSyncDetails.getMacAddress() != null && !slvSyncDetails.getMacAddress().startsWith("00")) {
                String temp = slvSyncDetails.getMacAddress();
                slvSyncDetails.setMacAddress(fixtureScan);
                fixtureScan = temp;
            }
            // buildFixtureStreetLightData(fixtureScan, paramsList, edgeNote);
            addStreetLightData("luminaire.installdate", dateFormat(edgeNote.getCreatedDateTime()), paramsList);
            addStreetLightData("install.date", dateFormat(edgeNote.getCreatedDateTime()), paramsList);                                                                                                // TODO
            buildFixtureStreetLightData(fixtureScan, paramsList, edgeNote);
        } catch (NoValueException e) {
            if (fixureID.isRequired()) {
                throw new NoValueException(e.getMessage());
            }
        } catch (InValidBarCodeException e) {
            slvSyncDetails.setErrorDetails("Warn: InValide Bar Code " + e.getMessage());
        }


    }


    private void validateExistingMACAddress(ConfigurationJson configurationJson, List<EdgeFormData> edgeFormDataList, EdgeNote edgeNote, List<Object> paramsList, SlvSyncDetails slvSyncDetails, String geozoneId) throws NoValueException {
        List<Id> idList = configurationJson.getIds();
        Id existingMacAddressId = getIDByType(idList, EdgeComponentType.EXISTING_MAC.toString());
        if (existingMacAddressId != null) {
            try {
                String existingMacAddress = valueById(edgeFormDataList, existingMacAddressId.getId());
                String validMacAddressResult = validateMACAddress(existingMacAddress, edgeNote.getTitle(), geozoneId);
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
                if (!actionValue.equals(action.getValue())) {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
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


    private String validateMACAddress(ConfigurationJson configurationJson, List<EdgeFormData> edgeFormDataList, EdgeNote edgeNote, List<Object> paramsList) throws NoValueException, QRCodeAlreadyUsedException, MacAddressProcessedException {
        List<Id> idList = configurationJson.getIds();
        Id macID = getIDByType(idList, EdgeComponentType.MAC.toString());
        if (macID != null) {
            try {
                String newNodeMacAddress = valueById(edgeFormDataList, macID.getId());
                logger.info("newNodeMacAddress:" + newNodeMacAddress);
                SlvDevice slvDevice = connectionDAO.getSlvDevices(edgeNote.getTitle());
                if (slvDevice != null && slvDevice.getMacAddress() != null && slvDevice.getMacAddress().equals(newNodeMacAddress)) {
                    throw new MacAddressProcessedException("Already mac address processed" + edgeNote.getTitle(), newNodeMacAddress);
                }
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

    private void reSync(String accessToken) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader("./data/resynclist.txt"));
            List<SlvSyncDetails> loggingModelList = connectionDAO.getSyncEntityList();
            String noteGuid = null;
            while ((noteGuid = bufferedReader.readLine()) != null) {
                logger.info("Current Note Guid:" + noteGuid);
                SlvSyncDetails loggingModelTemp = new SlvSyncDetails();
                loggingModelTemp.setNoteGuid(noteGuid);

                int pos = loggingModelList.indexOf(loggingModelTemp);
                if (pos != -1) {
                    logger.info("Note is Already Synced. Previous Sync Status" + loggingModelTemp.getStatus());
                    loggingModelTemp = loggingModelList.get(pos);
                    if (loggingModelTemp.getStatus() == null || loggingModelTemp.getStatus().toLowerCase().equals("error")) {
                        connectionDAO.deleteProcessedNotes(loggingModelTemp.getNoteGuid());
                        String utilLocId = getUtilLocationId(loggingModelTemp.getErrorDetails());
                        reSync(noteGuid, accessToken, true, utilLocId);
                    }
                } else {
                    logger.info("Note is not Synced. Syncing now.");
                    reSync(noteGuid, accessToken, false, null);
                }

            }
        } catch (Exception e) {
            logger.error("Error in  reSync.", e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }


    protected String getUtilLocationId(String errorDetails) {
        if (errorDetails != null && errorDetails.contains("Service point is already associated with LocationUtilID")) {
            int startAt = errorDetails.indexOf("LocationUtilID");
            int endAt = errorDetails.indexOf("with type", startAt);
            String utilLocationId = errorDetails.substring(startAt + 17, endAt);
            return utilLocationId.trim();
        }
        return null;
    }

    private void reSync(String noteGuid, String accessToken, boolean isResync, String utilLocId) {
        String formTemplateGuid = properties.getProperty("streetlight.edge.formtemplateguid");
        String controllerStrIdValue = properties.getProperty("streetlight.controller.str.id");
        String geozoneId = properties.getProperty("streetlight.slv.geozoneid");
        List<String> noteGuids = slvInterfaceDAO.getNoteGuids();
        if (noteGuids == null) {
            logger.error("Error while getting already process note list.");
            return;
        }

        // Get Edge Server Url from properties
        String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");

        url = url + PropertiesReader.getProperties().getProperty("streetlight.edge.url.notes.get");

        url = url + "/" + noteGuid;

        // Get NoteList from edgeserver
        ResponseEntity<String> responseEntity = slvRestService.getRequest(url, false, accessToken);

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
