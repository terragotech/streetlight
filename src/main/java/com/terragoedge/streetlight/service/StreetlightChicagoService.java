package com.terragoedge.streetlight.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;

import com.terragoedge.streetlight.OpenCsvUtils;
import com.terragoedge.streetlight.edgeinterface.SlvData;
import com.terragoedge.streetlight.edgeinterface.SlvToEdgeService;
import com.terragoedge.streetlight.json.model.ContextList;
import com.terragoedge.streetlight.json.model.CslpDate;
import com.terragoedge.streetlight.json.model.ExistingMacValidationFailure;
import com.terragoedge.streetlight.json.model.SlvInterfaceLogEntity;
import com.terragoedge.streetlight.logging.InstallMaintenanceLogModel;
import com.terragoedge.streetlight.logging.LoggingModel;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.http.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.streetlight.PropertiesReader;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

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




        String edgeSlvUrl = "https://amerescousa.terragoedge.com/edgeSlvServer/notesGuid?lastSyncTime=";

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
                                   installationMaintenanceProcessor.processNewAction(edgeNote, installMaintenanceLogModel, false, null,slvInterfaceLogEntity);
                                   //updateSlvStatusToEdge(installMaintenanceLogModel, edgeNote);
                                   LoggingModel loggingModel = installMaintenanceLogModel;
                                   streetlightDao.insertProcessedNotes(loggingModel, installMaintenanceLogModel);
                                   connectionDAO.saveSlvInterfaceLog(slvInterfaceLogEntity);
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

    public ResponseEntity<String> edgeSlvserverCall(String url) {
        long millis = DateTime.now().minusDays(1).withTimeAtStartOfDay().getMillis();
        List<ExistingMacValidationFailure> existingMacValidationFailures = connectionDAO.getAllExistingMacVaildationFailures(millis);
        List<String[]> datas = new ArrayList<>();
        String[] headers = {"idoncontroller","noteguid","createdby","slvmacaddress","edge_existingmacaddress","edge_newmacaddress","created_datetime","processed_datetime"};
        datas.add(headers);
        for(ExistingMacValidationFailure existingMacValidationFailure : existingMacValidationFailures){
            List<String> data = new ArrayList<>();
            data.add(existingMacValidationFailure.getIdOnController());
            data.add(existingMacValidationFailure.getNoteGuid());
            data.add(existingMacValidationFailure.getCreatedBy());
            data.add(existingMacValidationFailure.getSlvMacaddress());
            data.add(existingMacValidationFailure.getEdgeExistingMacaddress());
            data.add(existingMacValidationFailure.getEdgeNewNodeMacaddress());
            data.add(OpenCsvUtils.getFormatedDateTime(existingMacValidationFailure.getCreatedDateTime()));
            data.add(OpenCsvUtils.getFormatedDateTime(existingMacValidationFailure.getProcessedDateTime()));
            datas.add(data.toArray(new String[0]));
        }
        String fileName = OpenCsvUtils.getCsvFileName()+".csv";
        String folderPath = properties.getProperty("com.existing.macaddress.failure.report.path");
        File folder = new File(folderPath);
        if(!folder.exists()){
            folder.mkdirs();
        }
        String outputFilePath = folderPath+"/"+fileName;
        try {
            OpenCsvUtils.csvWriterAll(datas,outputFilePath);
        }catch (Exception e){
            e.printStackTrace();
        }
        return uploadFileToEdgeSlvServer(url,outputFilePath);
    }

    private ResponseEntity<String> uploadFileToEdgeSlvServer(String url,String outputFilePath){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body
                = new LinkedMultiValueMap<>();
        body.add("file", new File(outputFilePath));

        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, requestEntity, String.class);
        logger.info("------------ Response ------------------");
        logger.info("Response Code:" + responseEntity.getStatusCode().toString());
        return responseEntity;
    }
    // http://192.168.1.9:8080/edgeServer/oauth/token?grant_type=password&username=admin&password=admin&client_id=edgerestapp
}
