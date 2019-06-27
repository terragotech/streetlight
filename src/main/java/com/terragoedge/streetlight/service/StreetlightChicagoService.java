package com.terragoedge.streetlight.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;

import com.terragoedge.edgeserver.FormData;
import com.terragoedge.streetlight.OpenCsvUtils;
import com.terragoedge.streetlight.edgeinterface.SlvData;
import com.terragoedge.streetlight.edgeinterface.SlvToEdgeService;
import com.terragoedge.streetlight.json.model.ExistingMacValidationFailure;
import com.terragoedge.streetlight.json.model.SLVTransactionLogs;
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
        installationMaintenanceProcessor = new InstallationMaintenanceProcessor(contextListHashMap,cslpDateHashMap,macHashMap);
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
                    if (true ||loggingModelTemp.getStatus() == null || loggingModelTemp.getStatus().toLowerCase().equals("error") || loggingModelTemp.getStatus().toLowerCase().equals("failure")) {
                        streetlightDao.deleteProcessedNotes(loggingModelTemp.getProcessedNoteId());
                        String utilLocId = getUtilLocationId(loggingModelTemp.getErrorDetails());
                       // installationMaintenanceProcessor.reSync(line, accessToken, true, utilLocId,false);
                        doProcess(line,accessToken,true);
                    }
                } else {
                    logger.info("Note is not Synced. Syncing now.");
                    doProcess(line,accessToken,true);
                    //installationMaintenanceProcessor.reSync(line, accessToken, false, null,false);
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




        String edgeSlvUrl = "http://199.233.241.175/edgeSlvServer/notesGuid?lastSyncTime=";

        long lastSynctime = streetlightDao.getLastSyncTime();
        if(lastSynctime > 0){
            edgeSlvUrl = edgeSlvUrl + lastSynctime;

        }else{
            lastSynctime = System.currentTimeMillis() - (10 * 60000);
            edgeSlvUrl = edgeSlvUrl + lastSynctime;
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
                        doProcess(noteGuid,accessToken,false);
                   }

               }
           }


        } else {
            logger.error("Unable to get message from EdgeServer. Response Code is :" + edgeSlvServerResponse.getStatusCode());
        }
    }


    private void doProcess(String noteGuid,String accessToken,boolean isReSync){
        String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.main");

        url = url + PropertiesReader.getProperties().getProperty("streetlight.edge.url.notes.get");

        url = url + "/" +noteGuid;
        logger.info("Given url is :" + url);

        // Get NoteList from edgeserver
        ResponseEntity<String> responseEntity = restService.getRequest(url, false, accessToken);

        // Process only response code as success
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            SlvInterfaceLogEntity slvInterfaceLogEntity = new SlvInterfaceLogEntity();
            try{
                String notesData = responseEntity.getBody();
                logger.info("rest service data:" + notesData);
                EdgeNote edgeNote = gson.fromJson(notesData, EdgeNote.class);
                if ((!edgeNote.getCreatedBy().contains("admin") && !edgeNote.getCreatedBy().contains("slvinterface")) ||  isReSync) {
                    // Below commented line need for dropped pin workflow in future
                    boolean isDroppedPinWorkFlow = isDroppedPinNote(edgeNote,droppedPinTag);
                    logger.info("isDroppedPinWorkFlow:"+isDroppedPinWorkFlow);
                    InstallMaintenanceLogModel installMaintenanceLogModel = new InstallMaintenanceLogModel();
                    installMaintenanceLogModel.setDroppedPinWorkflow(isDroppedPinWorkFlow);
                    installMaintenanceLogModel.setProcessedNoteId(edgeNote.getNoteGuid());
                    installMaintenanceLogModel.setNoteName(edgeNote.getTitle());
                    installMaintenanceLogModel.setLastSyncTime(edgeNote.getSyncTime());
                    installMaintenanceLogModel.setCreatedDatetime(String.valueOf(edgeNote.getCreatedDateTime()));
                    installMaintenanceLogModel.setParentNoteId(edgeNote.getBaseParentNoteId());
                    loadDefaultVal(edgeNote, installMaintenanceLogModel);


                    slvInterfaceLogEntity.setIdOnController(edgeNote.getTitle());
                    slvInterfaceLogEntity.setCreateddatetime(System.currentTimeMillis());
                    slvInterfaceLogEntity.setResync(false);
                    String utilLocId = null;
                    // Below commented lines need for dropped pin workflow in future
                    if(isDroppedPinWorkFlow){
                        utilLocId = "5"+edgeNote.getTitle();
                    }
                    boolean isDeviceCreated = false;
                    if(isDroppedPinWorkFlow) {
                        isDeviceCreated = processDroppedPinWorkflow(edgeNote,slvInterfaceLogEntity,installMaintenanceLogModel);
                    }
                    if(!isDroppedPinWorkFlow || (isDroppedPinWorkFlow && isDeviceCreated)) {
                        loadDeviceValues(edgeNote.getTitle(),installMaintenanceLogModel);
                        installationMaintenanceProcessor.processNewAction(edgeNote, installMaintenanceLogModel, false, utilLocId, slvInterfaceLogEntity);
                       // updateSlvStatusToEdge(installMaintenanceLogModel, edgeNote);
                        LoggingModel loggingModel = installMaintenanceLogModel;
                        streetlightDao.insertProcessedNotes(loggingModel, installMaintenanceLogModel);
                    }
                }
            }catch (Exception e){
                logger.error("Error in run",e);
            }finally {
                connectionDAO.saveSlvInterfaceLog(slvInterfaceLogEntity);
            }
            // Get Response String

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
        logger.error("Output File Path:"+outputFilePath);
        try {
            OpenCsvUtils.csvWriterAll(datas,outputFilePath);
        }catch (Exception e){
           logger.error("Error in edgeSlvserverCall",e);
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
    private int validateForms(EdgeNote edgeNote){
        String installFormTemplateGuid = properties.getProperty("amerescousa.edge.formtemplateGuid");
        List<FormData> formDatas = edgeNote.getFormData();
        int count = 0;
        for(FormData formData : formDatas){
            if(formData.getFormTemplateGuid().equals(installFormTemplateGuid)){
                count++;
            }
        }
        return count;
    }

    private boolean processDroppedPinWorkflow(EdgeNote edgeNote,SlvInterfaceLogEntity slvInterfaceLogEntity,InstallMaintenanceLogModel installMaintenanceLogModel){
        SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(installMaintenanceLogModel);
        String idOnController = edgeNote.getTitle();
        boolean isDeviceCreated = false;
        boolean isdevicePresent = isDevicePresent(slvTransactionLogs, idOnController);
        logger.info("The device present with the same idoncontroller: "+idOnController+ "result: "+isdevicePresent);
        if (!isdevicePresent) {
            int geozoneid = checkAndCreateGeoZone(edgeNote.getEdgeNotebook().getNotebookName(),slvTransactionLogs);
            if(geozoneid == -1){
                logger.error("Skipping this device. Dueto there is no geozone present with this name: "+edgeNote.getEdgeNotebook().getNotebookName());
                slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                slvInterfaceLogEntity.setErrordetails("no geozone present with this name: "+edgeNote.getEdgeNotebook().getNotebookName()+" in the path of"+properties.getProperty("com.slv.root.geozone"));
                isDeviceCreated = false;
            }else{
                int count = validateForms(edgeNote);
                if(count == 0){
                    logger.error("Skipping this device. Due to there is no install and maintenance form for this device: "+idOnController);
                    slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                    slvInterfaceLogEntity.setErrordetails("No installAndMaintenance form for this note: "+idOnController);
                    isDeviceCreated = false;
                }else if(count > 1){
                    logger.error("Skipping this device. Due to there is two or more no.of install and maintenance form for this device: "+idOnController);
                    slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                    slvInterfaceLogEntity.setErrordetails("two or more installAndMaintenance forms available for this note: "+idOnController);
                    isDeviceCreated = false;
                }else {
                    logger.info(idOnController+ " this device having one install and maintenance form. So it's going to process");
                    int deviceId = createDevice(slvTransactionLogs, edgeNote, geozoneid);
                    logger.info(idOnController+" device created in slv and it's id is: "+deviceId);
                    if (deviceId == -1) {
                        logger.error("Device not created in slv for this idoncontroller: "+idOnController);
                        slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                        slvInterfaceLogEntity.setErrordetails("Not able to create device: " + idOnController);
                        isDeviceCreated = false;
                    } else {
                        logger.info("Device successfully created for this idoncontroller: "+idOnController);
                        isDeviceCreated = true;
                    }
                }
            }
        }else{
           // logger.info("Device already present with the same name: "+idOnController);
           // slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
           // slvInterfaceLogEntity.setErrordetails("device already present with the same name: "+idOnController);
            // As we discussed with vish, If Device is already present in SLV, then interface will continue  normal workflow eventhough device was created by Chicago(In SLV). Date: 19-June-2019
            isDeviceCreated = true;
        }
        return isDeviceCreated;
    }
    // http://192.168.1.9:8080/edgeServer/oauth/token?grant_type=password&username=admin&password=admin&client_id=edgerestapp
}
