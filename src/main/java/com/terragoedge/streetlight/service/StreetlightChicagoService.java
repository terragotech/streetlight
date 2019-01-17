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
        System.out.println("Object Created.");
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
                    loggingModelTemp = loggingModelList.get(pos);
                    logger.info("Note is Already Synced. Previous Sync Status" + loggingModelTemp.getStatus());
                    if (loggingModelTemp.getStatus() == null || loggingModelTemp.getStatus().toLowerCase().equals("error") || loggingModelTemp.getStatus().toLowerCase().equals("failure")) {
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
        logger.info("resync method called ");
        // Get Edge Server Url from properties
        String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");

        url = url + PropertiesReader.getProperties().getProperty("streetlight.edge.url.notes.get");

        url = url + "/" + noteGuid;
        logger.info("Given url is :" + url);
        // Get NoteList from edgeserver
        ResponseEntity<String> responseEntity = restService.getRequest(url, false, accessToken);

        // Process only response code as success
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            // Get Response String
            String notesData = responseEntity.getBody();
            logger.info("rest service data:" + notesData);
            EdgeNote edgeNote = gson.fromJson(notesData, EdgeNote.class);
            //   if(!edgeNote.getCreatedBy().contains("admin")){
            InstallMaintenanceLogModel installMaintenanceLogModel = new InstallMaintenanceLogModel();
            installMaintenanceLogModel.setLastSyncTime(edgeNote.getSyncTime());
            installMaintenanceLogModel.setProcessedNoteId(edgeNote.getNoteGuid());
            installMaintenanceLogModel.setNoteName(edgeNote.getTitle());
            installMaintenanceLogModel.setCreatedDatetime(String.valueOf(edgeNote.getCreatedDateTime()));
            loadDefaultVal(edgeNote, installMaintenanceLogModel);
            logger.info("going to call processnew action");
            installationMaintenanceProcessor.processNewAction(edgeNote, installMaintenanceLogModel, isResync, utilLocId);
            updateSlvStatusToEdge(installMaintenanceLogModel, edgeNote);
            LoggingModel loggingModel = installMaintenanceLogModel;
            streetlightDao.insertProcessedNotes(loggingModel, installMaintenanceLogModel);
            //  }
        }


    }


    private void updateSlvStatusToEdge(InstallMaintenanceLogModel installMaintenanceLogModel, EdgeNote edgeNote) {
        try {
            SlvData slvData = new SlvData();
            slvData.setNoteGuid(edgeNote.getNoteGuid());
            slvData.setNoteTitle(edgeNote.getTitle());
            slvData.setProcessedTime(String.valueOf(System.currentTimeMillis()));
            slvData.setSyncToSlvStatus(installMaintenanceLogModel.getStatus());
            slvData.setErrorDetails(installMaintenanceLogModel.getErrorDetails());
            slvData.setInstalledDate(installMaintenanceLogModel.getInstalledDate());
            slvData.setReplacedDate(installMaintenanceLogModel.getReplacedDate());
            slvData.setFixtureOnly(installMaintenanceLogModel.isFixtureOnly());
            slvToEdgeService.run(slvData);
        } catch (Exception e) {
            logger.error("Error in updateSlvStatusToEdge", e);
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


        if (contextListHashMap.isEmpty()) {
            logger.error("Proposed context data are not loaded.");
            return;
        }

        String edgeSlvUrl = "https://amerescousa.terragoedge.com/noteguids";

        long lastSynctime = 0L;
        lastSynctime = streetlightDao.getLastSyncTime();




        // Get NoteList from edgeserver
        ResponseEntity<String> edgeSlvServerResponse = restService.getRequest(edgeSlvUrl, false, accessToken);

        // Process only response code as success
        if (edgeSlvServerResponse.getStatusCode().is2xxSuccessful()) {

            // Get Response String
            String notesGuids = edgeSlvServerResponse.getBody();
            System.out.println(notesGuids);

           JsonArray noteGuidsJsonArray = (JsonArray)jsonParser.parse(notesGuids);

           if(noteGuidsJsonArray != null &&  !noteGuidsJsonArray.isJsonNull()){
               for(JsonElement noteGuidJson : noteGuidsJsonArray){
                   String noteGuid = noteGuidJson.getAsString();
                   if(!noteGuids.contains(noteGuid)){
                       String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");

                       url = url + PropertiesReader.getProperties().getProperty("streetlight.edge.url.notes.get");

                       url = url + "/" +noteGuid;
                               logger.info("Given url is :" + url);

                       // Get NoteList from edgeserver
                       ResponseEntity<String> responseEntity = restService.getRequest(url, false, accessToken);

                       // Process only response code as success
                       if (responseEntity.getStatusCode().is2xxSuccessful()) {
                           try{
                               String notesData = responseEntity.getBody();
                               logger.info("rest service data:" + notesData);
                               EdgeNote edgeNote = gson.fromJson(notesData, EdgeNote.class);
                               if (!edgeNote.getCreatedBy().contains("admin") && !edgeNote.getCreatedBy().contains("slvinterface")) {
                                   InstallMaintenanceLogModel installMaintenanceLogModel = new InstallMaintenanceLogModel();

                                   installMaintenanceLogModel.setProcessedNoteId(edgeNote.getNoteGuid());
                                   installMaintenanceLogModel.setNoteName(edgeNote.getTitle());
                                   installMaintenanceLogModel.setLastSyncTime(edgeNote.getSyncTime());
                                   installMaintenanceLogModel.setCreatedDatetime(String.valueOf(edgeNote.getCreatedDateTime()));
                                   loadDefaultVal(edgeNote, installMaintenanceLogModel);
                                   installationMaintenanceProcessor.processNewAction(edgeNote, installMaintenanceLogModel, false, null);
                                   updateSlvStatusToEdge(installMaintenanceLogModel, edgeNote);
                                   LoggingModel loggingModel = installMaintenanceLogModel;
                                   streetlightDao.insertProcessedNotes(loggingModel, installMaintenanceLogModel);
                               }
                           }catch (Exception e){

                           }
                           // Get Response String

                       }
                   }

               }
           }


        } else {
            logger.error("Unable to get message from EdgeServer. Response Code is :" + edgeSlvServerResponse.getStatusCode());
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
