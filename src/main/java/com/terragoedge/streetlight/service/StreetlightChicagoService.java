package com.terragoedge.streetlight.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;

import com.terragoedge.streetlight.edgeinterface.SlvData;
import com.terragoedge.streetlight.edgeinterface.SlvToEdgeService;
import com.terragoedge.streetlight.json.model.ContextList;
import com.terragoedge.streetlight.json.model.CslpDate;
import com.terragoedge.streetlight.json.model.SLVTransactionLogs;
import com.terragoedge.streetlight.json.model.SlvInterfaceLogEntity;
import com.terragoedge.streetlight.logging.InstallMaintenanceLogModel;
import com.terragoedge.streetlight.logging.LoggingModel;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.streetlight.PropertiesReader;

public class StreetlightChicagoService extends AbstractProcessor {


    final Logger logger = Logger.getLogger(StreetlightChicagoService.class);
    InstallationMaintenanceProcessor installationMaintenanceProcessor;
    SlvToEdgeService slvToEdgeService = null;

    public StreetlightChicagoService() {
        super();
       // loadContextList();
        installationMaintenanceProcessor = new InstallationMaintenanceProcessor(contextListHashMap,cslpDateHashMap);
        slvToEdgeService = new SlvToEdgeService();
        System.out.println("Object Created.");
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
                        installationMaintenanceProcessor.reSync(line, accessToken, true, utilLocId,false);
                    }
                } else {
                    logger.info("Note is not Synced. Syncing now.");
                    installationMaintenanceProcessor.reSync(line, accessToken, false, null,false);
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




        String edgeSlvUrl = "httpss://amerescousa.terragoedge.com/edgeSlvServer/notesGuid?lastSyncTime=";

        long lastSynctime = streetlightDao.getLastSyncTime();
        if(lastSynctime > 0){
            edgeSlvUrl = edgeSlvUrl + lastSynctime;

        }else{
            logger.error("Last Sync not loaded.");
            return;
        }



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

                                   boolean isDroppedPinWorkFlow = isDroppedPinNote(edgeNote,droppedPinTag);
                                   InstallMaintenanceLogModel installMaintenanceLogModel = new InstallMaintenanceLogModel();

                                   installMaintenanceLogModel.setProcessedNoteId(edgeNote.getNoteGuid());
                                   installMaintenanceLogModel.setNoteName(edgeNote.getTitle());
                                   installMaintenanceLogModel.setLastSyncTime(edgeNote.getSyncTime());
                                   installMaintenanceLogModel.setCreatedDatetime(String.valueOf(edgeNote.getCreatedDateTime()));
                                   installMaintenanceLogModel.setParentNoteId(edgeNote.getBaseParentNoteId());
                                   loadDefaultVal(edgeNote, installMaintenanceLogModel);
                                   loadDeviceValues(edgeNote.getTitle(),installMaintenanceLogModel);
                                   SlvInterfaceLogEntity slvInterfaceLogEntity = new SlvInterfaceLogEntity();
                                   slvInterfaceLogEntity.setIdOnController(edgeNote.getTitle());
                                   slvInterfaceLogEntity.setCreateddatetime(System.currentTimeMillis());
                                   slvInterfaceLogEntity.setResync(false);
                                   String utilLocId = null;
                                   if(isDroppedPinWorkFlow){
                                       utilLocId = "5"+edgeNote.getTitle();
                                   }
                                   boolean isDeviceCreated = false;
                                   if(isDroppedPinWorkFlow) {
                                       SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(installMaintenanceLogModel);
                                       String idOnController = edgeNote.getTitle();
                                       boolean isdevicePresent = isDevicePresent(slvTransactionLogs, idOnController);
                                       if (!isdevicePresent) {
                                           int geozoneid = checkGeoZone(edgeNote.getEdgeNotebook().getNotebookName(),slvTransactionLogs);
                                           if(geozoneid == -1){
                                               slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                                               slvInterfaceLogEntity.setErrordetails("no geozone present with this name: "+edgeNote.getEdgeNotebook().getNotebookName()+" in the path of"+properties.getProperty("com.slv.root.geozone"));
                                               isDeviceCreated = false;
                                           }else{
                                               int deviceId = createDevice(slvTransactionLogs,edgeNote,geozoneid);
                                               if(deviceId == -1){
                                                   slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                                                   slvInterfaceLogEntity.setErrordetails("Not able to create device: "+edgeNote.getTitle());
                                                   isDeviceCreated = false;
                                               }else{
                                                   isDeviceCreated = true;
                                               }
                                           }
                                       }else{
                                           isDeviceCreated = false;
                                       }
                                   }
                                   if(isDroppedPinWorkFlow && !isDeviceCreated){

                                   }
                                   if(!isDroppedPinWorkFlow || (isDroppedPinWorkFlow && isDeviceCreated)) {
                                       installationMaintenanceProcessor.processNewAction(edgeNote, installMaintenanceLogModel, false, utilLocId, slvInterfaceLogEntity, isDroppedPinWorkFlow);
                                       //updateSlvStatusToEdge(installMaintenanceLogModel, edgeNote);
                                       LoggingModel loggingModel = installMaintenanceLogModel;
                                       streetlightDao.insertProcessedNotes(loggingModel, installMaintenanceLogModel);
                                       connectionDAO.saveSlvInterfaceLog(slvInterfaceLogEntity);
                                   }
                               }
                           }catch (Exception e){
                                logger.error("Error in run",e);
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
}
