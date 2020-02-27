package com.terragoedge.streetlight.swap;

import com.google.gson.JsonObject;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.edgeserver.FormData;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.exception.InValidBarCodeException;
import com.terragoedge.streetlight.exception.NoValueException;
import com.terragoedge.streetlight.exception.QRCodeAlreadyUsedException;
import com.terragoedge.streetlight.exception.ReplaceOLCFailedException;
import com.terragoedge.streetlight.json.model.*;
import com.terragoedge.streetlight.logging.InstallMaintenanceLogModel;
import com.terragoedge.streetlight.logging.LoggingModel;
import com.terragoedge.streetlight.service.AbstractProcessor;
import com.terragoedge.streetlight.swap.exception.NoDataChangeException;
import com.terragoedge.streetlight.swap.exception.SkipNoteException;
import com.terragoedge.streetlight.swap.model.CityWorkflowSyncLog;
import com.terragoedge.streetlight.swap.model.DataDiffResponse;
import org.apache.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;

public class SwapTemplateProcessor extends AbstractProcessor {

    final Logger logger = Logger.getLogger(SwapTemplateProcessor.class);

    private String controllerStrId;

    public SwapTemplateProcessor(WeakHashMap<String, String> contextListHashMap, HashMap<String, SLVDates> cslpDateHashMap){
        super();
        this.contextListHashMap = contextListHashMap;
        this.cslpDateHashMap = cslpDateHashMap;
        controllerStrId = properties.getProperty("streetlight.slv.controllerstrid");
    }

    public void processSwapData(EdgeNote edgeNote) throws NoDataChangeException,SkipNoteException {
        CityWorkflowSyncLog cityWorkflowSyncLog = null;
        SlvInterfaceLogEntity slvInterfaceLogEntity = null;
        try {
            // Get Form Data
            List<FormData> formDataList = edgeNote.getFormData();
            boolean isSwapFormPresent = false;
            // Check current note has coc formtemplate or not.
            String formTemplateGuid = PropertiesReader.getProperties().getProperty("streetlight.edge.coc.formtemplate.guid");
            for (FormData formData : formDataList) {
                if (formData.getFormTemplateGuid().equals(formTemplateGuid)) {
                    isSwapFormPresent = true;
                }

            }
            // If COC Formtemplate is present, then continue the process. Otherwise, it will fall back to Ameresco workflow.
            if (isSwapFormPresent) {
                DataDiffResponse dataDiffResponse = compareRevisionData(edgeNote.getNoteGuid());
                if (dataDiffResponse != null) {
                    cityWorkflowSyncLog = new CityWorkflowSyncLog();

                    slvInterfaceLogEntity = new SlvInterfaceLogEntity();

                    slvInterfaceLogEntity.setIdOnController(edgeNote.getTitle());
                    slvInterfaceLogEntity.setCreateddatetime(System.currentTimeMillis());
                    slvInterfaceLogEntity.setResync(false);
                    slvInterfaceLogEntity.setParentnoteid((edgeNote.getBaseParentNoteId() == null) ? edgeNote.getNoteGuid() : edgeNote.getBaseParentNoteId());
                    slvInterfaceLogEntity.setIdOnController(edgeNote.getTitle());

                    slvProcess(edgeNote,cityWorkflowSyncLog,slvInterfaceLogEntity);

                    if(slvInterfaceLogEntity != null){
                        connectionDAO.saveSlvInterfaceLog(slvInterfaceLogEntity);
                    }
                } else {
                    logger.error("No Response from Previous Revision Comparison");
                    // Due to error, so need to skip this note.
                    throw new SkipNoteException("No Response from Previous Revision Comparison");
                }

            } else {
                // No COC form, so it throws NoDataChangeException to continue Ameresco workflow.
                throw new NoDataChangeException("COC FormTemplate is not Present.");

            }

        } catch (NoDataChangeException e) {
            throw new NoDataChangeException(e.getMessage());
        } catch (Exception e){
            logger.error("Error in processSwapData",e);
            if(slvInterfaceLogEntity != null){
                connectionDAO.saveSlvInterfaceLog(slvInterfaceLogEntity);
            }
            throw new SkipNoteException(e);
        }finally {
            // -- TODO Need to Save cityWorkflowSyncLog
        }
    }


    /**
     * Call Edge REST Api to analyze data change. Checking data with Previous Revision. If no data change, then it
     * throws NoDataChangeException (Continue to Ameresco Workflow).SkipNoteException (Something Error in REST, so dont process this note.)
     *
     * @param noteGuid
     * @return
     * @throws NoDataChangeException
     * @throws SkipNoteException
     */
    private DataDiffResponse compareRevisionData(String noteGuid) throws NoDataChangeException, SkipNoteException {
        logger.info("Comparing data from the Previous Revision.");
        String url = PropertiesReader.getProperties().getProperty("streetlight.edge.coc.url.checkrevisiondata");
        String config = PropertiesReader.getProperties().getProperty("streetlight.edge.coc.url.checkrevisiondata.config");
        JsonObject configJson = (JsonObject) jsonParser.parse(config);
        configJson.addProperty("noteGuid", noteGuid);
        logger.info("Given url is :" + url);
        // Compare Revision data to identify any changes or not.
        ResponseEntity<String> responseEntity = restService.callPostMethod(url, HttpMethod.POST, configJson.toString());
        // Success Response
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String responseBody = responseEntity.getBody();
            logger.info(responseBody);
            // Json to Java Object
            DataDiffResponse dataDiffResponse = gson.fromJson(responseBody, DataDiffResponse.class);
            // If status code, 404 either formtemplate not present or edgenote not present. So continue Ameresco worklfow
            if (dataDiffResponse.getStatusCode() == 404) {
                logger.info("Response Code: 404.");
                throw new NoDataChangeException(responseBody);
            } else if (dataDiffResponse.getStatusCode() == 500) {
                logger.info("Skip this note due to error");
                //  Something went wrong, Skip this note
                throw new SkipNoteException(responseBody);
            } else if (dataDiffResponse.getStatusCode() == 200) {
                // If there is not data change, then throws NoDataChangeException
                if (!dataDiffResponse.isChanged()) {
                    logger.info("No Data Changes with the Previous revision.");
                    throw new NoDataChangeException(responseBody);
                }
            }
            return dataDiffResponse;
        }
        return null;

    }

    private void slvProcess(EdgeNote edgeNote, CityWorkflowSyncLog cityWorkflowSyncLog,SlvInterfaceLogEntity slvInterfaceLogEntity) throws NoDataChangeException, SkipNoteException{
        List<FormData> formDataList = edgeNote.getFormData();
        // Iterate to COC formtemplate
        String formTemplateGuid = PropertiesReader.getProperties().getProperty("streetlight.edge.coc.formtemplate.guid");
        for (FormData formData : formDataList) {
            if (formData.getFormTemplateGuid().equals(formTemplateGuid)) {
                List<EdgeFormData> edgeFormDataList = formData.getFormDef();
                // As we discussed with Vish (26 feb 2020), based on action value, corresponding data needs to sync with SLV.
                try {
                    // Get Action Value from formdef.
                    String actionVal = valueById(edgeFormDataList, 24); // -- TODO Need to change Id
                    logger.info("Action Value:"+actionVal);
                    cityWorkflowSyncLog.setAction(actionVal);
                    slvInterfaceLogEntity.setSelectedAction(actionVal);
                    switch (actionVal) {
                        case "Replace Node only":
                            // Get MAC Address under Replace Node only
                            getFormValue(24,-1,edgeFormDataList,cityWorkflowSyncLog);  // -- TODO Need to change Id
                            break;

                        case "Replace Node and Fixture":
                            // Get MAC Address and Fixture QR Scan under Replace Node and Fixture
                            getFormValue(24,24,edgeFormDataList,cityWorkflowSyncLog);  // -- TODO Need to change Id
                            break;
                        case "Replace Fixture only":
                            // Get Fixture QR Scan under Replace Fixture only
                            getFormValue(-1,24,edgeFormDataList,cityWorkflowSyncLog);  // -- TODO Need to change Id
                            break;
                    }
                    slvSyncProcess(cityWorkflowSyncLog,edgeNote,slvInterfaceLogEntity);
                } catch (NoValueException e) {
                    logger.info("No Action value");
                    throw new NoDataChangeException("No Action value is selected.");
                }

            }

        }
    }


    /**
     * Get MAC Address and Fixture QR Scan based on Given id. Both Values are not Present, then throws an Exception
     * @param macId
     * @param fixId
     * @param edgeFormDataList
     * @param cityWorkflowSyncLog
     * @throws SkipNoteException
     */
    private void getFormValue(int macId,int fixId,List<EdgeFormData> edgeFormDataList,CityWorkflowSyncLog cityWorkflowSyncLog)throws SkipNoteException{
        // Get MAC Address
        if(macId != -1){
            try{
                String macAddress = valueById(edgeFormDataList, macId);
                logger.info("MAC Address:"+macAddress);
                cityWorkflowSyncLog.setMacAddress(macAddress);
            }catch (NoValueException e){
                cityWorkflowSyncLog.setMacAddressSynced(false);
                cityWorkflowSyncLog.setMacSyncStatus("No MAC");
            }
        }else{
            cityWorkflowSyncLog.setMacAddressSynced(false);
            cityWorkflowSyncLog.setMacSyncStatus("Only Fixture Changes");
        }



        // Get Fixture QR Scan
        if(fixId != -1){
            try {
                String fixtureQrScan = valueById(edgeFormDataList, fixId);
                cityWorkflowSyncLog.setFixtureQRScan(fixtureQrScan);
                logger.info("Fixture Qr Scan:"+fixtureQrScan);
            }catch (NoValueException e){
                cityWorkflowSyncLog.setFixtureQRScanSynced(false);
                cityWorkflowSyncLog.setFixtureQRScanSyncStatus("No Fixture QR Scan");
            }
        }else{
            cityWorkflowSyncLog.setFixtureQRScanSynced(false);
            cityWorkflowSyncLog.setFixtureQRScanSyncStatus("Only Mac Address Changes");
        }


        // If both MAC Address and Fixture QR Scan not present, then no need to process this fixture
        if((cityWorkflowSyncLog.getMacAddress() == null || cityWorkflowSyncLog.getMacAddress().trim().isEmpty()) && (cityWorkflowSyncLog.getFixtureQRScan() == null || cityWorkflowSyncLog.getFixtureQRScan().trim().isEmpty())){
            logger.info("Both MAC Address and Fixture QR Scan value not Present.");
            throw new SkipNoteException("");
        }
    }

    /**
     * Sync MAC Address and Fixture QR Scan to the SLV.
     * @param cityWorkflowSyncLog
     */
   private void slvSyncProcess(CityWorkflowSyncLog cityWorkflowSyncLog,EdgeNote edgeNote,SlvInterfaceLogEntity slvInterfaceLogEntity)throws SkipNoteException{
       InstallMaintenanceLogModel installMaintenanceLogModel = new InstallMaintenanceLogModel();
       installMaintenanceLogModel.setDroppedPinWorkflow(false);
       installMaintenanceLogModel.setProcessedNoteId(edgeNote.getNoteGuid());
       installMaintenanceLogModel.setNoteName(edgeNote.getTitle());
       installMaintenanceLogModel.setLastSyncTime(edgeNote.getSyncTime());
       installMaintenanceLogModel.setCreatedDatetime(String.valueOf(edgeNote.getCreatedDateTime()));
       installMaintenanceLogModel.setParentNoteId(edgeNote.getBaseParentNoteId());
       installMaintenanceLogModel.setIdOnController(edgeNote.getTitle());

       // This is need, this flag is used to add comed.componentffectivedate  and
       installMaintenanceLogModel.setReplace(true);

       // Adding dummy proposed context if not, so that we avoid proposed context validation.
       installMaintenanceLogModel.setProposedContext(edgeNote.getLocationDescription() != null ? edgeNote.getLocationDescription() : "No Val");
       try {
           loadDeviceValues(cityWorkflowSyncLog.getTitle(),installMaintenanceLogModel);
       } catch (Exception e) {
           logger.error("Error during get Device Details",e);
           throw new SkipNoteException(e);
       }


       // MAC Address is present, then call ReplaceOLC
        if(cityWorkflowSyncLog.getMacAddress() != null && !cityWorkflowSyncLog.getMacAddress().trim().isEmpty()){
            try{
                // Check MAC Address is already present in any other fixture or not. If it present, no need to sync this mac.
                checkMacAddressExists(cityWorkflowSyncLog.getMacAddress(),installMaintenanceLogModel.getIdOnController(),null,null,installMaintenanceLogModel,slvInterfaceLogEntity);

                // Call Empty ReplaceOLC if SLV has MAC Address.
                String slvMacAddress = installMaintenanceLogModel.getSlvMacaddress();
                if(slvMacAddress != null) {
                    SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(edgeNote);
                    try {
                        replaceOLC(controllerStrId,edgeNote.getTitle(),"",slvTransactionLogs,slvInterfaceLogEntity,installMaintenanceLogModel.getAtlasPhysicalPage(),installMaintenanceLogModel,edgeNote);
                    } catch (ReplaceOLCFailedException e) {
                        logger.error("Error in slvSyncProcess",e);
                    }
                }
                // Call Set Device to send MAC Address
                setMACAddress(installMaintenanceLogModel,slvMacAddress,edgeNote);

                // Call ReplaceOLC With MAC Address
                SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(edgeNote);

                try {
                    replaceOLC(controllerStrId,edgeNote.getTitle(),cityWorkflowSyncLog.getMacAddress().trim(),slvTransactionLogs,slvInterfaceLogEntity,installMaintenanceLogModel.getAtlasPhysicalPage(),installMaintenanceLogModel,edgeNote);
                } catch (ReplaceOLCFailedException e) {
                    logger.error("Error in slvSyncProcess",e);
                    cityWorkflowSyncLog.setMacAddressSynced(false);
                    cityWorkflowSyncLog.setMacSyncStatus(e.getMessage());
                }
            }catch (QRCodeAlreadyUsedException e){
                logger.error("Error in slvSyncProcess",e);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // Get Fixture QR Scan Value and
        if(cityWorkflowSyncLog.getFixtureQRScan() != null && !cityWorkflowSyncLog.getFixtureQRScan().trim().isEmpty()){
            SlvServerData slvServerData = new SlvServerData();
            List<Object> paramsList = new ArrayList<>();
            String idOnController = installMaintenanceLogModel.getIdOnController();
            paramsList.add("idOnController=" + idOnController);
            paramsList.add("controllerStrId=" + installMaintenanceLogModel.getControllerSrtId());
            addStreetLightData("luminaire.installdate", dateFormat(edgeNote.getCreatedDateTime()), paramsList);
            try {
                buildFixtureStreetLightData(cityWorkflowSyncLog.getFixtureQRScan().trim(),paramsList,edgeNote,slvServerData,installMaintenanceLogModel);
                SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(installMaintenanceLogModel);
                int errorCode = setDeviceValues(paramsList, slvTransactionLogs);
                logger.info("Error Code:"+errorCode);
                if (errorCode != 0) {
                    cityWorkflowSyncLog.setFixtureQRScanSynced(false);
                    cityWorkflowSyncLog.setFixtureQRScanSyncStatus("Error During SetDevice Values.");
                }
            } catch (InValidBarCodeException e) {
                logger.error("Error in slvSyncProcess",e);
            }

        }

   }





   private void setMACAddress( InstallMaintenanceLogModel installMaintenanceLogModel,String slvMacAddress,EdgeNote edgeNote){
       List paramsList = new ArrayList();
       String idOnController = installMaintenanceLogModel.getIdOnController();
       paramsList.add("idOnController=" + idOnController);
       paramsList.add("controllerStrId=" + installMaintenanceLogModel.getControllerSrtId());
       addStreetLightData("MacAddress", slvMacAddress, paramsList);
       addStreetLightData("install.date", dateFormat(edgeNote.getCreatedDateTime()), paramsList);
       SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(installMaintenanceLogModel);
       int errorCode = setDeviceValues(paramsList, slvTransactionLogs);
       logger.info("Error Code:"+errorCode);

   }


    public SLVTransactionLogs getSLVTransactionLogs(EdgeNote edgeNote) {
        SLVTransactionLogs slvTransactionLogs = new SLVTransactionLogs();
        slvTransactionLogs.setNoteGuid(edgeNote.getNoteGuid());
        slvTransactionLogs.setTitle(edgeNote.getTitle());
        slvTransactionLogs.setCreatedDateTime(edgeNote.getCreatedDateTime());
        slvTransactionLogs.setParentNoteGuid(edgeNote.getBaseParentNoteId());
        slvTransactionLogs.setDroppedPinWorkflow(false);

        return slvTransactionLogs;
    }




}
