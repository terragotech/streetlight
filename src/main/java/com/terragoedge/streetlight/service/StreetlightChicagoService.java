package com.terragoedge.streetlight.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.terragoedge.streetlight.edgeinterface.SlvData;
import com.terragoedge.streetlight.edgeinterface.SlvToEdgeService;
import com.terragoedge.streetlight.json.model.ContextList;
import com.terragoedge.streetlight.logging.InstallMaintenanceLogModel;
import com.terragoedge.streetlight.logging.LoggingModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.edgeserver.DeviceMacAddress;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.edgeserver.FormData;
import com.terragoedge.edgeserver.Value;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.dao.StreetlightDao;
import com.terragoedge.streetlight.exception.DeviceUpdationFailedException;
import com.terragoedge.streetlight.exception.InValidBarCodeException;
import com.terragoedge.streetlight.exception.NoValueException;
import com.terragoedge.streetlight.exception.QRCodeAlreadyUsedException;
import com.terragoedge.streetlight.exception.QRCodeNotMatchedException;
import com.terragoedge.streetlight.exception.ReplaceOLCFailedException;

public class StreetlightChicagoService extends AbstractProcessor {


    final Logger logger = Logger.getLogger(StreetlightChicagoService.class);
    InstallationMaintenanceProcessor installationMaintenanceProcessor;
    SlvToEdgeService slvToEdgeService = null;

    public StreetlightChicagoService() {
        super();
        loadContextList();
        installationMaintenanceProcessor = new InstallationMaintenanceProcessor(contextListHashMap);
        slvToEdgeService = new SlvToEdgeService();
    }


    protected void loadContextList() {
        String mainUrl = properties.getProperty("streetlight.url.main");
        String dataUrl = properties.getProperty("streetlight.url.get.fxiturecode");
        String url = mainUrl + dataUrl;

        ResponseEntity<String> response = restService.getContextPostRequest(url, null);
        String responseString = response.getBody();
        if (responseString != null) {
            ContextList contextList = gson.fromJson(responseString, ContextList.class);
            int idOnControllerPos = -1;
            int fixtureCodePos = -1;
            int pos = 0;
            for (String columnName : contextList.getColumns()) {
                switch (columnName) {
                    case "idOnController":
                        idOnControllerPos = pos;
                        break;
                    case "location.proposedcontext":
                        fixtureCodePos = pos;
                        break;
                }
                pos = pos + 1;
            }

            for (List<String> values : contextList.getValues()) {
                contextListHashMap.put(values.get(idOnControllerPos), values.get(fixtureCodePos));
            }
        }

    }

    //703079877   77jmb3
    private void reSync(String accessToken) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader("./data/resynclist.txt"));
            List<LoggingModel> loggingModelList = streetlightDao.getSyncStatus();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                logger.info("Current Note Guid:" + line);
                LoggingModel loggingModelTemp = new LoggingModel();
                loggingModelTemp.setProcessedNoteId(line);

                int pos = loggingModelList.indexOf(loggingModelTemp);
                if (pos != -1) {
                    logger.info("Note is Already Synced. Previous Sync Status" + loggingModelTemp.getStatus());
                    loggingModelTemp = loggingModelList.get(pos);
                    if (loggingModelTemp.getStatus() == null || loggingModelTemp.getStatus().toLowerCase().equals("error")) {
                        streetlightDao.deleteProcessedNotes(loggingModelTemp.getProcessedNoteId());
                        String utilLocId = getUtilLocationId(loggingModelTemp.getErrorDetails());
                        reSync(line, accessToken, true, utilLocId);
                    }
                } else {
                    logger.info("Note is not Synced. Syncing now.");
                    reSync(line, accessToken, false, null);
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


    private void reSync(String noteGuid, String accessToken, boolean isResync, String utilLocId) {
        // Get Edge Server Url from properties
        String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");

        url = url + PropertiesReader.getProperties().getProperty("streetlight.edge.url.notes.get");

        url = url + "/" + noteGuid;

        // Get NoteList from edgeserver
        ResponseEntity<String> responseEntity = restService.getRequest(url, false, accessToken);

        // Process only response code as success
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            // Get Response String
            String notesData = responseEntity.getBody();

            EdgeNote edgeNote = gson.fromJson(notesData, EdgeNote.class);
         //   if(!edgeNote.getCreatedBy().contains("admin")){
                InstallMaintenanceLogModel installMaintenanceLogModel = new InstallMaintenanceLogModel();
                installMaintenanceLogModel.setLastSyncTime(edgeNote.getSyncTime());
                installMaintenanceLogModel.setProcessedNoteId(edgeNote.getNoteGuid());
                installMaintenanceLogModel.setNoteName(edgeNote.getTitle());
                installMaintenanceLogModel.setCreatedDatetime(String.valueOf(edgeNote.getCreatedDateTime()));
                loadDefaultVal(edgeNote, installMaintenanceLogModel);

                installationMaintenanceProcessor.processNewAction(edgeNote, installMaintenanceLogModel, isResync, utilLocId);
                updateSlvStatusToEdge(installMaintenanceLogModel,edgeNote);
                LoggingModel loggingModel = installMaintenanceLogModel;
                streetlightDao.insertProcessedNotes(loggingModel, installMaintenanceLogModel);
          //  }
        }


    }


    private void updateSlvStatusToEdge(InstallMaintenanceLogModel installMaintenanceLogModel,EdgeNote edgeNote){
        try {
            SlvData slvData = new SlvData();
            slvData.setNoteGuid(edgeNote.getNoteGuid());
            slvData.setNoteTitle(edgeNote.getTitle());
            slvData.setProcessedTime(String.valueOf(System.currentTimeMillis()));
            slvData.setSyncToSlvStatus(installMaintenanceLogModel.getStatus());
            slvData.setErrorDetails(installMaintenanceLogModel.getErrorDetails());
            slvData.setInstalledDate(installMaintenanceLogModel.getInstalledDate());
            slvData.setReplacedDate(installMaintenanceLogModel.getReplacedDate());
            slvToEdgeService.run(slvData);
        }catch (Exception e){
            logger.error("Error in updateSlvStatusToEdge");
        }

    }


    public void run() {
        // Get Already synced noteguids from Database
        List<String> noteGuids = streetlightDao.getNoteIds();
        String accessToken = getEdgeToken();
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


        if( contextListHashMap.isEmpty()){
            logger.error("Proposed context data are not loaded.");
            return;
        }


        // Get Edge Server Url from properties
        String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");

        url = url + PropertiesReader.getProperties().getProperty("streetlight.edge.url.notes.get");

        String systemDate = PropertiesReader.getProperties().getProperty("streetlight.edge.customdate");
        long lastSynctime = 0L;
        lastSynctime = streetlightDao.getLastSyncTime();
        if (systemDate == null || systemDate.equals("false")) {
            String yesterday = getYesterdayDate();
            //  url = url + "modifiedAfter=" + yesterday;
            if(lastSynctime == -1){
                lastSynctime = System.currentTimeMillis() - (3600000 * 2);
            }
            url = url + "lastSyncTime=" + lastSynctime;

        }

        // Get NoteList from edgeserver
        ResponseEntity<String> responseEntity = restService.getRequest(url, false, accessToken);

        // Process only response code as success
        if (responseEntity.getStatusCode().is2xxSuccessful()) {

            // Get Response String
            String notesData = responseEntity.getBody();
            System.out.println(notesData);

            // Convert notes Json to List of notes object
            Type listType = new TypeToken<ArrayList<EdgeNote>>() {
            }.getType();
            List<EdgeNote> edgeNoteList = gson.fromJson(notesData, listType);

            // Iterate each note
            for (EdgeNote edgeNote : edgeNoteList) {
                try {
                    if (!noteGuids.contains(edgeNote.getNoteGuid())) {
                        if(!edgeNote.getCreatedBy().contains("admin")){
                            InstallMaintenanceLogModel installMaintenanceLogModel = new InstallMaintenanceLogModel();

                            installMaintenanceLogModel.setProcessedNoteId(edgeNote.getNoteGuid());
                            installMaintenanceLogModel.setNoteName(edgeNote.getTitle());
                            installMaintenanceLogModel.setLastSyncTime(edgeNote.getSyncTime());
                            installMaintenanceLogModel.setCreatedDatetime(String.valueOf(edgeNote.getCreatedDateTime()));
                            loadDefaultVal(edgeNote, installMaintenanceLogModel);
                            installationMaintenanceProcessor.processNewAction(edgeNote, installMaintenanceLogModel, false, null);
                            updateSlvStatusToEdge(installMaintenanceLogModel,edgeNote);
                            LoggingModel loggingModel = installMaintenanceLogModel;
                            streetlightDao.insertProcessedNotes(loggingModel, installMaintenanceLogModel);
                        }

                    }

                } catch (Exception e) {
                    logger.error("Error while processing edge note. NoteGuid :" + edgeNote.getNoteGuid(), e);
                }


            }
        } else {
            logger.error("Unable to get message from EdgeServer. Response Code is :" + responseEntity.getStatusCode());
        }
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

    private void syncData(EdgeNote edgeNote, List<String> noteGuids, LoggingModel loggingModel, boolean isResync, String utilLocId) {
        try {
            // Check current note is already synced with slv or not.
            if (!noteGuids.contains(edgeNote.getNoteGuid())) {
                // Get Form List
                List<FormData> formDatasList = edgeNote.getFormData();
                Map<String, FormData> formDataMaps = new HashMap<>();
                for (FormData formData : formDatasList) {
                    formDataMaps.put(formData.getFormTemplateGuid(), formData);
                }
                loggingModel.setProcessedNoteId(edgeNote.getNoteGuid());
                loggingModel.setNoteName(edgeNote.getTitle());
                loggingModel.setCreatedDatetime(String.valueOf(edgeNote.getCreatedDateTime()));
                syncData(formDataMaps, edgeNote, noteGuids, loggingModel, isResync, utilLocId);

            } else {
                // Logging this note is already synced with SLV.
                logger.info("Note " + edgeNote.getTitle() + " is already synced with SLV.");
                loggingModel.setNoteAlreadySynced(true);
            }
        } catch (QRCodeAlreadyUsedException e1) {
            logger.error("MacAddress (" + e1.getMacAddress()
                    + ")  - Already in use. So this pole is not synced with SLV. Note Title :[" + edgeNote.getTitle()
                    + " ]");
            loggingModel.setStatus(MessageConstants.ERROR);
            loggingModel.setErrorDetails("MacAddress (" + e1.getMacAddress() + ")  - Already in use");
        } catch (ReplaceOLCFailedException e) {
            logger.error("Error in syncData", e);
            loggingModel.setStatus(MessageConstants.ERROR);
            loggingModel.setErrorDetails(e.getMessage());
        } catch (DeviceUpdationFailedException e) {
            logger.error("Error in syncData", e);
            loggingModel.setStatus(MessageConstants.ERROR);
            loggingModel.setErrorDetails(e.getMessage());
        } catch (InValidBarCodeException e) {
            logger.error("Error in syncData", e);
            loggingModel.setStatus(MessageConstants.ERROR);
            loggingModel.setErrorDetails(e.getMessage());
        } catch (NoValueException e) {
            logger.error("Error in syncData", e);
            loggingModel.setStatus(MessageConstants.ERROR);
            loggingModel.setErrorDetails(e.getMessage());
        } catch (Exception e) {
            logger.error("Error in syncData", e);
            loggingModel.setStatus(MessageConstants.ERROR);
            loggingModel.setErrorDetails(e.getMessage());
        }
    }


    public void syncData(Map<String, FormData> formDatas, EdgeNote edgeNote, List<String> noteGuids,
                         LoggingModel loggingModel, boolean isResync, String utilLocId) throws InValidBarCodeException, DeviceUpdationFailedException,
            QRCodeAlreadyUsedException, NoValueException, ReplaceOLCFailedException, Exception {
        List<Object> paramsList = new ArrayList<Object>();
        String chicagoFormTemplateGuid = properties.getProperty("streetlight.edge.formtemplateguid.chicago");
        String replaceOlcFormTemplateGuid = properties.getProperty("streetlight.edge.formtemplateguid.replacenode");

        FormData replaceOLCFormData = formDatas.get(replaceOlcFormTemplateGuid);
        if (replaceOLCFormData == null) {
            replaceOLCFormData = formDatas.get("606fb4ca-40a4-466b-ac00-7c0434f82bfa");
        }

        if (replaceOLCFormData != null) {
            loggingModel.setIsQuickNote(true);
        }

        String idOnController = loggingModel.getIdOnController();
        String controllerStrId = loggingModel.getControllerSrtId();


        if (replaceOLCFormData != null) {
            processReplaceOLCFormVal(replaceOLCFormData, idOnController, controllerStrId, paramsList,
                    edgeNote.getNoteGuid(), edgeNote.getCreatedDateTime(), noteGuids, edgeNote, loggingModel);
        } else {

            String[] chicagoFormTemplateGuids = chicagoFormTemplateGuid.split(",");
            for (String chicagoFormTemplateGuidTemp : chicagoFormTemplateGuids) {
                FormData chicagoFromData = formDatas.get(chicagoFormTemplateGuidTemp);
                if (chicagoFromData == null) {
                    loggingModel.setErrorDetails(MessageConstants.CHICAGO_FORM_NOT_AVAILABLE);
                    loggingModel.setStatus(MessageConstants.ERROR);
                    logger.error("No Chicago FormTemplate is not Present. So note is not processed. Note Title is :"
                            + edgeNote.getTitle());
                    continue;
                }


                if (isResync) {
                    try {
                        replaceOLC(controllerStrId, idOnController, "");
                    } catch (ReplaceOLCFailedException e) {
                        e.printStackTrace();
                    }

                }
                // Get Fixture Code
                String macAddress = null;
                // Process Chicago Form data
                List<EdgeFormData> chicagoFromDef = chicagoFromData.getFormDef();
                logger.info("Current From Name" + chicagoFromData.getName());
                logger.info("From Data" + gson.toJson(chicagoFromDef));
                for (EdgeFormData edgeFormData : chicagoFromDef) {
                    if (edgeFormData.getLabel() == null) {
                        continue;
                    }
                    if (edgeFormData.getLabel()
                            .equals(properties.getProperty("edge.fortemplate.chicago.label.fixture.macaddress"))) {
                        if (edgeFormData.getValue() == null || edgeFormData.getValue().trim().isEmpty()) {
                            // logger.info("Fixture MAC address is empty. So note is not processed. Note
                            // Title :"+edgeNote.getTitle());
                            // return; -- TODO Need to skip or not later decide
                        } else {
                            addStreetLightData("luminaire.installdate", dateFormat(edgeNote.getCreatedDateTime()),
                                    paramsList); // -- TODO
                            buildFixtureStreetLightData(edgeFormData.getValue(), paramsList, edgeNote);
                        }

                    } else if (edgeFormData.getLabel()
                            .equals(properties.getProperty("edge.fortemplate.chicago.label.node.macaddress"))) {
                        logger.info("MAC Address Value:" + edgeFormData.getValue());
                        if (edgeFormData.getValue() == null || edgeFormData.getValue().trim().isEmpty()) {
                            logger.info("Node MAC address is empty. So note is not processed. Note Title :"
                                    + edgeNote.getTitle());
                            loggingModel.setErrorDetails(MessageConstants.NODE_MAC_ADDRESS_NOT_AVAILABLE);
                            loggingModel.setStatus(MessageConstants.ERROR);
                            continue;
                        }
                        paramsList.add("idOnController=" + idOnController);
                        paramsList.add("controllerStrId=" + controllerStrId);
                        macAddress = loadMACAddress(edgeFormData.getValue(), paramsList, idOnController);
                    }
                }
                if (macAddress != null) {
                    loggingModel.setMacAddress(macAddress);

                  //  addOtherParams(edgeNote, paramsList, idOnController, utilLocId, true);


                    // DimmingGroupName
                    sync2Slv(paramsList, edgeNote.getNoteGuid(), idOnController, macAddress, controllerStrId, edgeNote,
                            loggingModel);
                    noteGuids.add(edgeNote.getNoteGuid());
                }

            }

        }

    }


    private void processReplaceOLCFormVal(FormData replaceOLCFormData, String idOnController,
                                          String controllerStrIdValue, List<Object> paramsList, String noteGuid,
                                          long noteCreatedDateTime, List<String> noteGuids, EdgeNote edgeNote, LoggingModel loggingModel)
            throws QRCodeAlreadyUsedException, DeviceUpdationFailedException, Exception {
        List<EdgeFormData> replaceOLCFromDef = replaceOLCFormData.getFormDef();
        String existingNodeMacAddress = null;
        String newNodeMacAddress = null;
        // Get Existing Node MAC Address value
        try {
            existingNodeMacAddress = value(replaceOLCFromDef,
                    properties.getProperty("streetlight.edge.replacenode.label.existing"));
            loggingModel.setExistingNodeMACaddress(existingNodeMacAddress);
        } catch (NoValueException e) {
            e.printStackTrace();
            loggingModel.setErrorDetails(MessageConstants.OLD_MAC_ADDRESS_NOT_AVAILABLE);
            loggingModel.setStatus(MessageConstants.ERROR);
            return;
        }
        // Get New Node MAC Address value
        try {
            newNodeMacAddress = value(replaceOLCFromDef,
                    properties.getProperty("streetlight.edge.replacenode.label.newnode"));
            loggingModel.setNewNodeMACaddress(newNodeMacAddress);
        } catch (NoValueException e) {
            e.printStackTrace();
            loggingModel.setErrorDetails(MessageConstants.NEW_MAC_ADDRESS_NOT_AVAILABLE);
            loggingModel.setStatus(MessageConstants.ERROR);
            return;
        }
        String comment = "";
        // Check existingNodeMacAddress is valid or not
        try {
            comment = validateMacAddress(existingNodeMacAddress, idOnController, controllerStrIdValue);
            // comment = validateMACAddress(existingNodeMacAddress, idOnController,
            // geoZoneId);
        } catch (QRCodeNotMatchedException e1) {
            loggingModel.setErrorDetails(MessageConstants.REPLACE_MAC_NOT_MATCH);
            loggingModel.setStatus(MessageConstants.ERROR);
            return;
        }

        checkMacAddressExists(newNodeMacAddress, idOnController, null, null);

        boolean isError = false;
        StringBuffer statusDescription = new StringBuffer();
        // Call Empty ReplaceOLC
        try {
            replaceOLC(controllerStrIdValue, idOnController, "");
            statusDescription.append(MessageConstants.EMPTY_REPLACE_OLC_SUCCESS);
        } catch (ReplaceOLCFailedException e) {
            statusDescription.append(e.getMessage());
            isError = true;
            e.printStackTrace();

        }
        // update device with new mac address
        addStreetLightData("MacAddress", newNodeMacAddress, paramsList);
        comment = comment + " replaced on " + dateFormat(noteCreatedDateTime);
        addStreetLightData("comment", comment, paramsList);

        try {
            String fixtureQRScan = value(replaceOLCFromDef, "Fixture QR Scan");
            addStreetLightData("luminaire.installdate", dateFormat(edgeNote.getCreatedDateTime()), paramsList); // --
            addStreetLightData("install.date", dateFormat(edgeNote.getCreatedDateTime()), paramsList);                                                                                                // TODO
            buildFixtureStreetLightData(fixtureQRScan, paramsList, edgeNote);
        } catch (NoValueException e) {
            e.printStackTrace();
        } catch (InValidBarCodeException e) {
            e.printStackTrace();
        }

        int errorCode = setDeviceValues(paramsList);
        if (errorCode != 0) {
            statusDescription.append(MessageConstants.ERROR_UPDATE_DEVICE_VAL + errorCode);
            loggingModel.setErrorDetails(statusDescription.toString());
            loggingModel.setStatus(MessageConstants.ERROR);
            throw new DeviceUpdationFailedException(errorCode + "");
        } else {
            statusDescription.append(MessageConstants.SET_DEVICE_SUCCESS);
            try {
                // Call New Node MAC Address
                replaceOLC(controllerStrIdValue, idOnController, newNodeMacAddress);
                statusDescription.append(MessageConstants.NEW_REPLACE_OLC_SUCCESS);

                // Need to add new changes.

            } catch (Exception e) {
                isError = true;
                statusDescription.append(e.getMessage());
                e.printStackTrace();
            }
            if (isError) {
                loggingModel.setErrorDetails(statusDescription.toString());
                loggingModel.setStatus(MessageConstants.ERROR);
            } else {
                loggingModel.setStatus(MessageConstants.SUCCESS);
            }
            noteGuids.add(noteGuid);
        }

    }


    public void sync2Slv(List<Object> paramsList, String noteGuid, String idOnController, String macAddress,
                         String controllerStrIdValue, EdgeNote edgeNote, LoggingModel loggingModel)
            throws DeviceUpdationFailedException, ReplaceOLCFailedException {
        int errorCode = setDeviceValues(paramsList);
        // As per doc, errorcode is 0 for success. Otherwise, its not
        // success.
        if (errorCode != 0) {
            loggingModel.setErrorDetails(MessageConstants.ERROR_UPDATE_DEVICE_VAL);
            loggingModel.setStatus(MessageConstants.ERROR);

            throw new DeviceUpdationFailedException(errorCode + "");
        } else {
            // replace OlC
            replaceOLC(controllerStrIdValue, idOnController, macAddress);
            loggingModel.setStatus(MessageConstants.SUCCESS);

        }
    }


    private String loadMACAddress(String data, List<Object> paramsList, String idOnController)
            throws InValidBarCodeException, QRCodeAlreadyUsedException, Exception {
        if (data.contains("MACid")) {
            String[] nodeInfo = data.split(",");
            if (nodeInfo.length > 0) {
                for (String nodeData : nodeInfo) {
                    if (nodeData.startsWith("MACid")) {
                        String macAddress = nodeData.substring(6);
                        checkMacAddressExists(macAddress, idOnController, null, null);
                        addStreetLightData("MacAddress", macAddress, paramsList);
                        return macAddress;
                    }
                }
            }
        } else {
            checkMacAddressExists(data, idOnController, null, null);
            addStreetLightData("MacAddress", data, paramsList);
            return data;
        }

        throw new InValidBarCodeException("Node MAC address is not valid. Value is:" + data);
    }

    // http://192.168.1.9:8080/edgeServer/oauth/token?grant_type=password&username=admin&password=admin&client_id=edgerestapp

    private String getEdgeToken() {
        String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");
        String userName = properties.getProperty("streetlight.edge.username");
        String password = properties.getProperty("streetlight.edge.password");
        url = url + "/oauth/token?grant_type=password&username=" + userName + "&password=" + password
                + "&client_id=edgerestapp";
        ResponseEntity<String> responseEntity = restService.getRequest(url);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            JsonObject jsonObject = (JsonObject) jsonParser.parse(responseEntity.getBody());
            return jsonObject.get("access_token").getAsString();
        }
        return null;

    }

}
