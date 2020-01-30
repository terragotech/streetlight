package com.terragoedge.streetlight.service;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opencsv.CSVWriter;
import com.terragoedge.edgeserver.*;
import com.terragoedge.streetlight.OpenCsvUtils;
import com.terragoedge.streetlight.dao.ClientAccountEntity;
import com.terragoedge.streetlight.edgeinterface.SlvData;
import com.terragoedge.streetlight.edgeinterface.SlvToEdgeService;
import com.terragoedge.streetlight.exception.NoValueException;
import com.terragoedge.streetlight.json.model.*;
import com.terragoedge.streetlight.logging.InstallMaintenanceLogModel;
import com.terragoedge.streetlight.logging.LoggingModel;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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


    public void  run() {
        // Get Already synced noteguids from Database
        //List<String> noteGuids = streetlightDao.getNoteIds();
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



        String edgeSlvUrl =  PropertiesReader.getProperties().getProperty("streetlight.edge.slvserver.url");
        edgeSlvUrl = edgeSlvUrl+"/notesGuid?isBulkImport=true&withRevision=true&lastSyncTime=";

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

            streetlightDao.updateSLVInterfaceStatus();
            // Get Response String
            String notesGuids = edgeSlvServerResponse.getBody();
            System.out.println(notesGuids);

           JsonArray noteGuidsJsonArray = (JsonArray)jsonParser.parse(notesGuids);
           if(noteGuidsJsonArray != null &&  !noteGuidsJsonArray.isJsonNull()){
               for(JsonElement noteGuidJson : noteGuidsJsonArray){

                   String noteGuid = noteGuidJson.getAsString();
                   streetlightDao.updateSLVInterfaceStatus();
                   if(!streetlightDao.isNoteProcessed(noteGuid)){
                       try{
                           SlvRestTemplate.INSTANCE.refreshToken();
                           doProcess(noteGuid,accessToken,false);
                       }catch (Exception e){
                           logger.error("Error",e);
                       }

                   }
               }
           }


        } else {
            logger.error("Unable to get message from EdgeServer. Response Code is :" + edgeSlvServerResponse.getStatusCode());
        }
    }


    private void doProcess(String noteGuid,String accessToken,boolean isReSync){
        DataComparatorRes dataComparatorRes =  compareRevisionData(noteGuid);
        if(dataComparatorRes != null){
            if(dataComparatorRes.isMatched()){
                logger.info("Current Note Data Matched with Previous Revision.");
               return;
            }
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
                        installMaintenanceLogModel.getUnMatchedFormGuids().addAll(dataComparatorRes.getUnMatchedFormGuids());
                        //ES-274
                        String droppedPinUser = null;
                        if(isDroppedPinWorkFlow){
                            droppedPinUser = getDroppedPinUser(edgeNote);
                        }

                        // Check Current note is created via Bulk Import
                        //isBulkImport(edgeNote,accessToken,installMaintenanceLogModel);

                        loadDefaultVal(edgeNote, installMaintenanceLogModel,accessToken,droppedPinUser);


                        slvInterfaceLogEntity.setIdOnController(edgeNote.getTitle());
                        slvInterfaceLogEntity.setCreateddatetime(System.currentTimeMillis());
                        slvInterfaceLogEntity.setResync(false);
                        String utilLocId = null;
                        // Below commented lines need for dropped pin workflow in future
                        if(isDroppedPinWorkFlow){
                            //As per Email Communication, "5" is removed from utilLocId Fwd: Utility Location ID For Dropped Pins (20190918)
                            utilLocId = edgeNote.getTitle();
                        }
                        boolean isDeviceCreated = false;
                        if(edgeNote.getEdgeNotebook() != null && edgeNote.getEdgeNotebook().getNotebookName() != null &&  ( installMaintenanceLogModel.getAtlasPhysicalPage() == null || installMaintenanceLogModel.getAtlasPhysicalPage().isEmpty()) ){
                            installMaintenanceLogModel.setAtlasPhysicalPage(edgeNote.getEdgeNotebook().getNotebookName());
                        }
                        if(isDroppedPinWorkFlow) {
                            isDeviceCreated = processDroppedPinWorkflow(edgeNote,slvInterfaceLogEntity,installMaintenanceLogModel,utilLocId);
                        }
                        if(!isDroppedPinWorkFlow || (isDroppedPinWorkFlow && isDeviceCreated)) {
                            loadDeviceValues(edgeNote.getTitle(),installMaintenanceLogModel);
                            // Check whether the current note
                            isBulkImport(edgeNote,accessToken,installMaintenanceLogModel);

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

    public void generateInstallationRemovedExceptionReport() {
        CSVWriter csvWriter = null;
        Writer writer = null;
        String folderPath = null;
        try{

            folderPath = properties.getProperty("com.installation.report.root.path");
            String fileName = OpenCsvUtils.getCsvFileName();
            folderPath = folderPath + "/"+fileName;
            File folder = new File(folderPath);
            if(!folder.exists()){
                folder.mkdirs();
            }
            folderPath = folderPath + "/InstallationRemovedReport_"+fileName+".csv";
            writer = new FileWriter(folderPath);
            csvWriter = new CSVWriter(writer,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.DEFAULT_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);
            String[] headerRecord = {"title","createdby","slv_macaddress","created_datetime","communication_status"};
            csvWriter.writeNext(headerRecord);

            List<InstallationRemovedExceptionReport> installationRemovedExceptionReportList = connectionDAO.getInstallationRemovedExceptionReport();
            for(InstallationRemovedExceptionReport installationRemovedExceptionReport : installationRemovedExceptionReportList){
                csvWriter.writeNext(new String[]{installationRemovedExceptionReport.getIdOnController(),
                        installationRemovedExceptionReport.getCreatedBy(),
                        installationRemovedExceptionReport.getMacAddress(),
                        OpenCsvUtils.getFormatedDateTime(installationRemovedExceptionReport.getCreatedDateTime()),
                        installationRemovedExceptionReport.getCommunicationStatus()
                });
                csvWriter.flush();
            }
        }catch (Exception e){
            logger.error("Error in generateInstallationRemovedExceptionReport",e);
            return;
        }finally {
            if(csvWriter != null){
                try{
                    csvWriter.close();
                }catch (Exception e){
                    logger.error("Error in generateInstallationRemovedExceptionReport",e);
                }

            }

            if(writer != null){
                try{
                    writer.close();
                }catch (Exception e){
                    logger.error("Error in generateInstallationRemovedExceptionReport",e);
                }
            }
        }

         uploadFileToEdgeSlvServer(folderPath);

    }

    private ResponseEntity<String> uploadFileToEdgeSlvServer(String outputFilePath){
        try{
            String baseUrl =  properties.getProperty("streetlight.edge.slvserver.url");
            String uploadUrl = properties.getProperty("com.installation.report.upload.url");

            String subject = properties.getProperty("com.installation.report.mail.subject");
            String body = properties.getProperty("com.installation.report.mail.body");
            String receipents = properties.getProperty("com.installation.report.mail.receipts");

            baseUrl = baseUrl + uploadUrl;
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> multiValueMap
                    = new LinkedMultiValueMap<>();
            multiValueMap.add("file", new FileSystemResource(outputFilePath));
            multiValueMap.add("subject",subject);
            multiValueMap.add("body",body);
            multiValueMap.add("receipents",receipents);


            HttpEntity<MultiValueMap<String, Object>> requestEntity
                    = new HttpEntity<>(headers);

            ResponseEntity<String> responseEntity = restTemplate.postForEntity(baseUrl, requestEntity, String.class);
            logger.info("------------ Response ------------------");
            logger.info("Response Code:" + responseEntity.getStatusCode().toString());
            return responseEntity;
        }catch (Exception e){
            logger.error("Error in uploadFileToEdgeSlvServer",e);
        }
        return null;

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

    private boolean processDroppedPinWorkflow(EdgeNote edgeNote,SlvInterfaceLogEntity slvInterfaceLogEntity,InstallMaintenanceLogModel installMaintenanceLogModel,String utilLocId){
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
                    int deviceId = createDevice(slvTransactionLogs, edgeNote, geozoneid,installMaintenanceLogModel.getAtlasGroup());
                    logger.info(idOnController+" device created in slv and it's id is: "+deviceId);
                    if (deviceId == -1) {
                        logger.error("Device not created in slv for this idoncontroller: "+idOnController);
                        slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                        slvInterfaceLogEntity.setErrordetails("Not able to create device: " + idOnController);
                        isDeviceCreated = false;
                    } else
                        {
                        //Handle Extra value in SetDevice values
                        callSetDeviceValues(idOnController,utilLocId,edgeNote, slvTransactionLogs,installMaintenanceLogModel);
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


    private void addPoleHeightFixtureCodeComedLite(EdgeNote edgeNote, List<Object> paramsList ){
        String installationFormTemplateGUID = PropertiesReader.getProperties().getProperty("amerescousa.edge.formtemplateGuid");
        String strPoleHeightID = PropertiesReader.getProperties().getProperty("edge.formtemplate.poleheight.id");
        String strFixtureCodeID = PropertiesReader.getProperties().getProperty("edge.formtemplate.fixturecode.id");
        int poleHeightID = Integer.parseInt(strPoleHeightID);
        int fixtureCodeID = Integer.parseInt(strFixtureCodeID);
        List<FormData> lstFormData = edgeNote.getFormData();
        String poleHeight = null;
        String fixtureCode = null;
        List<EdgeFormData> edgeFormDatas = new ArrayList<>();
        for(FormData cur:lstFormData){
            if(cur.getFormTemplateGuid().equals(installationFormTemplateGUID)){
                edgeFormDatas.clear();
                edgeFormDatas.addAll(cur.getFormDef());
                try{
                    String poleHeightTemp = valueById(edgeFormDatas, poleHeightID);
                    poleHeight = poleHeightTemp;
                }catch (NoValueException e){
                    logger.error(e);
                }

                try{
                    String fixtureCodeTemp = valueById(edgeFormDatas, fixtureCodeID);
                    fixtureCode = fixtureCodeTemp;
                }catch (NoValueException e){
                    logger.error(e);
                }

            }
        }

        if(poleHeight != null) {
            addStreetLightData("pole.height", poleHeight, paramsList);
        }
        if(fixtureCode != null) {
            addStreetLightData("luminaire.fixturecode", fixtureCode, paramsList);
            String comedLiteTypeData = "";
            if(fixtureCode.equals("Cobrahead Alley"))
            {
                comedLiteTypeData = "Alley Light";
            }
            else if(fixtureCode.equals("Viaduct"))
            {
                comedLiteTypeData = "Viaduct Light";
            }
            else
            {
                comedLiteTypeData = "Street Light";
            }
            addStreetLightData("comed.litetype", comedLiteTypeData, paramsList);
        }

        addotherParamsForDroppedPin(edgeFormDatas,paramsList);

    }


    private void callSetDeviceValues(String idOnController,String utilLocId,
                                     EdgeNote edgeNote,
                                     SLVTransactionLogs slvTransactionLogs,InstallMaintenanceLogModel installMaintenanceLogModel){
        String controllerStrId = properties.getProperty("streetlight.slv.controllerstrid");
        List<Object> paramsList = new ArrayList<>();
        paramsList.add("idOnController=" + idOnController);
        paramsList.add("controllerStrId=" + controllerStrId);
        EdgeNotebook edgeNotebook = edgeNote.getEdgeNotebook();

        if(edgeNotebook != null) {
            addStreetLightData("location.atlasphysicalpage", edgeNotebook.getNotebookName(), paramsList);
            addStreetLightData("location.atlaspage", edgeNotebook.getNotebookName(), paramsList);
        }

        addStreetLightData("network.lowvoltagethreshold","108",paramsList);
        addStreetLightData("network.highvoltagethreshold","264",paramsList);
        addStreetLightData("location.locationtype","LOCATION_TYPE_POLE", paramsList);

        if (utilLocId != null) {
            addStreetLightData("location.utillocationid", utilLocId, paramsList);
        }

        addStreetLightData("location.cdotlocationtype", "CDOLOCATION_TYPE_STREET", paramsList);
        addPoleHeightFixtureCodeComedLite(edgeNote,paramsList);

        //ES-274
        if(installMaintenanceLogModel.isAmerescoUser()){
            addStreetLightData("comed.projectname","Ameresco dropped pin", paramsList);
        }
        //ES-279
        String clientAccountNumber = getClientAccountNumber(installMaintenanceLogModel,edgeNote);
        logger.info("callSetDeviceValues: clientAccountNumber = "+clientAccountNumber);
        if(clientAccountNumber != null){
            addStreetLightData("client.accountnumber", clientAccountNumber, paramsList);
        }

        installationMaintenanceProcessor.setDeviceValues(paramsList,slvTransactionLogs);
    }


    private String getClientAccountNumber(InstallMaintenanceLogModel installMaintenanceLogModel,EdgeNote edgeNote){
        try {
            String clientAccountNumber = "CDOT";
            String atlasPageName = "";
            int atlasPageRange = 0;
            String area = null;
            String physicalAtalasPage = installMaintenanceLogModel.getAtlasPhysicalPage();
            logger.info("getClientAccountNumber: physicalAtalasPage= " + physicalAtalasPage);
            if (physicalAtalasPage.contains("-")) {
                String[] atlaspageValues = physicalAtalasPage.split("-", -1);
                logger.info("getClientAccountNumber: atlaspageValues= " + atlaspageValues);
                if (atlaspageValues.length == 2) {
                    atlasPageName = atlaspageValues[0];
                    atlasPageRange = Integer.valueOf(atlaspageValues[1]);
                    logger.info("getClientAccountNumber: atlasPageName= " + atlasPageName);
                    logger.info("getClientAccountNumber: atlasPageRange= " + atlasPageRange);
                    if (atlasPageRange >= 4 && atlasPageRange <= 26) {//North
                        area = "CN";
                    } else if (atlasPageRange >= 27 && atlasPageRange <= 55) {
                        area = "CS";
                    }
                    if (area != null) {
                        clientAccountNumber += " " + area + " ";
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
                String fixtureCode = getFixtureCode(edgeNote);
                logger.info("getClientAccountNumber: fixtureCode= " + fixtureCode);
                if (fixtureCode.equals("Viaduct")) {
                    clientAccountNumber += "Subway";
                    return clientAccountNumber;
                } else {
                    ClientAccountEntity clientAccountEntity = connectionDAO.getClientAccountName(atlasPageName, atlasPageRange, area);
                    logger.info("getClientAccountNumber: clientAccountEntity= " + gson.toJson(clientAccountEntity));
                    if (clientAccountEntity != null) {
                        clientAccountNumber += clientAccountEntity.getValue();
                        return clientAccountNumber;
                    } else {
                        return null;
                    }
                }
            } else {
                return null;
            }
        }catch (Exception e){
            logger.error("Error in getClientAccountNumber", e);
            return null;
        }
    }

    private String getFixtureCode(EdgeNote edgeNote) {
        String fixtureCode = "";
        List<FormData> formDatas = edgeNote.getFormData();
        for (FormData formData : formDatas) {
            if (formData.getFormTemplateGuid().equals(properties.getProperty("amerescousa.edge.formtemplateGuid"))) {
                List<EdgeFormData> edgeFormDatas = formData.getFormDef();
                for (EdgeFormData edgeFormData : edgeFormDatas) {
                    if (edgeFormData.getId() == Integer.valueOf(properties.getProperty("edge.formtemplate.fixturecode.id"))) {
                        fixtureCode = edgeFormData.getValue();
                    }
                }
            }
        }
        return fixtureCode;
    }

    private void addotherParamsForDroppedPin(List<EdgeFormData> edgeFormDatas, List<Object> paramsList) {
        int poleMaterialId = Integer.valueOf(properties.getProperty("com.edge.pole.material.id"));
        int fixtureTypeId = Integer.valueOf(properties.getProperty("edge.formtemplate.fixturecode.id"));

        String poleMaterial = "";
        String fixtureType = "";
        try {
            poleMaterial = valueById(edgeFormDatas, poleMaterialId);
        } catch (Exception e) {
            logger.error("Error while getting pole material from this id: " + poleMaterialId, e);
        }

        try {
            fixtureType = valueById(edgeFormDatas, fixtureTypeId);
        } catch (Exception e) {
            logger.error("Error while getting fixture type from this id: " + fixtureTypeId, e);
        }
        if (poleMaterial != null && !poleMaterial.equals("")) {
            // pole material
            addStreetLightData("pole.material", poleMaterial, paramsList);
        }

        String comedLiteType = null;
        if (poleMaterial.equals("Wood")) {
            comedLiteType = "Alley Light";
        } else if (poleMaterial.equals("No Pole") && fixtureType.equals("Cobrahead Alley")) {
            comedLiteType = "Alley Light";
        } else if (poleMaterial.equals("No Pole") && fixtureType.equals("Viaduct")) {
            comedLiteType = "Viaduct Light";
        } else if (poleMaterial.equals("No Pole") && !fixtureType.equals("Cobrahead Alley") && !fixtureType.equals("Viaduct")) {
            comedLiteType = "Street Light";
        } else if (!poleMaterial.equals("Wood") && !poleMaterial.equals("No Pole")) {
            comedLiteType = "Street Light";
        }
        if (comedLiteType != null && !comedLiteType.equals("")) {
            // comed litetype
            addStreetLightData("comed.litetype", comedLiteType, paramsList);
        }
    }


    private DataComparatorRes compareRevisionData(String noteGuid) {
        logger.info("Comparing data from the Previous Revision.");
        String url =  PropertiesReader.getProperties().getProperty("streetlight.edge.url.checkrevisiondata");
        String config = PropertiesReader.getProperties().getProperty("streetlight.edge.url.checkrevisiondata.config");
        JsonObject configJson = (JsonObject)jsonParser.parse(config);
        configJson.addProperty("noteGuid",noteGuid);
        logger.info("Given url is :" + url);
        // Get NoteList from edgeserver
        ResponseEntity<String> responseEntity = restService.callPostMethod(url, HttpMethod.POST, configJson.toString());

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String responseBody = responseEntity.getBody();
            logger.info(responseBody);
            DataComparatorRes dataComparatorRes = gson.fromJson(responseBody, DataComparatorRes.class);
            return dataComparatorRes;
        }
        return null;

    }



    // http://192.168.1.9:8080/edgeServer/oauth/token?grant_type=password&username=admin&password=admin&client_id=edgerestapp
}
