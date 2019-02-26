package com.terragoedge.slvinterface.service;

import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.slvinterface.dao.tables.SlvDevice;
import com.terragoedge.slvinterface.enumeration.EdgeComponentType;
import com.terragoedge.slvinterface.enumeration.Status;
import com.terragoedge.slvinterface.exception.*;
import com.terragoedge.slvinterface.model.*;
import com.terragoedge.slvinterface.utils.PropertiesReader;
import com.terragoedge.slvinterface.utils.ResourceDetails;
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

    public SlvInterfaceService() {
        super();
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
        String dataReSync = PropertiesReader.getProperties().getProperty("streetlight.edge.data.resync");
        if (dataReSync != null && dataReSync.trim().equals("true")) {
            logger.info("ReSync Process Starts.");
            reSync(accessToken);
            logger.info("ReSync Process Ends.");
            System.exit(0);
            return;
        }
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
                        processSingleForm(jpsWorkflowModel,edgeFormDataList,edgeNote, paramsList, configurationJsonList, geozoneId, controllerStrid, isResync);
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


    /**
     * Based on configuration corresponding process going to take place.
     *
     * @param edgeFormDataList
     * @param edgeNote
     * @param slvSyncDetail
     * @param paramsList
     * @param configurationJsonList
     */
    public void processSingleForm(JPSWorkflowModel jpsWorkflowModel,List<EdgeFormData> edgeFormDataList, EdgeNote edgeNote, SlvSyncDetails slvSyncDetail, List<Object> paramsList, List<ConfigurationJson> configurationJsonList, String geozoneId, String controllerStrIdValue, boolean isReSync) {
        try {
            System.out.println("REPLACE_DEVICE");
            logger.info(edgeNote.getTitle() + " is going to Remove.");
            processReplaceDevice(edgeFormDataList, null, edgeNote, paramsList, slvSyncDetail, controllerStrIdValue, geozoneId);
            slvSyncDetail.setStatus("Success");
        } catch (ReplaceOLCFailedException | NoValueException | QRCodeAlreadyUsedException e) {
            slvSyncDetail.setErrorDetails(e.getMessage());
            slvSyncDetail.setStatus(Status.Failure.toString());
        } catch (DeviceUpdationFailedException | DeviceCreationFailedException e) {
            slvSyncDetail.setStatus(Status.Failure.toString());
        } catch (MacAddressProcessedException macException) {
            slvSyncDetail.setErrorDetails(macException.getMessage());
            slvSyncDetail.setStatus(Status.Failure.toString());
        } catch (QRCodeNotMatchedException e) {
            slvSyncDetail.setErrorDetails("Existing QR Code [" + e.getMacAddress() + "] is not matched with SVL.");
            slvSyncDetail.setStatus(Status.Failure.toString());
        }
        slvSyncDetail.setProcessedDateTime(new Date().getTime());
        connectionDAO.updateSlvDevice(slvSyncDetail.getNoteName(), slvSyncDetail.getMacAddress());

    }

    public boolean isAvailableDevice(String idOnController) {
        // SlvDevice slvDevice = connectionDAO.getSlvDevices(idOnController);
        // return slvDevice != null;
        return true;
    }

    public void createDevice(EdgeNote edgeNote, SlvSyncDetails slvSyncDetails, String geoZoneId, List<EdgeFormData> edgeFormDataList) throws DeviceCreationFailedException {
        if (geoZoneId != null) {
            System.out.println("Device created method called");
            ResponseEntity<String> responseEntity = createDevice(edgeNote, geoZoneId);
            String status = responseEntity.getStatusCode().toString();
            String responseBody = responseEntity.getBody();
            if ((status.equalsIgnoreCase("200") || status.equalsIgnoreCase("ok"))
                    && !responseBody.contains("<status>ERROR</status>")) {
                logger.info("Device Created Successfully, NoteId:" + edgeNote.getNoteGuid() + "-"
                        + edgeNote.getTitle());
                slvSyncDetails.setDeviceCreationStatus(Status.Success.toString());
                System.out.println("Device created :" + edgeNote.getTitle());
                createSLVDevice(edgeNote.getTitle());
            } else {
                System.out.println("Device created Failed:" + edgeNote.getTitle());
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

    //streetlight.controller.str.id
    //
    public JPSWorkflowModel processWorkFlowForm(List<FormData> formDataList) {
        JPSWorkflowModel jpsWorkflowModel = new JPSWorkflowModel();
        String categoryStrId = properties.getProperty("streetlight.categorystr.id");
        String controllerStrId = properties.getProperty("streetlight.controller.str.id");
        String geozoneId = properties.getProperty("streetlight.slv.geozoneid");
        jpsWorkflowModel.setGeozoneId(geozoneId);
        jpsWorkflowModel.setControllerStrId(controllerStrId);
        jpsWorkflowModel.setProvider_name("JPS");
        jpsWorkflowModel.setGeozonePath("Jamaica/Parish/Division/Street Name");
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


    public void processReplaceDevice(List<EdgeFormData> edgeFormDatas, EdgeNote edgeNote, List<Object> paramsList, SlvSyncDetails slvSyncDetails, String controllerStrIdValue, String geozoneId) throws NoValueException, QRCodeAlreadyUsedException, ReplaceOLCFailedException, DeviceUpdationFailedException, MacAddressProcessedException, QRCodeNotMatchedException {
        String macAddress = validateMACAddress(configurationJson, edgeFormDatas, edgeNote, paramsList);
        slvSyncDetails.setMacAddress(macAddress);
        System.out.println("newMac :" + macAddress);
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


    private void validateExistingMACAddress(ConfigurationJson configurationJson, List<EdgeFormData> edgeFormDataList, EdgeNote edgeNote, List<Object> paramsList, SlvSyncDetails slvSyncDetails, String geozoneId) throws NoValueException, QRCodeNotMatchedException {
        List<Id> idList = configurationJson.getIds();
        Id existingMacAddressId = getIDByType(idList, EdgeComponentType.EXISTING_MAC.toString());
        if (existingMacAddressId != null) {
            try {
                String existingMacAddress = valueById(edgeFormDataList, existingMacAddressId.getId());
                System.out.println("Existingmac :" + existingMacAddress);
                String validMacAddressResult = validateMACAddress(existingMacAddress, edgeNote.getTitle(), geozoneId);
                validMacAddressResult = validMacAddressResult + " replaced on " + dateFormat(edgeNote.getCreatedDateTime());
                System.out.println("validMacAddressResult :" + validMacAddressResult);
                addStreetLightData("comment", validMacAddressResult, paramsList);
            } catch (NoValueException e) {
                if (existingMacAddressId.isRequired()) {
                    throw new NoValueException(e.getMessage());
                }
            }
        }
    }


    public boolean checkActionType(List<EdgeFormData> edgeFormData, List<Action> actionList) {
        String actionValue = null;
        for (Action action : actionList) {
            try {
                actionValue = valueById(edgeFormData, action.getId());
                System.out.println("Action value :" + actionValue);
                if (!actionValue.equals(action.getValue())) {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        System.out.println("return true");
        return true;
    }


    protected String valueById(List<EdgeFormData> edgeFormDatas, int id) throws NoValueException {
        EdgeFormData edgeFormTemp = new EdgeFormData();
        edgeFormTemp.setId(id);

        int pos = edgeFormDatas.indexOf(edgeFormTemp);
        if (pos != -1) {
            EdgeFormData edgeFormData = edgeFormDatas.get(pos);
            String value = edgeFormData.getValue();
            logger.info("edgeFormData value:" + value);
            if (value == null || value.trim().isEmpty()) {
                return "";
                //throw new NoValueException("Value is Empty or null." + value);
            }
            return value;
        } else {
            throw new NoValueException(id + " is not found.");
        }
    }

    public List<ConfigurationJson> getConfigJson() throws Exception {
        JsonParser jsonParser = new JsonParser();
        FileReader reader = new FileReader(ResourceDetails.CONFIG_JSON_PATH);
        String configjson = jsonParser.parse(reader).toString();
        Type listType = new TypeToken<ArrayList<ConfigurationJson>>() {
        }.getType();
        List<ConfigurationJson> configurationJsons = gson.fromJson(configjson, listType);
        return configurationJsons;


    }


    private String validateMACAddress(ConfigurationJson configurationJson, List<EdgeFormData> edgeFormDataList, EdgeNote edgeNote, List<Object> paramsList) throws NoValueException, QRCodeAlreadyUsedException, MacAddressProcessedException {
        List<Id> idList = configurationJson.getIds();
        Id macID = getIDByType(idList, EdgeComponentType.MAC.toString());
        if (macID != null) {
            try {
                String newNodeMacAddress = valueById(edgeFormDataList, macID.getId());
                if (newNodeMacAddress == null || newNodeMacAddress.isEmpty()) {
                    System.out.println("process validation method interchange mac as fixture");
                    Id fixureID = getIDByType(idList, EdgeComponentType.FIXTURE.toString());
                    if (fixureID != null) {
                        String fixture = valueById(edgeFormDataList, fixureID.getId());
                        if (fixture != null && fixture.startsWith("00")) {
                            newNodeMacAddress = fixture;
                            System.out.println("Given fixture mac as fixture : " + newNodeMacAddress);
                        }
                    }


                }
                logger.info("newNodeMacAddress:" + newNodeMacAddress);
                if (newNodeMacAddress.contains("null") || newNodeMacAddress.equals("null")) {
                    throw new MacAddressProcessedException("InValid MAC address." + edgeNote.getTitle(), newNodeMacAddress);
                }
                SlvDevice slvDevice = connectionDAO.getSlvDevices(edgeNote.getTitle());
                if (slvDevice != null && slvDevice.getMacAddress() != null && slvDevice.getMacAddress().toLowerCase().equals(newNodeMacAddress.toLowerCase())) {
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
                // geozoneId = getGeoZoneValue(edgenote.getTitle());
                geozoneId = null;
                System.out.print("geozoneId :" + geozoneId);
                processEdgeNote(edgenote, noteGuids, formTemplateGuid, geozoneId, controllerStrIdValue, isResync);
            }
        }
    }


    private boolean nullCheck(String data) {
        return (data != null && data.length() > 0 && !data.contains("null")) ? true : false;
    }


}
