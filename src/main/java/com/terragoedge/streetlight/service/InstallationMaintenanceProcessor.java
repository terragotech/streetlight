package com.terragoedge.streetlight.service;

import com.terragoedge.edgeserver.*;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.Utils;
import com.terragoedge.streetlight.enumeration.InstallStatus;
import com.terragoedge.streetlight.enumeration.DateType;
import com.terragoedge.streetlight.exception.*;
import com.terragoedge.streetlight.json.model.*;
import com.terragoedge.streetlight.logging.InstallMaintenanceLogModel;
import com.terragoedge.streetlight.logging.LoggingModel;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

public class InstallationMaintenanceProcessor extends AbstractProcessor {


    public InstallationMaintenanceProcessor(WeakHashMap<String, String> contextListHashMap, HashMap<String, SLVDates> cslpDateHashMap, HashMap<String, String> macHashMap) {
        super();
        this.contextListHashMap = contextListHashMap;
        this.cslpDateHashMap = cslpDateHashMap;
        this.macHashMap = macHashMap;
    }

    private static final String INSTATALLATION_AND_MAINTENANCE_GUID = "8b722347-c3a7-41f4-a8a9-c35dece6f98b";

    final Logger logger = Logger.getLogger(InstallationMaintenanceProcessor.class);


    private LoggingModel getExistingIdOnContoller(EdgeNote edgeNote) {
        String fixtureFormTemplateGuid = properties.getProperty("streetlight.edge.formtemplateguid.fixture");
        LoggingModel loggingModel = new LoggingModel();
        for (FormData formData : edgeNote.getFormData()) {
            if (formData.getFormTemplateGuid().equals(fixtureFormTemplateGuid)) {
                List<EdgeFormData> fixtureFromDef = formData.getFormDef();
                loggingModel.setIdOnController(getIdOnController(fixtureFromDef));
                loggingModel.setControllerSrtId(getControllerStrId(fixtureFromDef));
            }

        }
        return loggingModel;

    }


    private String getNightRideFormVal(List<FormData> formDataList, EdgeNote edgeNote, LoggingModel loggingModel) {
        String nightRideTemplateGuid = properties.getProperty("amerescousa.night.ride.formtemplateGuid");
        logger.info("Night Ride Form TemplateGuid:"+nightRideTemplateGuid);
        int pos = formDataList.indexOf(nightRideTemplateGuid);
        if (pos != -1) {
            logger.info("Night Ride Form is Present");
            FormData formData = formDataList.get(pos);
            List<EdgeFormData> edgeFormDatas = formData.getFormDef();
            try {
                String nightRideValue = valueById(edgeFormDatas, 1);
                logger.info("Night Ride Form Val:"+nightRideValue);
                if(loggingModel.getSlvLuminaireSerialNumber() != null && nightRideValue.contains(loggingModel.getSlvLuminaireSerialNumber())){
                    logger.info("SlvLuminaireSerialNumber Val:"+loggingModel.getSlvLuminaireSerialNumber());
                    logger.info("SLV LuminaireSerialNumber and Night Ride Values are same.");
                    return null;
                }
                try {
                    SlvServerData dbSlvServerData = streetlightDao.getSlvServerData(edgeNote.getTitle());
                    if (dbSlvServerData != null && dbSlvServerData.getSerialNumber() != null) {
                        List<String> nightRideList = new ArrayList<>();
                        String serialNumber = dbSlvServerData.getSerialNumber();
                        String[] nightRideValues = serialNumber.split(",");
                        for (String nightRideVal : nightRideValues) {
                            String[] nightRideValArray = nightRideVal.split(":");
                            if (nightRideValArray.length > 1) {
                                nightRideList.add(nightRideValArray[1]);
                            }
                        }
                        boolean res = nightRideList.contains(nightRideValue);
                        loggingModel.setNigthRideSame(res);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return dateFormat(edgeNote.getCreatedDateTime()) + " :" + nightRideValue;

            } catch (NoValueException e) {

            }
        }
        return null;
    }

    private void syncNightRideFormAlone(List<FormData> formDataList, InstallMaintenanceLogModel installMaintenanceLogModel, EdgeNote edgeNote, SlvInterfaceLogEntity slvInterfaceLogEntity) {
        try {
            String nightRideKey = properties.getProperty("amerescousa.night.ride.key_for_slv");
            try {

                String nightRideTemplateGuid = properties.getProperty("amerescousa.night.ride.formtemplateGuid");
                int pos = formDataList.indexOf(nightRideTemplateGuid);
                if (pos != -1) {
                    List<Object> paramsList = new ArrayList<>();
                    String nightRideValue = getNightRideFormVal(formDataList, edgeNote, installMaintenanceLogModel);
                    if (nightRideValue != null) {
                        String idOnController = installMaintenanceLogModel.getIdOnController();
                        paramsList.add("idOnController=" + idOnController);
                        paramsList.add("controllerStrId=" + installMaintenanceLogModel.getControllerSrtId());
                        addStreetLightData(nightRideKey, nightRideValue, paramsList);
                        SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(installMaintenanceLogModel);
                        int errorCode = setDeviceValues(paramsList, slvTransactionLogs);
                        if (errorCode != 0) {
                            installMaintenanceLogModel.setErrorDetails(MessageConstants.ERROR_NIGHTRIDE_FORM_VAL);
                            installMaintenanceLogModel.setStatus(MessageConstants.ERROR);
                            slvInterfaceLogEntity.setErrorcategory(MessageConstants.SLV_VALIDATION_ERROR);
                            slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                            slvInterfaceLogEntity.setErrordetails(MessageConstants.ERROR_NIGHTRIDE_FORM_VAL);
                            return;
                        } else {
                            logger.info("Night ride value successfully updated");
                            installMaintenanceLogModel.setStatus(MessageConstants.SUCCESS);
                            slvInterfaceLogEntity.setStatus(MessageConstants.SUCCESS);
                            logger.info("Status Changed. to Success");
                        }
                    } else {
                        slvInterfaceLogEntity.setErrorcategory(MessageConstants.EDGE_VALIDATION_ERROR);
                        slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                        slvInterfaceLogEntity.setErrordetails(MessageConstants.NOVALUE_NIGHTRIDE_FORM);
                        installMaintenanceLogModel.setErrorDetails(MessageConstants.NOVALUE_NIGHTRIDE_FORM);
                        installMaintenanceLogModel.setStatus(MessageConstants.ERROR);
                    }

                }
            } catch (Exception e) {
                logger.error("Error in while getting nightRideValue's value : ", e);
                installMaintenanceLogModel.setErrorDetails("Error while syncing Night Ride value.");
                installMaintenanceLogModel.setStatus(MessageConstants.ERROR);
                slvInterfaceLogEntity.setErrorcategory(MessageConstants.SLV_VALIDATION_ERROR);
                slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                slvInterfaceLogEntity.setErrordetails("Error in while getting nightRideValue's value : " + e);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error in while getting nightRideValue's value : ", e);
            installMaintenanceLogModel.setErrorDetails("Error while syncing Night Ride value.");
            installMaintenanceLogModel.setStatus(MessageConstants.ERROR);
            slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
            slvInterfaceLogEntity.setErrorcategory(MessageConstants.EDGE_VALIDATION_ERROR);
            slvInterfaceLogEntity.setErrordetails("Error in while getting nightRideValue's value : " + e);
        }
    }

    public void processNewAction(EdgeNote edgeNote, InstallMaintenanceLogModel installMaintenanceLogModel, boolean isReSync, String utilLocId, SlvInterfaceLogEntity slvInterfaceLogEntity,WorkflowConfig workflowConfig,String notesData) {
        slvInterfaceLogEntity.setParentnoteid((edgeNote.getBaseParentNoteId() == null) ? edgeNote.getNoteGuid() : edgeNote.getBaseParentNoteId());
        logger.info("processNewAction");
        slvInterfaceLogEntity.setIdOnController(edgeNote.getTitle());
        List<FormData> formDatas = edgeNote.getFormData();

        String nightRideKey = properties.getProperty("amerescousa.night.ride.key_for_slv");
        String formatedValueNR = getNightRideFormVal(formDatas, edgeNote, installMaintenanceLogModel);

        boolean isInstallForm = false;

        for (FormData formData : formDatas) {
            edgeNoteCreatedDateTime = new SLVDates();
            noteCreatedDateTime = String.valueOf(edgeNote.getCreatedDateTime());
            logger.info("Processing Form :" + formData.getFormTemplateGuid());
            if (installMaintenanceLogModel.getUnMatchedFormGuids().contains(formData.getFormGuid())) {
                installMaintenanceLogModel.setReplace(false);
                isInstallForm = true;

                logger.info("Install  Form  is present.");
                installMaintenanceLogModel.setInstallFormPresent(true);
                List<EdgeFormData> edgeFormDatas = formData.getFormDef();
                String value = null;
                try {
                    logger.info("before Action Val");

                    try {
                        value = getActionFromConfig(edgeFormDatas, edgeNote.getTitle(), installMaintenanceLogModel, edgeNote, slvInterfaceLogEntity,workflowConfig);
                        logger.info("changed Action value is "+value);
                        logger.info("After Action Val");
                    } catch (AlreadyUsedException w) {
                        if (installMaintenanceLogModel.isNigthRideSame()) {
                            installMaintenanceLogModel.setStatus(MessageConstants.ERROR);
                            continue;
                        }
                    }

                    logger.info("Action Val is:" + value);
                    if (value != null) {
                        switch (value) {
                            case "New":
                                installMaintenanceLogModel.setActionNew(true);
                                slvInterfaceLogEntity.setSelectedAction("New");
                                processNewGroup(edgeFormDatas, edgeNote, installMaintenanceLogModel, isReSync, utilLocId, nightRideKey, formatedValueNR, slvInterfaceLogEntity);
                                installMaintenanceLogModel.setInstalledDate(edgeNote.getCreatedDateTime());
                                break;
                            case "Repairs & Outages":
                                installMaintenanceLogModel.setActionNew(false);
                                installMaintenanceLogModel.setReplace(true);
                                slvInterfaceLogEntity.setSelectedAction("Repairs & Outages");
                                repairAndOutage(edgeFormDatas, edgeNote, installMaintenanceLogModel, utilLocId, nightRideKey, formatedValueNR, formatedValueNR, slvInterfaceLogEntity);
                                installMaintenanceLogModel.setReplacedDate(edgeNote.getCreatedDateTime());
                                break;
                            case "Remove":
                                slvInterfaceLogEntity.setSelectedAction("Remove");
                                logger.info("entered remove action");
                                processRemoveAction(edgeFormDatas, utilLocId, installMaintenanceLogModel, slvInterfaceLogEntity,edgeNote,notesData);
                                break;
                            case "Other Task":
                                slvInterfaceLogEntity.setSelectedAction("Other Task");
                                // processOtherTask(edgeFormDatas, edgeNote, installMaintenanceLogModel, nightRideKey, formatedValueNR);
                                break;
                        }
                    }

                } catch (Exception e) {
                    logger.error("error in processNewAction method", e);
                    slvInterfaceLogEntity.setErrorcategory(MessageConstants.SLV_VALIDATION_ERROR);
                    slvInterfaceLogEntity.setErrordetails(MessageConstants.ACTION_NO_VAL);
                    slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                }
                if(value == null || !value.equals("Remove")){
                    // As per vish comment, If CNR Workflow has no MAC then send current date(No need to send Form Date).
                    if(!installMaintenanceLogModel.isCNRNoMAC()){
                        syncEdgeDates(installMaintenanceLogModel);
                    }

                }

                updatePromotedFormData(edgeNoteCreatedDateTime,installMaintenanceLogModel.getIdOnController(),installMaintenanceLogModel);
            }
        }

        if (!isInstallForm) {
            syncNightRideFormAlone(formDatas, installMaintenanceLogModel, edgeNote, slvInterfaceLogEntity);
        }
    }

    public String getActionFromConfig(List<EdgeFormData> edgeFormDatas, String idOnController, InstallMaintenanceLogModel loggingModel, EdgeNote edgeNote, SlvInterfaceLogEntity slvInterfaceLogEntity,WorkflowConfig workflowConfig) throws AlreadyUsedException {
        DataComparatorConfig dataComparatorConfig = new DataComparatorConfig();
        dataComparatorConfig.setFormTemplateGuid(properties.getProperty("amerescousa.edge.formtemplateGuid"));
        dataComparatorConfig.setNoteGuid(edgeNote.getNoteGuid());
        dataComparatorConfig.setRevisionFromNoteId(edgeNote.getRevisionfromNoteid() == null ? edgeNote.getNoteGuid() : edgeNote.getRevisionfromNoteid());
        List<Integer> ids = dataComparatorConfig.getIds();
        addIds(ids,workflowConfig.getAction());
        NewAction newAction = workflowConfig.getNewAction();
        ReplaceAction replaceAction = workflowConfig.getReplaceAction();
        addIds(ids,newAction.getInstallStatus());
        ids.addAll(newAction.getCompleteIds());
        ids.addAll(newAction.getCouldNotCompleteIds());
        ids.addAll(newAction.getPhotocellIds());
        addIds(ids,replaceAction.getRepairAndOutages());
        ids.addAll(replaceAction.getCIMCONIds());
        ids.addAll(replaceAction.getResolvedIds());
        ids.addAll(replaceAction.getRFIds());
        ids.addAll(replaceAction.getRNFIds());
        ids.addAll(replaceAction.getRNIds());
        ids.addAll(replaceAction.getUnableToRepairIds());
        ids.addAll(workflowConfig.getRemoveAction());
        List<SelectedWorkflow> selectedWorkflows = new ArrayList<>();
        ResponseEntity<String> responseEntity = restService.callPostMethod("/note/revision/dataAnalyzer",HttpMethod.POST,gson.toJson(dataComparatorConfig));
        String body = responseEntity.getBody();
        DataDiffResponse dataDiffResponse = gson.fromJson(body,DataDiffResponse.class);
        if(dataDiffResponse.isChanged()){
            List<DataDiffValueHolder> dataDiffValueHolders = dataDiffResponse.getDataDiff();
            for(DataDiffValueHolder dataDiffValueHolder : dataDiffValueHolders){
                String value = getValidValue(dataDiffValueHolder.getValue());
                int id = dataDiffValueHolder.getId();
                String selectedAction = getDataDiffValue(dataDiffValueHolders,workflowConfig.getAction());
                String formAction = getFormValue(edgeFormDatas,workflowConfig.getAction());
                selectedAction = selectedAction.equals("") ? formAction : selectedAction;
                selectedAction = selectedAction.equals("") ? "New" : selectedAction;
                //Remove
                if(workflowConfig.getRemoveAction().contains(id)){
                    addSelectedWorkflow(selectedWorkflows,selectedAction,"Remove","Remove","Remove",value,id);
                }
                //New
                if(newAction.getCompleteIds().contains(id)){
                    boolean isComplete = iscompleteOrPhotocell(dataDiffValueHolders,edgeFormDatas);
                    String selectedSubAction = getActionValue(dataDiffValueHolders,edgeFormDatas,newAction.getInstallStatus());
                    if(isComplete) {
                        addSelectedWorkflow(selectedWorkflows, selectedAction, selectedSubAction, "New", "Complete", value, id);
                    }else{
                        addSelectedWorkflow(selectedWorkflows,selectedAction,selectedSubAction,"New","Button Photocell Installation",value,id);
                    }
                }
                if(newAction.getCouldNotCompleteIds().contains(id)){
                    String selectedSubAction = getActionValue(dataDiffValueHolders,edgeFormDatas,newAction.getInstallStatus());
                    addSelectedWorkflow(selectedWorkflows,selectedAction,selectedSubAction,"New","Could not complete",value,id);
                }
                //Replace
                if(replaceAction.getCIMCONIds().contains(id)){
                    String selectedSubAction = getActionValue(dataDiffValueHolders,edgeFormDatas,replaceAction.getRepairAndOutages());
                    addSelectedWorkflow(selectedWorkflows,selectedAction,selectedSubAction,"Repairs & Outages","CIMCON Node Replacements JBCC Only",value,id);
                }
                if(replaceAction.getResolvedIds().contains(id)){
                    String selectedSubAction = getActionValue(dataDiffValueHolders,edgeFormDatas,replaceAction.getRepairAndOutages());
                    addSelectedWorkflow(selectedWorkflows,selectedAction,selectedSubAction,"Repairs & Outages","Resolved (Other)",value,id);
                }
                if(replaceAction.getRFIds().contains(id)){
                    String selectedSubAction = getActionValue(dataDiffValueHolders,edgeFormDatas,replaceAction.getRepairAndOutages());
                    addSelectedWorkflow(selectedWorkflows,selectedAction,selectedSubAction,"Repairs & Outages","Replace Fixture only",value,id);
                }
                if(replaceAction.getRNFIds().contains(id)){
                    String selectedSubAction = getActionValue(dataDiffValueHolders,edgeFormDatas,replaceAction.getRepairAndOutages());
                    selectedSubAction = selectedSubAction.equals("") ? "Replace Node and Fixture" : selectedSubAction;
                    addSelectedWorkflow(selectedWorkflows,selectedAction,selectedSubAction,"Repairs & Outages","Replace Node and Fixture",value,id);
                }
                if(replaceAction.getRNIds().contains(id)){
                    String selectedSubAction = getActionValue(dataDiffValueHolders,edgeFormDatas,replaceAction.getRepairAndOutages());
                    addSelectedWorkflow(selectedWorkflows,selectedAction,selectedSubAction,"Repairs & Outages","Replace Node only",value,id);
                }
                if(replaceAction.getUnableToRepairIds().contains(id)){
                    String selectedSubAction = getActionValue(dataDiffValueHolders,edgeFormDatas,replaceAction.getRepairAndOutages());
                    addSelectedWorkflow(selectedWorkflows,selectedAction,selectedSubAction,"Repairs & Outages","Unable to Repair(CDOT Issue)",value,id);
                }
            }
            logger.info("Selected workflows: "+gson.toJson(selectedWorkflows));
            if(selectedWorkflows.size() == 0){
                String changedAction = getDataDiffValue(dataDiffValueHolders,workflowConfig.getAction());
                String changedNewSubAction = getDataDiffValue(dataDiffValueHolders,newAction.getInstallStatus());
                String changedReplaceSubAction = getDataDiffValue(dataDiffValueHolders,replaceAction.getRepairAndOutages());
                if(changedAction.equals("") && changedNewSubAction.equals("") && changedReplaceSubAction.equals("")) {
                    return "New";
                }else{
                    if(changedAction.equals("")){
                        changedAction = getFormValue(edgeFormDatas,workflowConfig.getAction());
                    }
                    if(changedAction.equals("New")){
                        changedNewSubAction = getFormValue(edgeFormDatas,newAction.getInstallStatus());
                        SelectedWorkflow selectedWorkflow = getSelectWorkflow(changedAction,changedNewSubAction);
                        processSelectedWorkflow(selectedWorkflow,loggingModel,edgeFormDatas,edgeNote,slvInterfaceLogEntity,idOnController);
                    }else if(changedAction.equals("Repairs & Outages") || changedAction.equals("Repairs \\u0026 Outages")){
                        SelectedWorkflow selectedWorkflow = getSelectWorkflow(changedAction,changedReplaceSubAction);
                        processSelectedWorkflow(selectedWorkflow,loggingModel,edgeFormDatas,edgeNote,slvInterfaceLogEntity,idOnController);
                    }else if(changedAction.equals("Remove")){
                        SelectedWorkflow selectedWorkflow  = getSelectWorkflow("Remove","Remove");
                        processSelectedWorkflow(selectedWorkflow,loggingModel,edgeFormDatas,edgeNote,slvInterfaceLogEntity,idOnController);
                    }else{
                        return "New";
                    }
                }
            }else if(selectedWorkflows.size() == 1){
                SelectedWorkflow selectedWorkflow = selectedWorkflows.get(0);
                return processSelectedWorkflow(selectedWorkflow,loggingModel,edgeFormDatas,edgeNote,slvInterfaceLogEntity,idOnController);
            }else{
                for(SelectedWorkflow selectedWorkflow : selectedWorkflows){
                    if(selectedWorkflow.getActualSubaction().equals(selectedWorkflow.getSelectedSubAction())){
                        logger.info("selected workflow is: "+gson.toJson(selectedWorkflow));
                        return processSelectedWorkflow(selectedWorkflow,loggingModel,edgeFormDatas,edgeNote,slvInterfaceLogEntity,idOnController);
                    }
                }
            }
        }else{
            return "New";
        }
        return "New";
    }

    private SelectedWorkflow getSelectWorkflow(String changedAction,String changedSubAction){
        SelectedWorkflow selectedWorkflow = new SelectedWorkflow();
        selectedWorkflow.setSelectedAction(changedAction);
        selectedWorkflow.setSelectedSubAction(changedSubAction);
        return selectedWorkflow;
    }

    private boolean iscompleteOrPhotocell(List<DataDiffValueHolder> dataDiffValueHolders,List<EdgeFormData> edgeFormDatas){
        boolean isComplete = true;
        String installStatusValue = getValidValue(getDataDiffValue(dataDiffValueHolders,22));
        if(installStatusValue.equals("")){
            installStatusValue = getFormValue(edgeFormDatas,22);
        }
        if(installStatusValue.equals("Button Photocell Installation")){
            isComplete = false;
        }
        return isComplete;
    }

    private String getActionValue(List<DataDiffValueHolder> dataDiffValueHolders,List<EdgeFormData> edgeFormDatas,int compId){
        String action = getDataDiffValue(dataDiffValueHolders,compId);
        action = action.equals("") ? getFormValue(edgeFormDatas,compId) : action;
        return action;
    }

    private String processSelectedWorkflow(SelectedWorkflow selectedWorkflow,InstallMaintenanceLogModel loggingModel,List<EdgeFormData> edgeFormDatas,EdgeNote edgeNote,SlvInterfaceLogEntity slvInterfaceLogEntity,String idOnController)  throws AlreadyUsedException{
        String action = selectedWorkflow.getSelectedAction();
        String subAction = selectedWorkflow.getSelectedSubAction();
        if(action.equals("Remove")){
            return "Remove";
        }else if(action.equals("Repairs & Outages") || action.equals("Repairs \\u0026 Outages")){
            if(subAction.equals("Unable to Repair(CDOT Issue)")){
                loggingModel.setRepairsOption("Unable to Repair(CDOT Issue)");
                return "Repairs & Outages";
            }else if (subAction.contains("CIMCON Node Replacements")) {
                loggingModel.setRepairsOption("CNR");
                return "Repairs & Outages";
            }else if(subAction.equals("Resolved (Other)")){
                loggingModel.setRepairsOption("Resolved (Other)");
                return "Repairs & Outages";
            }else if(subAction.equals("Replace Fixture only")){
                String newFixtureQrScanValue = null;
                try {
                    newFixtureQrScanValue = valueById(edgeFormDatas, 39);
                    checkFixtureQrScan(newFixtureQrScanValue, edgeNote, loggingModel, slvInterfaceLogEntity);
                } catch (NoValueException e) {
                    if (newFixtureQrScanValue != null) {
                        if (loggingModel.isFixtureQRSame()) {
                            throw new AlreadyUsedException("QR Scan Already Used");
                        }
                        loggingModel.setRepairsOption("Replace Fixture only");
                        return "Repairs & Outages";
                    }
                } catch (InValidBarCodeException e) {
                    // Replace Fixture Only, If QR Code is invalid, then no need to process.
                    loggingModel.setFixtureQRSame(true);
                    // No need to worry, crew may be node replaced.
                }
                if (loggingModel.isFixtureQRSame()) {
                    slvInterfaceLogEntity.setErrorcategory(MessageConstants.SLV_VALIDATION_ERROR);
                    slvInterfaceLogEntity.setErrordetails("Given fixtureQrScan Already exist in slv server");
                }
                if (newFixtureQrScanValue != null) {
                    if (loggingModel.isFixtureQRSame()) {
                        throw new AlreadyUsedException("QR Scan Already Used");
                    }
                    loggingModel.setRepairsOption("Replace Fixture only");
                    return "Repairs & Outages";
                }
                throw new AlreadyUsedException("Fixture qrscan already used");
            }else if(subAction.equals("Replace Node only")){
                try {
                    String newNodeMacAddress = valueById(edgeFormDatas, 30);
                    checkMacAddressExists(newNodeMacAddress, idOnController, null, null, loggingModel, slvInterfaceLogEntity);
                    loggingModel.setRepairsOption("Replace Node only");
                    return "Repairs & Outages";
                }catch (Exception e){
                    logger.error("Error while processing replace node only in processSelectedWorkflow: ",e);
                }
            }else if(subAction.equals("Replace Node and Fixture")){
                loggingModel.setMacAddressUsed(false);
                loggingModel.setFixtureQRSame(false);
                try {
                    validateValue(edgeFormDatas, idOnController, loggingModel, edgeNote, 26, 38, slvInterfaceLogEntity);
                    loggingModel.setRepairsOption("Replace Node and Fixture");
                    return "Repairs & Outages";
                } catch (AlreadyUsedException e) {
                    throw new AlreadyUsedException(e.getMessage());
                } catch (NoValueException e) {

                }
            }
        }else if(action.equals("New")){
            if(subAction.equals("Could not complete")){
                DeviceAttributes deviceAttributes = getDeviceValues(loggingModel);
                String installStatus = properties.getProperty("could_note_complete_install_status");
                if (deviceAttributes != null && deviceAttributes.getInstallStatus() != null && deviceAttributes.getInstallStatus().equals(installStatus)) {
                    logger.info("Current edge and SLV Install Status is same (Could not be installed)");
                    loggingModel.setCouldNotComplete(true);
                }
                return "New";
            }else if(subAction.equals("Button Photocell Installation")){
                DeviceAttributes deviceAttributes = getDeviceValues(loggingModel);
                loggingModel.setButtonPhotoCell(true);
                if (deviceAttributes != null && deviceAttributes.getInstallStatus() != null && (deviceAttributes.getInstallStatus().equals(InstallStatus.Verified.getValue()) || deviceAttributes.getInstallStatus().equals(InstallStatus.Photocell_Only.getValue()))) {
                    loggingModel.setFixtureQRSame(true);
                }
                return "New";
            }else{//complete
                try {
                    validateValue(edgeFormDatas, idOnController, loggingModel, edgeNote, 19, 20, slvInterfaceLogEntity);
                    return "New";
                } catch (AlreadyUsedException e) {
                    throw new AlreadyUsedException(e.getMessage());
                } catch (NoValueException e) {
                    return "New";
                }
            }
        }
        return "New";
    }

    private String getDataDiffValue(List<DataDiffValueHolder> dataDiffValueHolders,int componentId){
        DataDiffValueHolder dataDiffValueHolder = new DataDiffValueHolder();
        dataDiffValueHolder.setId(componentId);
        int pos  = dataDiffValueHolders.indexOf(dataDiffValueHolder);
        if(pos != -1){
            String value = dataDiffValueHolders.get(pos).getValue();
            return getValidValue(value);
        }
        return "";
    }

    private String getFormValue(List<EdgeFormData> edgeFormDatas,int componentId){
        for(EdgeFormData edgeFormData : edgeFormDatas){
            if(edgeFormData.getId() == componentId){
                return getValidValue(edgeFormData.getValue());
            }
        }
        return "";
    }
    private String getValidValue(String value){
        value = value == null ? "" : value;
        value = value.contains("#") ? value.split("#",-1)[1] : value;
        value = value.contains("null") ? "" : value;
        return value;
    }
    private void addSelectedWorkflow(List<SelectedWorkflow> list,String selectedAction,String selectedSubAction,String actualAction,String actualSubAction,String value,int id){
        SelectedWorkflow selectedWorkflow = new SelectedWorkflow();
        selectedWorkflow.setSelectedAction(selectedAction);
        selectedWorkflow.setSelectedSubAction(selectedSubAction);
        selectedWorkflow.setActualAction(actualAction);
        selectedWorkflow.setActualSubaction(actualSubAction);
        selectedWorkflow.setId(id);
        selectedWorkflow.setValue(value);
        list.add(selectedWorkflow);
    }

    private void addIds(List<Integer> list, int id){
        list.add(id);
    }

    private String getAction(List<EdgeFormData> edgeFormDatas, String idOnController, InstallMaintenanceLogModel loggingModel, EdgeNote edgeNote, SlvInterfaceLogEntity slvInterfaceLogEntity) throws AlreadyUsedException {
        try {

            loadDateValFromEdge(edgeFormDatas,loggingModel);
            String value = value(edgeFormDatas, "Action");
            if (value.equals("Repairs & Outages")) {
                String repairsOutagesValue = null;
                try {
                    repairsOutagesValue = valueById(edgeFormDatas, 24);
                    if (repairsOutagesValue.equals("Power Issue")) {
                        loggingModel.setRepairsOption("Power Issue");
                        return "Repairs & Outages";
                    } else if (repairsOutagesValue.equals("Unable to Repair(CDOT Issue)")) {
                        loggingModel.setRepairsOption("Unable to Repair(CDOT Issue)");
                        return "Repairs & Outages";
                    }
                    else if (repairsOutagesValue.contains("CIMCON Node Replacements")) {
                        loggingModel.setRepairsOption("CNR");
                        return "Repairs & Outages";
                    }else if(repairsOutagesValue.equals("Resolved (Other)")){
                        loggingModel.setRepairsOption("Resolved (Other)");
                        return "Repairs & Outages";
                    }


                } catch (NoValueException e) {
                    loggingModel.setStatus(MessageConstants.ERROR);
                    loggingModel.setErrorDetails("Repairs & Outages options are not selected.");
                }

            } else if (value.equals("Remove")) {
                return value;
            }
        } catch (NoValueException e) {
            e.printStackTrace();
        }


        // Replace Fixture Only
        String newFixtureQrScanValue = null;
        try {
            newFixtureQrScanValue = valueById(edgeFormDatas, 39);
            checkFixtureQrScan(newFixtureQrScanValue, edgeNote, loggingModel, slvInterfaceLogEntity);
        } catch (NoValueException e) {

        } catch (InValidBarCodeException e) {
            // Replace Fixture Only, If QR Code is invalid, then no need to process.
            loggingModel.setFixtureQRSame(true);
            // No need to worry, crew may be node replaced.
        }
        if (loggingModel.isFixtureQRSame()) {
            slvInterfaceLogEntity.setErrorcategory(MessageConstants.SLV_VALIDATION_ERROR);
            slvInterfaceLogEntity.setErrordetails("Given fixtureQrScan Already exist in slv server");
        }
        //Check Replace Node only option
        try {
            String newNodeMacAddress = valueById(edgeFormDatas, 30);
            checkMacAddressExists(newNodeMacAddress, idOnController, null, null, loggingModel, slvInterfaceLogEntity);
            loggingModel.setRepairsOption("Replace Node only");
            return "Repairs & Outages";
        } catch (NoValueException e) {
            // Crew may be replaced Fixture QR value.
            if (newFixtureQrScanValue != null) {
                if (loggingModel.isFixtureQRSame()) {
                    throw new AlreadyUsedException("QR Scan Already Used");
                }
                loggingModel.setRepairsOption("Replace Fixture only");
                return "Repairs & Outages";
            }
        } catch (Exception e) {
            if (newFixtureQrScanValue != null) {
                if (loggingModel.isFixtureQRSame()) {
                    throw new AlreadyUsedException("QR Scan Already Used");
                }
                loggingModel.setRepairsOption("Replace Fixture only");
                return "Repairs & Outages";
            }
            throw new AlreadyUsedException(e.getMessage());

        }

        loggingModel.setMacAddressUsed(false);
        loggingModel.setFixtureQRSame(false);

        try {
            validateValue(edgeFormDatas, idOnController, loggingModel, edgeNote, 26, 38, slvInterfaceLogEntity);
            loggingModel.setRepairsOption("Replace Node and Fixture");
            return "Repairs & Outages";
        } catch (AlreadyUsedException e) {
            throw new AlreadyUsedException(e.getMessage());
        } catch (NoValueException e) {

        }


        // Get Install status
        String installStatusValue = null;
        try {
            installStatusValue = valueById(edgeFormDatas, 22);
            logger.info("installStatus Val" + installStatusValue);
        } catch (NoValueException e) {
            logger.error("Error in while getting installStatusValue", e);
        }


        if (installStatusValue != null && installStatusValue.equals("Button Photocell Installation")) {
            DeviceAttributes deviceAttributes = getDeviceValues(loggingModel);
            loggingModel.setButtonPhotoCell(true);
            if (deviceAttributes != null && deviceAttributes.getInstallStatus() != null && (deviceAttributes.getInstallStatus().equals(InstallStatus.Verified.getValue()) || deviceAttributes.getInstallStatus().equals(InstallStatus.Photocell_Only.getValue()))) {
                loggingModel.setFixtureQRSame(true);
            }
        } else  if(installStatusValue != null && installStatusValue.equals("Could not complete")){
            DeviceAttributes deviceAttributes = getDeviceValues(loggingModel);
            String installStatus = properties.getProperty("could_note_complete_install_status");
            if (deviceAttributes != null && deviceAttributes.getInstallStatus() != null && deviceAttributes.getInstallStatus().equals(installStatus)) {
                logger.info("Current edge and SLV Install Status is same (Could not be installed)");
                loggingModel.setCouldNotComplete(true);
            }
            return "New";
        }

        try {
            validateValue(edgeFormDatas, idOnController, loggingModel, edgeNote, 19, 20, slvInterfaceLogEntity);
            return "New";
        } catch (AlreadyUsedException e) {
            throw new AlreadyUsedException(e.getMessage());
        } catch (NoValueException e) {
            return "New";
        }
    }


    private void validateValue(List<EdgeFormData> edgeFormDatas, String idOnController, InstallMaintenanceLogModel loggingModel, EdgeNote edgeNote, int macAddressId, int qrScanId, SlvInterfaceLogEntity slvInterfaceLogEntity) throws AlreadyUsedException, NoValueException {
        //Replace Node and Fixture
        String newNodeMacAddress = null;
        try {
            newNodeMacAddress = valueById(edgeFormDatas, macAddressId);
        } catch (NoValueException e) {
            loggingModel.setMacAddressUsed(true);
        }


        String fixerQrScanValue = null;
        try {
            fixerQrScanValue = valueById(edgeFormDatas, qrScanId);
        } catch (NoValueException e) {
            loggingModel.setFixtureQRSame(true);
        }

        if (newNodeMacAddress == null && fixerQrScanValue == null) {
            loggingModel.setMacAddressUsed(false);
            loggingModel.setFixtureQRSame(false);
            throw new NoValueException("MAC Address and Fixture QR Scan value is Empty");
        }

        if (newNodeMacAddress != null && !newNodeMacAddress.startsWith("00") && (fixerQrScanValue != null && fixerQrScanValue.startsWith("00"))) {
            String temp = newNodeMacAddress;
            newNodeMacAddress = fixerQrScanValue;
            fixerQrScanValue = temp;
        }


        //Suppose fixtureqrscan value has mac address but MAC Address is empty
        if (newNodeMacAddress == null && (fixerQrScanValue != null && fixerQrScanValue.startsWith("00"))) {
            newNodeMacAddress = fixerQrScanValue;
            fixerQrScanValue = null;
            loggingModel.setFixtureQRSame(true);
        }

        if (newNodeMacAddress != null) {
            try {
                checkMacAddressExists(newNodeMacAddress, idOnController, null, null, loggingModel, slvInterfaceLogEntity);
            } catch (QRCodeAlreadyUsedException e) {
                // No need to worry, Fixture QR Scan may be differ. So let's continue.
            } catch (Exception e) {
                // No need to worry, Fixture QR Scan may be differ. So let's continue.
            }

        }

        if (fixerQrScanValue != null && !loggingModel.isButtonPhotoCell()) {
            try {
                checkFixtureQrScan(fixerQrScanValue, edgeNote, loggingModel, slvInterfaceLogEntity);
            } catch (InValidBarCodeException e) {
                loggingModel.setFixtureQRSame(true);
                // No need to process,bcs its invalid.
                if (loggingModel.isMacAddressUsed()) {
                    throw new AlreadyUsedException(e.getMessage());
                }
            }
        }

        if (loggingModel.isMacAddressUsed() && loggingModel.isFixtureQRSame()) {
            slvInterfaceLogEntity.setErrordetails("MAC Address and QR Scan Already Used.");
            slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
            throw new AlreadyUsedException("MAC Address and QR Scan Already Used.");
        }


    }


    private void checkFixtureQrScan(String fixtureQrScan, EdgeNote edgeNote, InstallMaintenanceLogModel loggingModel, SlvInterfaceLogEntity slvInterfaceLogEntity) throws InValidBarCodeException {
        List<Object> paramsList = new ArrayList<>();
        SlvServerData slvServerData = new SlvServerData();
        try {
            // check given macaddress exist or not in edge_all_fix table
            boolean isExistFixture = connectionDAO.isExistFixture(edgeNote.getTitle(), fixtureQrScan);
            logger.info("given fixture idoncontroller :" + edgeNote.getTitle());
            logger.info("Given fixture fixturequrscan:" + fixtureQrScan);
            if (isExistFixture) {
                slvInterfaceLogEntity.setErrorcategory(MessageConstants.EDGE_VALIDATION_ERROR);
                slvInterfaceLogEntity.setErrordetails("Fixture QR is present edge_all_fix table.");
                logger.info("Fixture QR is present edge_all_fix table.");
                loggingModel.setFixtureQRSame(true);
                return;
            }
            logger.info("Fixture QR Scan Validation Starts.");
            buildFixtureStreetLightData(fixtureQrScan, paramsList, edgeNote, slvServerData, loggingModel);
            paramsList.clear();
            slvServerData.setIdOnController(edgeNote.getTitle());

            String mainUrl = properties.getProperty("streetlight.slv.url.main");
            String updateDeviceValues = properties.getProperty("streetlight.slv.url.search.device");
            String url = mainUrl + updateDeviceValues;
            paramsList.add("attribute=idOnController");
            paramsList.add("value=" + edgeNote.getTitle().trim());

            addFixtureQrScanData("luminaire.brand", slvServerData.getLuminaireBrand(), paramsList);
            addFixtureQrScanData("device.luminaire.partnumber", slvServerData.getLuminairePartNumber(), paramsList);
            addFixtureQrScanData("luminaire.model", slvServerData.getLuminaireModel(), paramsList);
            addFixtureQrScanData("device.luminaire.manufacturedate", slvServerData.getLuminaireManufacturedate(), paramsList);
            addFixtureQrScanData("device.luminaire.driverpartnumber", slvServerData.getDriverPartNumber(), paramsList);
            addFixtureQrScanData("luminaire.colorcode", slvServerData.getColorCode(), paramsList);
            paramsList.add("operator=eq-i");
            paramsList.add("recurse=true");
            paramsList.add("ser=json");
            String params = StringUtils.join(paramsList, "&");
            url = url + "?" + params;
            ResponseEntity<String> response = restService.getRequest(url, true, null);
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseString = response.getBody();
                logger.info("-------QR Address Check Address----------");
                logger.info(responseString);
                logger.info("-------QR Address Check End----------");

                DeviceMacAddress deviceMacAddress = gson.fromJson(responseString, DeviceMacAddress.class);
                List<Value> values = deviceMacAddress.getValue();
                StringBuilder stringBuilder = new StringBuilder();
                if (values == null || values.size() == 0) {
                    logger.info("Fixture QR Not present.");
                    loggingModel.setFixtureQRSame(false);
                } else {
                    logger.info("Fixture QR is present.");
                    loggingModel.setFixtureQRSame(true);
                }
            }else{
                logger.error(response.getStatusCode().toString());
                logger.error(response.getBody());
            }


            logger.info("Fixture QR Scan Validation End.");
        } catch (InValidBarCodeException e) {
            loggingModel.setFixtureQRSame(true);
            throw new InValidBarCodeException(e.getMessage());
        } catch (Exception e) {
            loggingModel.setFixtureQRSame(true);
            throw new InValidBarCodeException(e.getMessage());

        }

    }

    /*private void checkFixtureQrScan(String fixtureQrScan, EdgeNote edgeNote, LoggingModel loggingModel) throws InValidBarCodeException {
        List<Object> paramsList = new ArrayList<>();
        SlvServerData slvServerData = new SlvServerData();
        try {
            buildFixtureStreetLightData(fixtureQrScan, paramsList, edgeNote, slvServerData);
            slvServerData.setIdOnController(edgeNote.getTitle());
            SlvServerData dbSlvServerData = streetlightDao.getSlvServerData(edgeNote.getTitle());
            if (slvServerData.equals(dbSlvServerData)) {
                loggingModel.setFixtureQRSame(true);
            }
        } catch (InValidBarCodeException e) {
            throw new InValidBarCodeException(e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }*/


    private int sync2SlvInstallStatus(String idOnController, String controllerStrIdValue, InstallMaintenanceLogModel loggingModel, String nightRideKey, String nightRideValue) {
        List<Object> paramsList = new ArrayList<>();
        String installStatus = properties.getProperty("could_note_complete_install_status");
        paramsList.add("idOnController=" + idOnController);
        paramsList.add("controllerStrId=" + controllerStrIdValue);
        if (nightRideValue != null) {
            addStreetLightData(nightRideKey, nightRideValue, paramsList);
        }
        addStreetLightData("installStatus", installStatus, paramsList);
        SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(loggingModel);
        int errorCode = setDeviceValues(paramsList, slvTransactionLogs);
        logger.info("Error code" + errorCode);
        return errorCode;
    }

    private void sync2Slv(String macAddress, String fixerQrScanValue, EdgeNote edgeNote, InstallMaintenanceLogModel loggingModel, String idOnController, String controllerStrIdValue, String utilLocId, boolean isNew, String nightRideKey, String nightRideValue, SlvInterfaceLogEntity slvInterfaceLogEntity) {
        try {

            List<Object> paramsList = new ArrayList<>();
            loggingModel.setPowerAdded(false);
            paramsList.add("idOnController=" + idOnController);
            paramsList.add("controllerStrId=" + controllerStrIdValue);
            SlvServerData slvServerData = new SlvServerData();

            logger.info("Atlasphysicalpage in SLV:"+loggingModel.getAtlasPhysicalPage());
            logger.info("IsDroppedPinWorkflow:"+loggingModel.isDroppedPinWorkflow());
            logger.info("Edge NoteBook:"+edgeNote.getEdgeNotebook());
            if(loggingModel.isDroppedPinWorkflow() && loggingModel.getAtlasPhysicalPage() == null && edgeNote.getEdgeNotebook() != null && edgeNote.getEdgeNotebook().getNotebookName() != null){
                logger.info("atlasphysicalpage is empty.");
                addStreetLightData("location.atlasphysicalpage", edgeNote.getEdgeNotebook().getNotebookName(), paramsList);
            }

            if (fixerQrScanValue != null && !loggingModel.isFixtureQRSame() && fixerQrScanValue.startsWith("Existing")) {
                loggingModel.setNodeOnly(true);
            }
            addOtherParams(edgeNote, paramsList, idOnController, utilLocId, isNew, fixerQrScanValue, macAddress, loggingModel);



            if (fixerQrScanValue != null && !loggingModel.isFixtureQRSame()) {
                buildFixtureStreetLightData(fixerQrScanValue, paramsList, edgeNote, slvServerData, loggingModel);//update fixer qrscan value
            }

            logger.info("Night Ride Val in Set Device"+nightRideValue);
            if (nightRideValue != null) {
                addStreetLightData(nightRideKey, nightRideValue, paramsList);
            }
            boolean isButtonPhotoCell = loggingModel.isButtonPhotoCell();
            boolean isNodeOnly = loggingModel.isNodeOnly();
            if (isButtonPhotoCell || isNodeOnly) {
                // If its bulk import, then we need to send only Form Date value
                if(!loggingModel.isBulkImport()){
                    addStreetLightData("install.date", dateFormat(edgeNote.getCreatedDateTime()), paramsList);
                }

            }
            if (macAddress != null && !macAddress.trim().isEmpty() && !loggingModel.isMacAddressUsed()) {
                if(loggingModel.getSlvMacaddress() != null && !loggingModel.isReSync()){
                    try {
                        SLVTransactionLogs slvTransactionLogsTemp = getSLVTransactionLogs(loggingModel);
                        replaceOLC(loggingModel.getControllerSrtId(), loggingModel.getIdOnController(), "", slvTransactionLogsTemp, slvInterfaceLogEntity,loggingModel.getAtlasPhysicalPage(),loggingModel,edgeNote);
                    } catch (Exception e) {
                        logger.error("Error in empty replaceOLC",e);
                    }
                }

                boolean isNodeDatePresent = isNodeDatePresent(idOnController);
                if (!isNodeDatePresent && !isButtonPhotoCell) {
                    // If its bulk import, then we need to send only Form Date value
                    if(!loggingModel.isBulkImport()){
                        addStreetLightData("cslp.node.install.date", dateFormat(edgeNote.getCreatedDateTime()), paramsList);
                    }

                }
                if (!isButtonPhotoCell && !isNodeOnly) {
                    // If its bulk import, then we need to send only Form Date value
                    if(!loggingModel.isBulkImport()){
                        addStreetLightData("install.date", dateFormat(edgeNote.getCreatedDateTime()), paramsList);
                    }

                }
                addStreetLightData("MacAddress", macAddress, paramsList);
            }
            setEdgeDates(loggingModel,paramsList);
            SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(loggingModel);
            int errorCode = setDeviceValues(paramsList, slvTransactionLogs);
            logger.info("Error code" + errorCode);
            if (errorCode != 0) {
                loggingModel.setErrorDetails(MessageConstants.ERROR_UPDATE_DEVICE_VAL);
                loggingModel.setStatus(MessageConstants.ERROR);
                slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                slvInterfaceLogEntity.setErrorcategory(MessageConstants.SLV_VALIDATION_ERROR);
                slvInterfaceLogEntity.setErrordetails(MessageConstants.ERROR_UPDATE_DEVICE_VAL);
                return;
            } else {
                slvInterfaceLogEntity.setSetDevice(MessageConstants.SETDEVICE);
                slvInterfaceLogEntity.setStatus(MessageConstants.SUCCESS);
                if (fixerQrScanValue != null && !fixerQrScanValue.trim().isEmpty()) {
                    createEdgeAllFixture(idOnController, fixerQrScanValue);
                }
                createAllSLVDate(loggingModel.getDatesHolder().getSyncEdgeDates(),idOnController);
                logger.info("Replace OLC called");
                // replace OlC
                System.out.println("New :" + isNew + " \nmacAddress :" + macAddress);
                logger.info("New :" + isNew + " \nmacAddress :" + macAddress);
                if (macAddress == null || macAddress.isEmpty()) {
                    loggingModel.setStatus(MessageConstants.SUCCESS);
                    removeSwapPromotedData(loggingModel.getIdOnController());
                    return;
                } else {
                    removeSwapPromotedData(loggingModel.getIdOnController());
                    if (!loggingModel.isMacAddressUsed()) {
                        slvTransactionLogs = getSLVTransactionLogs(loggingModel);
                        replaceOLC(controllerStrIdValue, idOnController, macAddress, slvTransactionLogs, slvInterfaceLogEntity,loggingModel.getAtlasPhysicalPage(),loggingModel,edgeNote);// insert mac address
                    }

                }
                logger.info("Replace OLC End");
                loggingModel.setStatus(MessageConstants.SUCCESS);
                slvInterfaceLogEntity.setStatus(MessageConstants.SUCCESS);

                logger.info("Status Changed. to Success");
            }

        } catch (InValidBarCodeException e) {
            logger.error("Error in processNewGroup", e);
            loggingModel.setStatus(MessageConstants.ERROR);
            slvInterfaceLogEntity.setErrorcategory(MessageConstants.SLV_VALIDATION_ERROR);
            loggingModel.setErrorDetails(e.getMessage());
            slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
            slvInterfaceLogEntity.setErrordetails(e.getMessage());

        } catch (Exception e) {
            slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
            slvInterfaceLogEntity.setErrorcategory(MessageConstants.SLV_VALIDATION_ERROR);
            slvInterfaceLogEntity.setErrordetails(e.getMessage());
            logger.error("Error in processNewGroup", e);
            loggingModel.setStatus(MessageConstants.ERROR);
            loggingModel.setErrorDetails(e.getMessage());

        }
    }



    private String getIdOnController(List<EdgeFormData> fixtureFromDef) {
        // Get IdOnController value
        String idOnController = null;
        try {
            idOnController = value(fixtureFromDef, properties.getProperty("edge.fortemplate.fixture.label.idoncntrl"));
        } catch (NoValueException e) {
        }
        return idOnController;

    }


    private String getControllerStrId(List<EdgeFormData> fixtureFromDef) {
        // Get ControllerStdId value
        String controllerStrId = null;
        try {
            controllerStrId = value(fixtureFromDef, properties.getProperty("edge.fortemplate.fixture.label.cnrlstrid"));
        } catch (NoValueException e) {
        }
        return controllerStrId;
    }

    public void processOtherTask(List<EdgeFormData> edgeFormDatas, EdgeNote edgeNote, InstallMaintenanceLogModel loggingModel, String nightRideKey, String nightRideValue, SlvInterfaceLogEntity slvInterfaceLogEntity) {
        logger.info("Other task value processed");
        String Key = properties.getProperty("amerescousa.night.ride.key_for_slv");
        logger.info("key :" + Key);
        List<Object> paramsList = new ArrayList<>();
        String cdotIssue = null;
        try {
            cdotIssue = valueById(edgeFormDatas, 106);
            cdotIssue =  getUnableRepairComment(cdotIssue,edgeFormDatas);
            logger.info("cdotIssue's issue Value :" + cdotIssue);
        } catch (NoValueException e) {
            logger.error("Error in while getting cdotIssue's issue", e);
            loggingModel.setErrorDetails("Value is not selected");
            loggingModel.setStatus(MessageConstants.ERROR);
            slvInterfaceLogEntity.setErrorcategory(MessageConstants.EDGE_VALIDATION_ERROR);
            slvInterfaceLogEntity.setErrordetails("Error in while getting cdotIssue's issue:" + e);
            slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
            return;
        }
        logger.info("cdotIssue :" + cdotIssue);
        String controllerStarId = loggingModel.getControllerSrtId();
        String idOnController = loggingModel.getIdOnController();
        paramsList.add("idOnController=" + idOnController);
        paramsList.add("controllerStrId=" + controllerStarId);


        try{
            checkLuminaireSerialNumberSync(cdotIssue,edgeNote.getTitle(),loggingModel);
        }catch (SerialNumberSyncedException e){
            logger.info("SLV Install Status is already Could not Complete and Skipped Fixture Reason Value also already Synced");
            slvInterfaceLogEntity.setErrordetails("SLV Install Status is already Could not Complete and nightRideValue Value is Empty");
            slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
            return;
        }


        if (nightRideValue != null) {
            nightRideValue = nightRideValue + "," + cdotIssue;
            nightRideValue = addUserToLuminaireSerialNumber(nightRideValue,edgeNote.getCreatedBy());
            addStreetLightData(nightRideKey, nightRideValue, paramsList);
        } else {

            if(loggingModel.getSlvLuminaireSerialNumber() != null && loggingModel.getSlvLuminaireSerialNumber().contains(cdotIssue)){
                logger.info("SLV Install Status is already Could not Complete and cdotIssue Value also already Synced");
                slvInterfaceLogEntity.setErrordetails("SLV Install Status is already Could not Complete and cdotIssue Value also already Synced");
                slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                return;
            }

            String formatedValue = dateFormat(edgeNote.getCreatedDateTime()) + " :" + cdotIssue;
            formatedValue = addUserToLuminaireSerialNumber(formatedValue,edgeNote.getCreatedBy());
            addStreetLightData(Key, formatedValue, paramsList);
            addEdgeAllSerialNumber(formatedValue,idOnController);
        }
        SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(loggingModel);
        int errorCode = setDeviceValues(paramsList, slvTransactionLogs);
        if (errorCode != 0) {
            logger.info("CDOT issue value serDevice error");
            loggingModel.setErrorDetails(MessageConstants.ERROR_OTHERTASK_DEVICE_VAL);
            loggingModel.setStatus(MessageConstants.ERROR);
            return;
        } else {
            slvInterfaceLogEntity.setStatus(MessageConstants.SUCCESS);
            logger.info("OtherTask's value updated successfully in SLV");
            loggingModel.setStatus(MessageConstants.SUCCESS);
            logger.info("Status Changed. to Success");
        }
    }


    private String getSkippedReason(String skippedFixtureReasonVal,List<EdgeFormData> edgeFormDatas){
        if(skippedFixtureReasonVal.equals("Other - Add note")){
            try{
                skippedFixtureReasonVal = valueById(edgeFormDatas, 42);
            }catch (Exception e){
                logger.error("Error in getSkippedReason",e);
            }
        }
        return skippedFixtureReasonVal;
    }


    private String getUnableRepairComment(String unableRepair,List<EdgeFormData> edgeFormDatas){
        if(unableRepair.equals("Other (Comment)")){
            try{
                unableRepair = valueById(edgeFormDatas, 121);
            }catch (Exception e){
                logger.error("Error in getUnableRepairComment",e);
            }
        }
        return unableRepair;
    }

    private void processNewGroup(List<EdgeFormData> edgeFormDatas, EdgeNote edgeNote, InstallMaintenanceLogModel loggingModel, boolean isResync, String utilLocId, String nightRideKey, String nightRideValue, SlvInterfaceLogEntity slvInterfaceLogEntity) {
        try {

            // Get Install status
            String installStatusValue = null;
            try {
                installStatusValue = valueById(edgeFormDatas, 22);
                slvInterfaceLogEntity.setInstallStatus(installStatusValue);
                logger.info("installStatus Val" + installStatusValue);
            } catch (NoValueException e) {
                logger.error("Error in while getting installStatusValue", e);
            }
            if (installStatusValue != null && installStatusValue.equals("Could not complete")) {
                try {
                    String skippedFixtureReasonVal = valueById(edgeFormDatas, 23);
                    skippedFixtureReasonVal = getSkippedReason(skippedFixtureReasonVal,edgeFormDatas);
                    logger.info("Skipped Fixture Reason Val" + skippedFixtureReasonVal);


                    try{
                        checkLuminaireSerialNumberSync(skippedFixtureReasonVal,edgeNote.getTitle(),loggingModel);
                    }catch (SerialNumberSyncedException e){
                        logger.info("SLV Install Status is already Could not Complete and Skipped Fixture Reason Value also already Synced");
                        slvInterfaceLogEntity.setErrordetails("SLV Install Status is already Could not Complete and nightRideValue Value is Empty");
                        slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                        return;
                    }


                    if (nightRideValue != null && !nightRideValue.trim().isEmpty()) {
                        nightRideValue = nightRideValue + "," + skippedFixtureReasonVal;
                    } else {
                        logger.info("SLV Install Status is Could Not Complete"+loggingModel.isCouldNotComplete());
                        logger.info("SLV Luminaire SerialNumber"+loggingModel.getSlvLuminaireSerialNumber());
                        if(loggingModel.isCouldNotComplete() && loggingModel.getSlvLuminaireSerialNumber() != null && loggingModel.getSlvLuminaireSerialNumber().contains(skippedFixtureReasonVal)){
                            logger.info("SLV Install Status is already Could not Complete and Skipped Fixture Reason Value also already Synced");
                            slvInterfaceLogEntity.setErrordetails("SLV Install Status is already Could not Complete and Skipped Fixture Reason Value also already Synced");
                            slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                            return;
                        }
                        nightRideValue = dateFormat(edgeNote.getCreatedDateTime()) + " :" + skippedFixtureReasonVal;
                    }
                } catch (NoValueException e) {
                    logger.error("Error in while getting installStatusValue", e);

                    if(loggingModel.isCouldNotComplete() && (nightRideValue == null || nightRideValue.trim().isEmpty())){
                        logger.info("SLV Install Status is already Could not Complete and Skipped Fixture Reason Value also already Synced");
                        slvInterfaceLogEntity.setErrordetails("SLV Install Status is already Could not Complete and nightRideValue Value is Empty");
                        slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                        return;
                    }
                }
                logger.info("Before addUserToLuminaireSerialNumber nightRideValue:"+nightRideValue);
                logger.info("Start of addUserToLuminaireSerialNumber");
                nightRideValue = addUserToLuminaireSerialNumber(nightRideValue,edgeNote.getCreatedBy());
                addEdgeAllSerialNumber(nightRideValue,loggingModel.getIdOnController());
                logger.info("End of addUserToLuminaireSerialNumber");
                logger.info("After addUserToLuminaireSerialNumber nightRideValue:"+nightRideValue);
                int errorCode = sync2SlvInstallStatus(loggingModel.getIdOnController(), loggingModel.getControllerSrtId(), loggingModel, nightRideKey, nightRideValue);
                if (errorCode != 0) {
                    loggingModel.setErrorDetails("Error while updating Could not complete install status.Corresponding Error code :" + errorCode);
                    loggingModel.setStatus(MessageConstants.ERROR);
                    slvInterfaceLogEntity.setErrordetails("Error while updating Could not complete install status.Corresponding Error code :" + errorCode);
                    slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                } else {
                    loggingModel.setErrorDetails(MessageConstants.COULD_NOT_COMPLETE_SUCCESS_MSG);
                    loggingModel.setStatus(MessageConstants.SUCCESS);
                    slvInterfaceLogEntity.setErrordetails(MessageConstants.COULD_NOT_COMPLETE_SUCCESS_MSG);
                    slvInterfaceLogEntity.setStatus(MessageConstants.SUCCESS);
                }

                return;
            }

            // Get MAC Address
            String nodeMacValue = null;
            try {
                nodeMacValue = valueById(edgeFormDatas, 19);
                logger.info("Node MAC Val" + nodeMacValue);
            } catch (NoValueException e) {
                logger.error("Error in while getting MAC Address", e);
                // loggingModel.setErrorDetails(MessageConstants.NODE_MAC_ADDRESS_NOT_AVAILABLE);
                // loggingModel.setStatus(MessageConstants.ERROR);
                // return;
            }

            // Get Fixer QR Scan value
            String fixerQrScanValue = null;
            try {
                fixerQrScanValue = valueFixtureValueById(edgeFormDatas, 20);
                logger.info("Fixture QR Scan Val" + fixerQrScanValue);
                if (fixerQrScanValue == null || fixerQrScanValue.contains("null") || fixerQrScanValue.trim().isEmpty()) {
                    fixerQrScanValue = valueById(edgeFormDatas, 115);
                    logger.info("Fixture QR Scan Val - 115" + fixerQrScanValue);
                }
            } catch (NoValueException e) {

                logger.info("Fixture QR Scan Val" + fixerQrScanValue);
                logger.error("Error in while getting MAC Fixture QR Scan", e);
                /*loggingModel.setErrorDetails(MessageConstants.FIXTURE_CODE_NOT_AVAILABLE);
                loggingModel.setStatus(MessageConstants.ERROR);*/
                // loggingModel.setProcessOtherForm(true);
                // return;
            }
            if ((nodeMacValue == null && fixerQrScanValue == null) || ((nodeMacValue != null && nodeMacValue.isEmpty()) && (fixerQrScanValue != null && fixerQrScanValue.isEmpty()))) {
                loggingModel.setStatus(MessageConstants.ERROR);
                loggingModel.setErrorDetails("No MacAddress and FixtureQrScan");
                slvInterfaceLogEntity.setErrorcategory(MessageConstants.EDGE_VALIDATION_ERROR);
                slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                slvInterfaceLogEntity.setErrordetails(MessageConstants.NO_MACADDRESS_NO_FIXTURE);
                return;
            }
            if (nodeMacValue != null && !nodeMacValue.startsWith("00") && (fixerQrScanValue != null && fixerQrScanValue.startsWith("00"))) {
                String temp = nodeMacValue;
                nodeMacValue = fixerQrScanValue;
                fixerQrScanValue = temp;
            }

            //Suppose fixtureqrscan value has mac address but MAC Address is empty
            if (nodeMacValue == null && (fixerQrScanValue != null && fixerQrScanValue.startsWith("00"))) {
                nodeMacValue = fixerQrScanValue;
                fixerQrScanValue = null;
                loggingModel.setFixtureQRSame(true);
            }

            if ((nodeMacValue == null || nodeMacValue.isEmpty()) && fixerQrScanValue != null) {
                loggingModel.setFixtureOnly(true);
            } else {
                loggingModel.setFixtureOnly(false);
            }

            if (isResync) {
                if (nodeMacValue != null && !nodeMacValue.trim().isEmpty() && !loggingModel.isMacAddressUsed()) {
                    try {
                        loggingModel.setReSync(true);
                        SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(loggingModel);
                        replaceOLC(loggingModel.getControllerSrtId(), loggingModel.getIdOnController(), "", slvTransactionLogs, slvInterfaceLogEntity,loggingModel.getAtlasPhysicalPage(),loggingModel,edgeNote);
                    } catch (Exception e) {
                        String message = e.getMessage();
                    }
                }


            }

            // Check Whether MAC Address is already assigned to other fixtures or not.
            try {
                if (nodeMacValue != null) {
                    checkMacAddressExists(nodeMacValue, loggingModel.getIdOnController(), nightRideKey, nightRideValue, loggingModel, slvInterfaceLogEntity);
                }
            } catch (QRCodeAlreadyUsedException e1) {
                logger.error("MacAddress (" + e1.getMacAddress()
                        + ")  - Already in use. So this pole is not synced with SLV. Note Title :[" + edgeNote.getTitle()
                        + " ]");
            }
            loggingModel.setMacAddress(nodeMacValue);
            slvInterfaceLogEntity.setMacAddress(nodeMacValue);
            slvInterfaceLogEntity.setFixtureqrscan(fixerQrScanValue);
            sync2Slv(nodeMacValue, fixerQrScanValue, edgeNote, loggingModel, loggingModel.getIdOnController(), loggingModel.getControllerSrtId(), utilLocId, true, nightRideKey, nightRideValue, slvInterfaceLogEntity);
        } catch (NoValueException e) {
            logger.error("Error no value", e);
            loggingModel.setErrorDetails(e.getMessage());
            loggingModel.setStatus(MessageConstants.ERROR);
            return;
        } catch (Exception e) {
            loggingModel.setErrorDetails(MessageConstants.ERROR + "" + e.getMessage());
            loggingModel.setStatus(MessageConstants.ERROR);
            logger.error("Error ", e);
        }

    }


    public void repairAndOutage(List<EdgeFormData> edgeFormDatas, EdgeNote edgeNote, InstallMaintenanceLogModel loggingModel, String utilLocId, String nightRideKey, String nightRideValue, String formatedValueNR, SlvInterfaceLogEntity slvInterfaceLogEntity) {

        String repairsOutagesValue = loggingModel.getRepairsOption();
        /*try {
            repairsOutagesValue = valueById(edgeFormDatas, 24);
        } catch (NoValueException e) {
            loggingModel.setStatus(MessageConstants.ERROR);
            loggingModel.setErrorDetails("Repairs & Outages options are not selected.");
            return;
        }*/

        System.out.println(repairsOutagesValue);
        switch (repairsOutagesValue) {
            case "Replace Node and Fixture":
                slvInterfaceLogEntity.setSelectedReplace("Replace Node and Fixture");
                replaceNodeFixture(edgeFormDatas, edgeNote, loggingModel, utilLocId, nightRideKey, nightRideValue, slvInterfaceLogEntity);
                break;
            case "Replace Node only":
                slvInterfaceLogEntity.setSelectedReplace("Replace Node only");
                replaceNodeOnly(edgeFormDatas, edgeNote, loggingModel, utilLocId, nightRideKey, nightRideValue, slvInterfaceLogEntity);
                break;
            case "Replace Fixture only":
                slvInterfaceLogEntity.setSelectedReplace("Replace Fixture only");
                String fixerQrScanValue = null;
                try {
                    fixerQrScanValue = valueById(edgeFormDatas, 39);
                    String idOnController = loggingModel.getIdOnController();
                    String controllerStrIdValue = loggingModel.getControllerSrtId();
                    sync2Slv(null, fixerQrScanValue, edgeNote, loggingModel, idOnController, controllerStrIdValue, utilLocId, false, nightRideKey, nightRideValue, slvInterfaceLogEntity);
                    break;
                } catch (NoValueException e) {
                    slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                    slvInterfaceLogEntity.setErrordetails("Fixture QR Scan value is empty");
                    slvInterfaceLogEntity.setErrorcategory(MessageConstants.EDGE_VALIDATION_ERROR);
                }


            case "Power Issue":
                loggingModel.setStatus(MessageConstants.ERROR);
                loggingModel.setErrorDetails("SLV Interface Doest not support this(Power Issue option is selected).");
                slvInterfaceLogEntity.setErrorcategory(MessageConstants.EDGE_VALIDATION_ERROR);
                slvInterfaceLogEntity.setSelectedReplace("Power Issue");
                break;
            case "Unable to Repair(CDOT Issue)":
                logger.info("Processed Unable to Repair option :" + edgeNote.getTitle());
                logger.info("Processed NoteGuid :" + edgeNote.getNoteGuid());
                slvInterfaceLogEntity.setSelectedReplace("Unable to Repair(CDOT Issue)");
                //  String nightRideKey = properties.getProperty("amerescousa.night.ride.key_for_slv");
                processOtherTask(edgeFormDatas, edgeNote, loggingModel, nightRideKey, formatedValueNR, slvInterfaceLogEntity);
                break;

            case "CNR":
                cnrWorkFlow(edgeFormDatas,edgeNote,loggingModel,slvInterfaceLogEntity);
                break;

            case "Resolved (Other)":
                loggingModel.setStatus(MessageConstants.ERROR);
                loggingModel.setErrorDetails("SLV Interface Doest not support this(Power Issue option is selected).");
                slvInterfaceLogEntity.setErrorcategory(MessageConstants.EDGE_VALIDATION_ERROR);
                slvInterfaceLogEntity.setSelectedReplace("Power Issue");
                break;
        }
    }


    private void replaceNodeFixture(List<EdgeFormData> edgeFormDatas, EdgeNote edgeNote, InstallMaintenanceLogModel loggingModel, String utilLocId, String nightRideKey, String nightRideValue, SlvInterfaceLogEntity slvInterfaceLogEntity) {
        try {
            String existingNodeMacAddress = null;
            String newNodeMacAddress = null;

            // Get New Node MAC Address value
            try {
                newNodeMacAddress = valueById(edgeFormDatas, 26);
                loggingModel.setNewNodeMACaddress(newNodeMacAddress);
            } catch (NoValueException e) {
                loggingModel.setErrorDetails(MessageConstants.NEW_MAC_ADDRESS_NOT_AVAILABLE);
                loggingModel.setStatus(MessageConstants.ERROR);
                slvInterfaceLogEntity.setErrorcategory(MessageConstants.EDGE_VALIDATION_ERROR);
                slvInterfaceLogEntity.setErrordetails(MessageConstants.NEW_MAC_ADDRESS_NOT_AVAILABLE);
                slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
            }

            // Get Existing Node MAC Address value
            try {
                existingNodeMacAddress = valueById(edgeFormDatas, 36);
                loggingModel.setExistingNodeMACaddress(existingNodeMacAddress);
            } catch (NoValueException e) {
                /*loggingModel.setErrorDetails(MessageConstants.OLD_MAC_ADDRESS_NOT_AVAILABLE);
                loggingModel.setProcessOtherForm(true);
                loggingModel.setStatus(MessageConstants.ERROR);
                return;*/
            }

            // Get Fixer QR Scan value
            String fixerQrScanValue = null;
            try {
                fixerQrScanValue = valueById(edgeFormDatas, 38);
            } catch (NoValueException e) {
               /* loggingModel.setProcessOtherForm(true);
                loggingModel.setErrorDetails(MessageConstants.FIXTURE_CODE_NOT_AVAILABLE);
                loggingModel.setStatus(MessageConstants.ERROR);
                // loggingModel.setProcessOtherForm(true);*/
                // return;
            }


            if (newNodeMacAddress != null && !newNodeMacAddress.startsWith("00") && (fixerQrScanValue != null && fixerQrScanValue.startsWith("00"))) {
                String temp = newNodeMacAddress;
                newNodeMacAddress = fixerQrScanValue;
                fixerQrScanValue = temp;
            }

            //Suppose fixtureqrscan value has mac address but MAC Address is empty
            if (newNodeMacAddress == null && (fixerQrScanValue != null && fixerQrScanValue.startsWith("00"))) {
                newNodeMacAddress = fixerQrScanValue;
                fixerQrScanValue = null;
                loggingModel.setFixtureQRSame(true);
            }

            String idOnController = loggingModel.getIdOnController();


            String controllerStrIdValue = loggingModel.getControllerSrtId();


            if (newNodeMacAddress != null && !newNodeMacAddress.isEmpty()) {
                // Check Existing MAC Address matches with SLV or not.
                boolean isMatched = checkExistingMacAddressValid(edgeNote,loggingModel);
                if(!isMatched){
                    logger.info("Existing MAC Address not Matched with SLV.");
                    // If not, set Account Number value as Unsuccessful.
                    List<Object> paramsList = new ArrayList<>();
                    syncAccountNumber(paramsList,loggingModel,edgeNote, Utils.UN_SUCCESSFUL,newNodeMacAddress);
                    return;
                }


                try {
                    checkMacAddressExists(newNodeMacAddress, idOnController, nightRideKey, nightRideValue, loggingModel, slvInterfaceLogEntity);
                } catch (QRCodeAlreadyUsedException e1) {
                    logger.error("MacAddress (" + e1.getMacAddress()
                            + ")  - Already in use. So this pole is not synced with SLV. Note Title :[" + edgeNote.getTitle()
                            + " ]");
                }
            }

            boolean isError = false;
            StringBuffer statusDescription = new StringBuffer();
            // Call Empty ReplaceOLC
            try {
                if (!loggingModel.isMacAddressUsed()) {
                    SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(loggingModel);
                    replaceOLC(controllerStrIdValue, idOnController, "", slvTransactionLogs, slvInterfaceLogEntity,loggingModel.getAtlasPhysicalPage(),loggingModel,edgeNote);
                    statusDescription.append(MessageConstants.EMPTY_REPLACE_OLC_SUCCESS);
                }

            } catch (ReplaceOLCFailedException e) {
                isError = true;
                statusDescription.append(e.getMessage());
                e.printStackTrace();
            }


            sync2Slv(newNodeMacAddress, fixerQrScanValue, edgeNote, loggingModel, idOnController, controllerStrIdValue, utilLocId, false, nightRideKey, nightRideValue, slvInterfaceLogEntity);
            if (isError) {
                loggingModel.setErrorDetails(loggingModel.getErrorDetails() + statusDescription.toString());
            }
        } catch (Exception e) {
            loggingModel.setErrorDetails(e.getMessage());
            loggingModel.setStatus(MessageConstants.ERROR);
            slvInterfaceLogEntity.setErrorcategory(MessageConstants.SLV_VALIDATION_ERROR);
            slvInterfaceLogEntity.setErrordetails("Replace Olc Error :" + e.getMessage());
            slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
            return;
        }

    }


    private void replaceNodeOnly(List<EdgeFormData> edgeFormDatas, EdgeNote edgeNote, InstallMaintenanceLogModel loggingModel, String utilLocId, String nightRideKey, String nightRideValue, SlvInterfaceLogEntity slvInterfaceLogEntity) {
        try {
            String existingNodeMacAddress = null;
            String newNodeMacAddress = null;
            // Get New Node MAC Address value
            try {
                newNodeMacAddress = valueById(edgeFormDatas, 30);
                loggingModel.setNewNodeMACaddress(newNodeMacAddress);
            } catch (NoValueException e) {
                loggingModel.setErrorDetails(MessageConstants.NEW_MAC_ADDRESS_NOT_AVAILABLE);
                loggingModel.setStatus(MessageConstants.ERROR);
                slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                slvInterfaceLogEntity.setErrorcategory(MessageConstants.EDGE_VALIDATION_ERROR);
                slvInterfaceLogEntity.setErrordetails(MessageConstants.NEW_MAC_ADDRESS_NOT_AVAILABLE);
                return;
            }

            // Get Existing Node MAC Address value
            try {
                existingNodeMacAddress = valueById(edgeFormDatas, 29);
                loggingModel.setExistingNodeMACaddress(existingNodeMacAddress);
            } catch (NoValueException e) {
                /*loggingModel.setErrorDetails(MessageConstants.OLD_MAC_ADDRESS_NOT_AVAILABLE);
                loggingModel.setStatus(MessageConstants.ERROR);
                return;*/
            }

            String idOnController = loggingModel.getIdOnController();

            String controllerStrIdValue = loggingModel.getControllerSrtId();
            
            boolean isMatched = checkExistingMacAddressValid(edgeNote,loggingModel);
            logger.info("Existing MAC Address match result:"+isMatched);
            if(!isMatched){
                logger.info("Existing MAC Address not match with SLV. So Current note is skipped.");

                if(!isMatched){
                    logger.info("Existing MAC Address not Matched with SLV.");
                    // If not, set Account Number value as Unsuccessful.
                    List<Object> paramsList = new ArrayList<>();
                    syncAccountNumber(paramsList,loggingModel,edgeNote, Utils.UN_SUCCESSFUL,newNodeMacAddress);
                }

                return;
            }

            try {
                checkMacAddressExists(newNodeMacAddress, idOnController, nightRideKey, nightRideValue, loggingModel, slvInterfaceLogEntity);
            } catch (QRCodeAlreadyUsedException e1) {
                slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                slvInterfaceLogEntity.setErrordetails("MacAddress Already use. So this pole not synced with slv");
                slvInterfaceLogEntity.setErrorcategory(MessageConstants.EDGE_VALIDATION_ERROR);
                logger.error("MacAddress (" + e1.getMacAddress()
                        + ")  - Already in use. So this pole is not synced with SLV. Note Title :[" + edgeNote.getTitle()
                        + " ]");
                loggingModel.setStatus(MessageConstants.ERROR);
                loggingModel.setErrorDetails("MacAddress (" + e1.getMacAddress()
                        + ")  - Already in use. So this pole is not synced with SLV. Note Title :[" + edgeNote.getTitle()
                        + " ]");
                return;
            }


            // Call Empty ReplaceOLC
            try {
                if (!loggingModel.isMacAddressUsed()) {
                    SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(loggingModel);
                    replaceOLC(controllerStrIdValue, idOnController, "", slvTransactionLogs, slvInterfaceLogEntity,loggingModel.getAtlasPhysicalPage(),loggingModel,edgeNote);
                }

            } catch (ReplaceOLCFailedException e) {
                e.printStackTrace();
            }


            sync2Slv(newNodeMacAddress, null, edgeNote, loggingModel, idOnController, controllerStrIdValue, utilLocId, false, nightRideKey, nightRideValue, slvInterfaceLogEntity);


        } catch (Exception e) {
            slvInterfaceLogEntity.setErrorcategory(MessageConstants.EDGE_VALIDATION_ERROR);
            slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
            slvInterfaceLogEntity.setErrordetails("replaceNodeOnly failure" + e.getMessage());
            loggingModel.setErrorDetails(e.getMessage());
            loggingModel.setStatus(MessageConstants.ERROR);
            return;
        }
    }

    private void processRemoveAction(List<EdgeFormData> edgeFormDatas, String utilLocId, InstallMaintenanceLogModel installMaintenanceLogModel, SlvInterfaceLogEntity slvInterfaceLogEntity,EdgeNote edgeNote,String notesData) {
        String removeReason = null;
        try {
            removeReason = valueById(edgeFormDatas, 35);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (removeReason != null) {
            logger.info("remove reason:" + removeReason);
            DeviceAttributes deviceAttributes = getDeviceValues(installMaintenanceLogModel);
            switch (removeReason) {
                case "Installed on Wrong Fixture":
                    try {
                        logger.info("Luminaire Serial Number going to clear.");
                        connectionDAO.removeEdgeAllSerialNumber(installMaintenanceLogModel.getIdOnController());

                        logger.info("Luminaire Serial Number value cleared.");
                    }catch (Exception e){
                        logger.error("Error in removeEdgeAllSerialNumber",e);
                    }


                    try {
                        if(installMaintenanceLogModel.isDroppedPinWorkflow())
                        {
                            DroppedPinRemoveEvent droppedPinRemoveEvent = new DroppedPinRemoveEvent();
                            droppedPinRemoveEvent.setEventTime(System.currentTimeMillis());
                            droppedPinRemoveEvent.setNoteguid(installMaintenanceLogModel.getProcessedNoteId());
                            droppedPinRemoveEvent.setIdoncontroller(installMaintenanceLogModel.getNoteName());
                            connectionDAO.createOrUpdateDroppedPinRemoveEvent(droppedPinRemoveEvent);
                        }
                        if (deviceAttributes != null && deviceAttributes.getInstallStatus().equals(InstallStatus.To_be_installed.getValue())) {
                            installMaintenanceLogModel.setStatus(MessageConstants.ERROR);
                            installMaintenanceLogModel.setErrorDetails("Already Processed.Install Status: To be installed");
                            slvInterfaceLogEntity.setErrorcategory(MessageConstants.SLV_VALIDATION_ERROR);
                            slvInterfaceLogEntity.setErrordetails("Already Processed.Install Status: To be installed");
                            slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);

                            try {
                                clearDeviceValues(installMaintenanceLogModel.getIdOnController(), installMaintenanceLogModel.getControllerSrtId(), "serialnumber", installMaintenanceLogModel,false);
                            } catch (Exception e) {
                                e.printStackTrace();
                                logger.error("Error in clear device values:" + e.getMessage());
                            }

                            return;
                        }
                        String macaddress = null;
                        if (deviceAttributes != null) {
                            logger.info("removed mac address:" + deviceAttributes.getMacAddress());
                            macaddress = deviceAttributes.getMacAddress();
                        }

                        try {
                            SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(installMaintenanceLogModel);
                            replaceOLC(installMaintenanceLogModel.getControllerSrtId(), installMaintenanceLogModel.getIdOnController(), "", slvTransactionLogs, slvInterfaceLogEntity,null,installMaintenanceLogModel,null);
                        } catch (Exception e) {
                            e.printStackTrace();
                            logger.error("error in replace OLC:" + e.getMessage());
                        }
                        logger.info("empty replaceOLC called");
                        try {
                            clearDeviceValues(installMaintenanceLogModel.getIdOnController(), installMaintenanceLogModel.getControllerSrtId(), "Installed on Wrong Fixture", installMaintenanceLogModel,false);
                        } catch (Exception e) {
                            e.printStackTrace();
                            logger.error("Error in clear device values:" + e.getMessage());
                        }
                        logger.info("cleared device value");
                        if (macaddress != null) {
                            DuplicateMacAddress duplicateMacAddress = connectionDAO.getDuplicateMacAddress(macaddress);
                            logger.info("duplicate mac address: " + duplicateMacAddress);
                            if (duplicateMacAddress != null) {
                                //reSync(duplicateMacAddress.getNoteguid(),getEdgeToken(),true,utilLocId,true);
                                logger.info("resync called due to duplicate mac address");
                            }

                            logger.info("Data going to remove from EdgeAllMac and EdgeAllFix Table.");
                            logger.info("IdOnController:"+installMaintenanceLogModel.getIdOnController());
                            logger.info("MACAddress:"+macaddress);
                            connectionDAO.removeEdgeAllMAC(installMaintenanceLogModel.getIdOnController(),macaddress);
                        }

                        slv2EdgeService.removeSwapForm(edgeNote,notesData);
                        removeSwapPromotedData(installMaintenanceLogModel.getIdOnController());
                        connectionDAO.removeEdgeAllFixture(installMaintenanceLogModel.getIdOnController());
                        installMaintenanceLogModel.setInstallOnWrongFix(true);
                        removeEdgeSLVMacAddress(installMaintenanceLogModel.getIdOnController());

                        connectionDAO.removeAllEdgeFormDates(installMaintenanceLogModel.getIdOnController());

                        logger.info("Luminaire Serial Number going to clear.");
                        connectionDAO.removeEdgeAllSerialNumber(installMaintenanceLogModel.getIdOnController());
                        logger.info("Luminaire Serial Number value cleared.");
                    } catch (Exception e) {
                        logger.error("Error in processRemoveAction", e);
                    }
                    break;
                case "Pole Removed":
                    try {
                        if (deviceAttributes != null && deviceAttributes.getInstallStatus().equals(InstallStatus.Removed.getValue())) {
                            installMaintenanceLogModel.setStatus(MessageConstants.ERROR);
                            installMaintenanceLogModel.setErrorDetails("Already Processed.Install Status: Pole Removed");
                            slvInterfaceLogEntity.setErrorcategory(MessageConstants.SLV_VALIDATION_ERROR);
                            slvInterfaceLogEntity.setErrordetails("Already Processed.Install Status: Removed");
                            slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                            return;
                        }
                         boolean isInActive =  isMACInActive(installMaintenanceLogModel.getCommunicationStatus());
                        if(isInActive){
                            try {
                                SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(installMaintenanceLogModel);
                                replaceOLC(installMaintenanceLogModel.getControllerSrtId(), installMaintenanceLogModel.getIdOnController(), "", slvTransactionLogs, slvInterfaceLogEntity,null,installMaintenanceLogModel,null);
                                if (deviceAttributes != null) {
                                    logger.info("removed mac address:" + deviceAttributes.getMacAddress());
                                    String macaddress = deviceAttributes.getMacAddress();
                                    connectionDAO.removeEdgeAllMAC(installMaintenanceLogModel.getIdOnController(),macaddress);
                                    removeEdgeSLVMacAddress(installMaintenanceLogModel.getIdOnController());
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                logger.error("error in replace OLC:" + e.getMessage());
                            }
                        }
                        clearDeviceValues(installMaintenanceLogModel.getIdOnController(), installMaintenanceLogModel.getControllerSrtId(), "Pole Removed", installMaintenanceLogModel,false);
                        slvInterfaceLogEntity.setStatus(MessageConstants.SUCCESS);
                        connectionDAO.removeCurrentEdgeFormDates(installMaintenanceLogModel.getIdOnController());
                        slv2EdgeService.removeSwapForm(edgeNote,notesData);
                        removeSwapPromotedData(installMaintenanceLogModel.getIdOnController());
                        installMaintenanceLogModel.setPoleKnockDown(true);
                    } catch (Exception e) {
                        logger.error("Error in processRemoveAction", e);
                    }
                    break;
                case "Installation Removed":
                    try {
                        if (deviceAttributes != null && (deviceAttributes.getInstallStatus().equals(InstallStatus.Pole_Knocked_Down.getValue()) || deviceAttributes.getInstallStatus().equals(InstallStatus.Installation_Removed.getValue()))) {
                            installMaintenanceLogModel.setStatus(MessageConstants.ERROR);
                            installMaintenanceLogModel.setErrorDetails("Already Processed.Install Status: Pole Knocked Down");
                            slvInterfaceLogEntity.setErrorcategory(MessageConstants.SLV_VALIDATION_ERROR);
                            slvInterfaceLogEntity.setErrordetails("Already Processed.Install Status: Pole Knocked Down");
                            slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                            return;
                        }
                        boolean isInActive =  isMacActive(installMaintenanceLogModel.getCommunicationStatus());
                        String macaddress = null;
                        if (deviceAttributes != null) {
                            logger.info("removed mac address:" + deviceAttributes.getMacAddress());
                             macaddress = deviceAttributes.getMacAddress();
                        }
                        connectionDAO.removeEdgeAllFixture(installMaintenanceLogModel.getIdOnController());
                        boolean isMacRemoved = false;
                        if(isInActive){
                            addInstallationRemovedExpReport(macaddress,edgeNote,installMaintenanceLogModel.getCommunicationStatus());
                        }else{
                            if(macaddress != null){
                                connectionDAO.removeEdgeAllMAC(installMaintenanceLogModel.getIdOnController(),macaddress);
                            }
                            try {
                                SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(installMaintenanceLogModel);
                                replaceOLC(installMaintenanceLogModel.getControllerSrtId(), installMaintenanceLogModel.getIdOnController(), "", slvTransactionLogs, slvInterfaceLogEntity,null,installMaintenanceLogModel,null);
                                removeEdgeSLVMacAddress(installMaintenanceLogModel.getIdOnController());
                                isMacRemoved = true;
                                installMaintenanceLogModel.setMacRemoved(isMacRemoved);
                                connectionDAO.removeCurrentEdgeFormDates(installMaintenanceLogModel.getIdOnController());
                            } catch (Exception e) {
                                e.printStackTrace();
                                logger.error("error in replace OLC:" + e.getMessage());
                            }
                        }
                        clearDeviceValues(installMaintenanceLogModel.getIdOnController(), installMaintenanceLogModel.getControllerSrtId(), "Pole Knocked-Down", installMaintenanceLogModel,isMacRemoved);
                        slvInterfaceLogEntity.setStatus(MessageConstants.SUCCESS);
                        installMaintenanceLogModel.setPoleKnockDown(true);
                        slv2EdgeService.removeSwapForm(edgeNote,notesData);
                        removeSwapPromotedData(installMaintenanceLogModel.getIdOnController());

                    } catch (Exception e) {
                        logger.error("Error in processRemoveAction", e);
                    }
                    break;
            }
        }
    }


    private boolean isMACInActive(String communicationStatus){
        if(communicationStatus != null && (communicationStatus.equals("No data ever received") || communicationStatus.equals("No data for more than 48 hours"))){
            return true;
        }
        return false;
    }


    private boolean isMacActive(String communicationStatus){
        if(communicationStatus != null && communicationStatus.trim().toLowerCase().contains("active")){
            return true;
        }
        return false;
    }

    private void clearDeviceValues(String idOnController, String controllerStrIdValue, String type, InstallMaintenanceLogModel loggingModel,boolean isMacRemoved) {
        List<Object> paramsList = new ArrayList<>();
        paramsList.add("idOnController=" + idOnController);
        paramsList.add("controllerStrId=" + controllerStrIdValue);
        switch (type) {
            case "Installed on Wrong Fixture":
                clearFixtureValues(paramsList);
                addStreetLightData("cslp.lum.install.date", "", paramsList);
                addStreetLightData("luminaire.installdate", "", paramsList);
                addStreetLightData("cslp.node.install.date", "", paramsList);
                addStreetLightData("MacAddress", "", paramsList);
                addStreetLightData("install.date", "", paramsList);
                addStreetLightData("luminaire.type", "HPS", paramsList);
                addStreetLightData("DimmingGroupName", "", paramsList);
                addStreetLightData("client.name", "", paramsList);
                addStreetLightData("installStatus", InstallStatus.To_be_installed.getValue(), paramsList);
                addStreetLightData("luminaire.serialnumber", "", paramsList);
                addStreetLightData("comed.componentffectivedate", "", paramsList);

                String power = getLuminaireWattage(loggingModel);
                logger.info("Power:"+power);
                addStreetLightData("power", power, paramsList);
                break;
            case "Pole Removed":
               // clearFixtureValues(paramsList);
                addStreetLightData("install.date", "", paramsList);
                addStreetLightData("luminaire.installdate", "", paramsList);
                addStreetLightData("installStatus", InstallStatus.Removed.getValue(), paramsList);
                addStreetLightData("DimmingGroupName", "", paramsList);
                addStreetLightData("power", "", paramsList);
                break;
            case "Pole Knocked-Down": // Installation_Removed previously Pole Knocked-Down
               // clearFixtureValues(paramsList);
                if(isMacRemoved){
                    addStreetLightData("install.date", "", paramsList);
                    addStreetLightData("luminaire.installdate", "", paramsList);
                }
               // addStreetLightData("DimmingGroupName", "", paramsList);
                addStreetLightData("installStatus", InstallStatus.Installation_Removed.getValue(), paramsList);
                //addStreetLightData("power", "", paramsList);
                break;

            case "serialnumber":
                addStreetLightData("installStatus", InstallStatus.To_be_installed.getValue(), paramsList);
                addStreetLightData("luminaire.serialnumber", "", paramsList);
                break;
        }
        SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(loggingModel);
        int errorCode = setDeviceValues(paramsList, slvTransactionLogs);
        if (errorCode == 0) {
            logger.info("clearing device values completed: " + idOnController);
        } else {
            logger.error("Error in clearDeviceValues");
        }
    }

    private String getLuminaireWattage(LoggingModel loggingModel){
        logger.info("Luminaire Fixturecode :"+loggingModel.getLuminaireFixturecode());
        if(loggingModel.getLuminaireFixturecode() != null && !loggingModel.getLuminaireFixturecode().trim().isEmpty()){
            if(loggingModel.getLuminaireFixturecode().toUpperCase().contains("PIGGY")){
                return "100";
            }
            if(loggingModel.getLuminaireFixturecode().toUpperCase().contains("COBRAHEAD")){
                return "250";
            }
            if(loggingModel.getLuminaireFixturecode().toUpperCase().contains("FLOOD")){
                return "400";
            }

        }
        return "250";
    }

    private void clearFixtureValues(List<Object> paramsList) {
        addStreetLightData("luminaire.brand", "", paramsList);
        addStreetLightData("device.luminaire.partnumber", "", paramsList);
        addStreetLightData("luminaire.model", "", paramsList);
        addStreetLightData("device.luminaire.manufacturedate", "", paramsList);
        addStreetLightData("device.luminaire.colortemp", "", paramsList);
        addStreetLightData("device.luminaire.lumenoutput", "", paramsList);
        addStreetLightData("luminaire.DistributionType", "", paramsList);
        addStreetLightData("luminaire.colorcode", "", paramsList);
        addStreetLightData("device.luminaire.drivermanufacturer", "", paramsList);
        addStreetLightData("device.luminaire.driverpartnumber", "", paramsList);
        addStreetLightData("ballast.dimmingtype", "", paramsList);
    }




    private void loadDateValFromEdge(List<EdgeFormData> edgeFormDatas,InstallMaintenanceLogModel installMaintenanceLogModel){
        SLVDates edgeSLVDates = new SLVDates();
        installMaintenanceLogModel.getDatesHolder().setEdgeDates(edgeSLVDates);
        // Get CSLP Node Install Date
        String cslpNodeInstallDate = null;
        try {
            cslpNodeInstallDate = valueById(edgeFormDatas, 169);
            if(cslpNodeInstallDate != null && !cslpNodeInstallDate.trim().isEmpty()){
                try{
                    String startOfDay = getStartOfDay(cslpNodeInstallDate);
                    //To avoid invalid date value, we added this restrictions
                    if(Long.valueOf(startOfDay) >= 1420092000000L){
                        edgeSLVDates.setCslpNodeDate(startOfDay);
                    }else{
                        logger.info("Invalid Date Val:"+startOfDay);
                    }

                }catch (Exception e){
                    logger.error("Error in cslpNodeInstallDate:"+cslpNodeInstallDate,e);
                }
            }

            logger.info("cslpNodeInstallDate Val" + cslpNodeInstallDate);
        } catch (NoValueException e) {
            logger.error("CSLP Node Install Date is empty", e);
        }


        // Get CSLP Node Install Date
        String cslpLumInstallDate = null;
        try {
            cslpLumInstallDate = valueById(edgeFormDatas, 170);
            if(cslpLumInstallDate != null && !cslpLumInstallDate.trim().isEmpty()){
                try{
                    String startOfDay = getStartOfDay(cslpLumInstallDate);
                    //To avoid invalid date value, we added this restrictions
                    if(Long.valueOf(startOfDay) >= 1420092000000L){
                        edgeSLVDates.setCslpLumDate(startOfDay);
                    }else{
                        logger.info("Invalid Date Val:"+startOfDay);
                    }
                }catch (Exception e){
                    logger.error("Error in cslpLumInstallDate:"+cslpLumInstallDate,e);
                }
            }
            logger.info("cslpLumInstallDate Val" + cslpLumInstallDate);
        } catch (NoValueException e) {
            logger.error("CSLP Lum Install Date is empty", e);
        }


        // Get Node Install Date
        String installDate = null;
        try {
            installDate = valueById(edgeFormDatas, 171);
            if(installDate != null && !installDate.trim().isEmpty()){
                try{
                    String startOfDay = getStartOfDay(installDate);
                    //To avoid invalid date value, we added this restrictions
                    if(Long.valueOf(startOfDay) >= 1420092000000L){
                        edgeSLVDates.setNodeInstallDate(startOfDay);
                    }else{
                        logger.info("Invalid Date Val:"+startOfDay);
                    }
                }catch (Exception e){
                    logger.error("Error in installDate:"+installDate,e);
                }

            }


            logger.info("installDate Val" + installDate);
        } catch (NoValueException e) {
            logger.error("Install Date is empty", e);
        }


        // Get Lum Install Date
        String lumInstallDate = null;
        try {
            lumInstallDate = valueById(edgeFormDatas, 172);
            if(lumInstallDate != null && !lumInstallDate.trim().isEmpty()){
                try{
                    String startOfDay = getStartOfDay(lumInstallDate);
                    //To avoid invalid date value, we added this restrictions
                    if(Long.valueOf(startOfDay) >= 1420092000000L){
                        edgeSLVDates.setLumInstallDate(startOfDay);
                    }else{
                        logger.info("Invalid Date Val:"+startOfDay);
                    }


                }catch (Exception e){
                    logger.error("Error in lumInstallDate:"+lumInstallDate,e);
                }

            }
            logger.info("lumInstallDate Val" + lumInstallDate);
        } catch (NoValueException e) {
            logger.error("Lum Install Date is empty", e);
        }


        logger.info("Edge Dates:"+gson.toJson(edgeSLVDates));
        getSlvSyncDates(installMaintenanceLogModel,edgeSLVDates);
    }

    private String getStartOfDay(String milliseconds){
        DateTime someDate = new DateTime(Long.valueOf(milliseconds), DateTimeZone.forTimeZone(TimeZone.getTimeZone("CST")));
        return String.valueOf(someDate.withTimeAtStartOfDay().getMillis());
    }


    public static void main(String[] r){
       String milliseconds = "1561629600000";
        DateTime someDate = new DateTime(Long.valueOf(milliseconds), DateTimeZone.forTimeZone(TimeZone.getTimeZone("CST")));
        String dateTime = String.valueOf(someDate.withTimeAtStartOfDay().getMillis());

        Date date = new Date(Long.valueOf(dateTime));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("CST"));
        String dff = dateFormat.format(date);


        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(DateTimeZone.forTimeZone(TimeZone.getTimeZone("CST")));
        DateTime dt =  fmt.parseDateTime(dff);

        System.out.println(String.valueOf(dt.getMillis()));
    }

    private void getSlvSyncDates(InstallMaintenanceLogModel installMaintenanceLogModel,SLVDates edgeSLVDates){
        SLVDates syncEdgeDates = new SLVDates();
        installMaintenanceLogModel.getDatesHolder().setSyncEdgeDates(syncEdgeDates);
        SLVDates slvDates = installMaintenanceLogModel.getDatesHolder().getSlvDates();

        logger.info("===========================");
        logger.info(gson.toJson(edgeSLVDates));
        logger.info(gson.toJson(slvDates));
        logger.info("===========================");

        // CSLP Node Install Dates
        String edgeCslpNodeDate =  installMaintenanceLogModel.getDatesHolder().compareSLVEdgeDate(edgeSLVDates.getCslpNodeDate(),slvDates.getCslpNodeDate(),true);
        logger.info("edgeCslpNodeDate::::"+edgeCslpNodeDate);
        if(edgeCslpNodeDate != null){
            boolean res =  isDatePresent(installMaintenanceLogModel.getIdOnController(),edgeCslpNodeDate, DateType.CSLP_NODE.toString());
            logger.info("res::::"+res);
            if(!res){
                syncEdgeDates.setCslpNodeDate(edgeCslpNodeDate);
            }
        }

        // CSLP Lum Install Dates
        String edgeCslpLumDate =   installMaintenanceLogModel.getDatesHolder().compareSLVEdgeDate(edgeSLVDates.getCslpLumDate(),slvDates.getCslpLumDate(),true);
        logger.info("edgeCslpLumDate::::"+edgeCslpLumDate);
        if(edgeCslpLumDate != null){
            boolean res =  isDatePresent(installMaintenanceLogModel.getIdOnController(),edgeCslpLumDate, DateType.CSLP_LUM.toString());
            logger.info("res::::"+res);
            if(!res){
                syncEdgeDates.setCslpLumDate(edgeCslpLumDate);
            }
        }


        //  Node Install Dates
        String edgeNodeDate =  installMaintenanceLogModel.getDatesHolder().compareSLVEdgeDate(edgeSLVDates.getNodeInstallDate(),slvDates.getNodeInstallDate(),false);
        logger.info("edgeNodeDate::::"+edgeNodeDate);
        if(edgeNodeDate != null){
            boolean res =  isDatePresent(installMaintenanceLogModel.getIdOnController(),edgeNodeDate, DateType.NODE.toString());
            logger.info("res::::"+res);
            if(!res){
                syncEdgeDates.setNodeInstallDate(edgeNodeDate);
            }
        }

        // CSLP Lum Install Dates
        String edgeLumInstallDate = installMaintenanceLogModel.getDatesHolder().compareSLVEdgeDate(edgeSLVDates.getLumInstallDate(),slvDates.getLumInstallDate(),false);
        logger.info("edgeLumInstallDate::::"+edgeLumInstallDate);
        if(edgeLumInstallDate != null){
            boolean res =  isDatePresent(installMaintenanceLogModel.getIdOnController(),edgeLumInstallDate, DateType.LUM.toString());
            logger.info("res::::"+res);
            if(!res){
                syncEdgeDates.setLumInstallDate(edgeLumInstallDate);
            }
        }
        logger.info(gson.toJson(syncEdgeDates));
    }


    /**
     * Check EdgeForm has CSLP node install date,CSLP lum install date,Install date, Lum Install date value.
     * If value is present, set those value to the corresponding fields and set date sync flag as true
     * @param loggingModel
     * @param paramsList
     */
    private void setEdgeDates(InstallMaintenanceLogModel loggingModel,List<Object> paramsList){
        DatesHolder datesHolder = loggingModel.getDatesHolder();
        logger.info("Populate EdgeForm Date Values.");
        if(datesHolder != null && datesHolder.getSyncEdgeDates() != null){
            SLVDates syncEdgeDates = datesHolder.getSyncEdgeDates();
            logger.info("SyncEdgeDates:"+syncEdgeDates.toString());
            // CSLP Node Install Date.
            boolean isCSLPNodeInstallDate = addDateParam("cslp.node.install.date",syncEdgeDates.getCslpNodeDate(),paramsList);
            logger.info("isCSLPNodeInstallDate added:"+isCSLPNodeInstallDate);
            datesHolder.setCslpNodeDateSynced(isCSLPNodeInstallDate);

            // CSLP Lum Install Date.
            boolean isCSLPLumInstallDate = addDateParam("cslp.lum.install.date",syncEdgeDates.getCslpLumDate(),paramsList);
            logger.info("isCSLPLumInstallDate added:"+isCSLPLumInstallDate);
            datesHolder.setCslpLumDateSynced(isCSLPLumInstallDate);

            // Install Date
            boolean isInstallDate = addDateParam("install.date",syncEdgeDates.getNodeInstallDate(),paramsList);
            logger.info("isInstallDate added:"+isInstallDate);
            datesHolder.setInstallDateSynced(isInstallDate);

            // Lum Install Date
            boolean isLumInstallDate = addDateParam("luminaire.installdate",syncEdgeDates.getLumInstallDate(),paramsList);
            logger.info("isLumInstallDate added:"+isLumInstallDate);
            datesHolder.setLumInstallDateSynced(isLumInstallDate);
        }
    }

    private boolean addDateParam(String paramName,String value,List<Object> paramsList){
        if(value != null && !value.trim().isEmpty()){
            addStreetLightData(paramName, dateFormat(Long.valueOf(value)), paramsList);
            return true;
        }
        return false;
    }



    private void syncEdgeDates(InstallMaintenanceLogModel installMaintenanceLogModel){
        logger.info("Edge Date Sync Process Starts.");
        if(installMaintenanceLogModel.getDatesHolder() != null && installMaintenanceLogModel.getDatesHolder().getSyncEdgeDates() != null){
            SLVDates syncEdgeDates = installMaintenanceLogModel.getDatesHolder().getSyncEdgeDates();
            logger.info("SyncEdgeDates:"+syncEdgeDates.toString());
            List<Object> paramsList = new ArrayList<>();

            if(syncEdgeDates.getCslpNodeDate() != null && !installMaintenanceLogModel.getDatesHolder().isCslpNodeDateSynced()){
                logger.info("CslpNodeInstallDate Added");
                addDateParam("cslp.node.install.date",syncEdgeDates.getCslpNodeDate(),paramsList);
            }

            if(syncEdgeDates.getCslpLumDate() != null && !installMaintenanceLogModel.getDatesHolder().isCslpLumDateSynced()){
                logger.info("CslpLumInstallDate Added");
                addDateParam("cslp.lum.install.date",syncEdgeDates.getCslpLumDate(),paramsList);
            }


            if(syncEdgeDates.getNodeInstallDate() != null && !installMaintenanceLogModel.getDatesHolder().isInstallDateSynced()){
                logger.info("Install Date Added");
                addDateParam("install.date",syncEdgeDates.getNodeInstallDate(),paramsList);
            }


            if(syncEdgeDates.getLumInstallDate() != null && !installMaintenanceLogModel.getDatesHolder().isLumInstallDateSynced()){
                logger.info("Luminaire Install Date Added");
                addDateParam("luminaire.installdate",syncEdgeDates.getLumInstallDate(),paramsList);
            }

            if(paramsList.size() > 0){
                logger.info("Date value is Present. Syncing Date value....");
                String idOnController = installMaintenanceLogModel.getIdOnController();
                paramsList.add("idOnController=" + idOnController);
                paramsList.add("controllerStrId=" + installMaintenanceLogModel.getControllerSrtId());
                SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(installMaintenanceLogModel);
                int errorCode = setDeviceValues(paramsList, slvTransactionLogs);
                createAllSLVDate(syncEdgeDates,idOnController);
                logger.info("Error Code:"+errorCode);
            }else{
                logger.info("No Date value is present");
            }

        }

    }

    /**
     * Save Edgeform date value to the local db. So that, we can avoid to resend same date again.
     * @param slvDates
     * @param idOnController
     */
    private void createAllSLVDate(SLVDates slvDates,String idOnController){
        logger.info("createAllSLVDate Process Starts");
        if(slvDates != null){
            saveEdgeSLVDate(slvDates.getCslpNodeDate(),DateType.CSLP_NODE.toString(),idOnController);
            saveEdgeSLVDate(slvDates.getCslpLumDate(),DateType.CSLP_LUM.toString(),idOnController);
            saveEdgeSLVDate(slvDates.getNodeInstallDate(),DateType.NODE.toString(),idOnController);
            saveEdgeSLVDate(slvDates.getLumInstallDate(),DateType.LUM.toString(),idOnController);
        }


    }

    private void saveEdgeSLVDate(String date,String dateType,String idOnController){
        if(date != null){
            connectionDAO.deleteEdgeNoteFormDate(idOnController,dateType);
            EdgeSLVDate edgeSLVDate = new EdgeSLVDate();
            edgeSLVDate.setTitle(idOnController);
            edgeSLVDate.setEdgeDate(date);
            edgeSLVDate.setDatesType(dateType);
            connectionDAO.saveEdgeNodeDate(edgeSLVDate);
            logger.info("EdgeSLVDate Saved..");
        }

    }



    /**
     * Update Promoted value if any four date has value.
     * @param slvDates
     * @param idOnController
     */
    public void updatePromotedFormData(SLVDates slvDates,String idOnController,InstallMaintenanceLogModel installMaintenanceLogModel){
        try{
            if(slvDates != null){
                boolean hasData = false;
                PromotedFormData promotedFormData = new PromotedFormData();
                promotedFormData.setIdonController(idOnController);
                if(slvDates.getCslpLumDate() != null){
                    hasData = true;
                   // promotedFormData.setCslpLumInstallDate(slvDateFormat(slvDates.getCslpLumDate(),"Promote"));
                    promotedFormData.setCslpLumInstallDate(slvDates.getCslpLumDate());
                }
                if(slvDates.getCslpNodeDate() != null){
                    hasData = true;
                   // promotedFormData.setCslpNodeInstallDate(slvDateFormat(slvDates.getCslpNodeDate(),"Promote"));
                    promotedFormData.setCslpNodeInstallDate(slvDates.getCslpNodeDate());
                }
                if(slvDates.getNodeInstallDate() != null){
                    hasData = true;
                   // promotedFormData.setInstallDate(slvDateFormat(slvDates.getNodeInstallDate(),"Promote"));
                    promotedFormData.setInstallDate(slvDates.getNodeInstallDate());
                }
                if(slvDates.getLumInstallDate() != null){
                    hasData = true;
                   // promotedFormData.setLumInstallDate(slvDateFormat(slvDates.getLumInstallDate(),"Promote"));
                    promotedFormData.setLumInstallDate(slvDates.getLumInstallDate());
                }

                if(hasData){
                    if(installMaintenanceLogModel.isInstallOnWrongFix()){
                        promotedFormData.setCslpLumInstallDate("");
                        promotedFormData.setCslpNodeInstallDate("");
                        promotedFormData.setInstallDate("");
                        promotedFormData.setLumInstallDate("");
                    }else if(installMaintenanceLogModel.isPoleKnockDown() && installMaintenanceLogModel.isMacRemoved()){ // Installation Removed.
                        promotedFormData.setInstallDate("");
                        promotedFormData.setLumInstallDate("");
                    }

                     String requestJson = gson.toJson(promotedFormData);
                     String edgeSlvUrl = PropertiesReader.getProperties().getProperty("streetlight.edge.slvserver.url");
                     edgeSlvUrl = edgeSlvUrl+"/updatePromotedFormDates";
                    serverCall(edgeSlvUrl,HttpMethod.POST,requestJson);
                }else{
                    logger.info("No Dates available to update promoted data");
                }
            }
        }catch (Exception e){
            logger.error("Error in updatePromotedFormData",e);
        }

    }


    public ResponseEntity<String> serverCall(String url, HttpMethod httpMethod, String body) {
        logger.info("Request Url : " + url);
        logger.info("Request Data : " + body);
        RestTemplate restTemplate = getRestTemplate();
        HttpHeaders headers =new HttpHeaders();

        HttpEntity request = null;
        if (body != null) {
            headers.add("Content-Type", "application/json");
            request = new HttpEntity<String>(body, headers);
        } else {
            request = new HttpEntity<>(headers);
        }

        ResponseEntity<String> responseEntity = restTemplate.exchange(url, httpMethod, request, String.class);
        logger.info("------------ Response ------------------");

        logger.info("Response Code:" + responseEntity.getStatusCode().toString());
        if (responseEntity.getBody() != null) {
            logger.info("Response Data:" + responseEntity.getBody());
        }

        return responseEntity;
    }




    public void removeEdgeSLVMacAddress(String idOnController){
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("slvIdOnController",idOnController);
        restService.slv2Edge("/rest/validation/removeSLVMacAddress", HttpMethod.GET,params);
        syncMacAddress2Promoted(idOnController,null);

    }

    private String addUserToLuminaireSerialNumber(String luminaireSerialNumber,String user){
        return luminaireSerialNumber+":"+user;
    }



    private EdgeAllSerialNumber checkLuminaireSerialNumberSync(String serialNumber,String idOnController,InstallMaintenanceLogModel installMaintenanceLogModel)throws  SerialNumberSyncedException{
        EdgeAllSerialNumber edgeAllSerialNumber = null;
        try{
            // Get Serial Number from DB
            edgeAllSerialNumber = connectionDAO.getEdgeAllSerialNumber(idOnController);
            // If its, present then check current value matches with DB
            if(edgeAllSerialNumber != null){
                if(edgeAllSerialNumber.getSerialNumber().trim().toLowerCase().contains(serialNumber.trim().toLowerCase())){
                    logger.info("Serial Number already Synced with SLV.");
                    throw  new SerialNumberSyncedException("Given Serial Number already Synced with SLV");
                }else{
                    // Check given value present in SLV.
                    checkSLVSerialNumber(serialNumber,idOnController,installMaintenanceLogModel);
                }
            }else{
                checkSLVSerialNumber(serialNumber,idOnController,installMaintenanceLogModel);
            }
        }catch (SerialNumberSyncedException e){
            throw  new SerialNumberSyncedException("Given Serial Number already Synced with SLV");
        }catch (Exception e){
            logger.error("Error in checkLuminaireSerialNumber",e);
        }
        return  edgeAllSerialNumber;
    }


    private void checkSLVSerialNumber(String serialNumber,String idOnController,InstallMaintenanceLogModel installMaintenanceLogModel)throws  SerialNumberSyncedException{
        String slvLuminaireSerialNumber =  getSLVValues(installMaintenanceLogModel,"luminaire.serialnumber");
        if(slvLuminaireSerialNumber != null && !slvLuminaireSerialNumber.trim().isEmpty()){
            if(slvLuminaireSerialNumber.trim().toLowerCase().contains(serialNumber.trim().toLowerCase())){
                logger.info("Serial Number already Synced with SLV.");
                throw  new SerialNumberSyncedException("Given Serial Number already Synced with SLV");
            }
        }
    }


    private void addEdgeAllSerialNumber(String serialNumber,String idOnController){
        if(serialNumber != null && !serialNumber.trim().isEmpty()){
            EdgeAllSerialNumber edgeAllSerialNumber = connectionDAO.getEdgeAllSerialNumber(idOnController);
            if(edgeAllSerialNumber != null){
                edgeAllSerialNumber.setSerialNumber(serialNumber);
                connectionDAO.updateEdgeAllSerialNumber(edgeAllSerialNumber);
            }else{
                edgeAllSerialNumber = new EdgeAllSerialNumber();
                edgeAllSerialNumber.setSerialNumber(serialNumber);
                edgeAllSerialNumber.setTitle(idOnController);
                connectionDAO.createEdgeAllSerialNumber(edgeAllSerialNumber);

            }
        }

    }



    private void addInstallationRemovedExpReport(String macAddress,EdgeNote edgeNote,String communicationStatus){
        InstallationRemovedExceptionReport installationRemovedExceptionReport = new InstallationRemovedExceptionReport();
        installationRemovedExceptionReport.setIdOnController(edgeNote.getTitle());
        installationRemovedExceptionReport.setCreatedBy(edgeNote.getCreatedBy());
        installationRemovedExceptionReport.setCreatedDateTime(edgeNote.getCreatedDateTime());
        installationRemovedExceptionReport.setEventTime(System.currentTimeMillis());
        installationRemovedExceptionReport.setMacAddress(macAddress);
        installationRemovedExceptionReport.setCommunicationStatus(communicationStatus);
        connectionDAO.addInstallationRemovedReport(installationRemovedExceptionReport);
    }


    private void cnrWorkFlow(List<EdgeFormData> edgeFormDatas, EdgeNote edgeNote, InstallMaintenanceLogModel loggingModel, SlvInterfaceLogEntity slvInterfaceLogEntity){
        try{
            // Get MAC value
            String macAddress = null;
            try {
                macAddress = valueById(edgeFormDatas, 180);
                logger.info("CNR MAC Address:"+macAddress);
            } catch (NoValueException e) {

            }
            logger.info("CNR MAC Address:"+macAddress);
            DeviceAttributes deviceAttributes = getDeviceValues(loggingModel);
            // If MAC Address is empty or null, then we need to call Empty Replace OLC with Install Status as "To be verified"
            if(macAddress == null || macAddress.trim().isEmpty()){
                loggingModel.setCNRNoMAC(true);
                logger.info("MAC Address is empty, so Remove MAC Address from SLV.");
                if (deviceAttributes != null && deviceAttributes.getMacAddress() != null) {
                    String idOnController = loggingModel.getIdOnController();
                    String controllerStrIdValue = loggingModel.getControllerSrtId();
                    logger.info("Empty Replace OLC Called.");
                    callReplaceOLC(loggingModel,slvInterfaceLogEntity,"",edgeNote);
                    connectionDAO.removeEdgeAllMAC(loggingModel.getIdOnController(),deviceAttributes.getMacAddress());
                    removeEdgeSLVMacAddress(loggingModel.getIdOnController());

                }
                String idOnController = loggingModel.getIdOnController();
                String controllerStrIdValue = loggingModel.getControllerSrtId();
                List<Object> paramsList = new ArrayList<>();
                paramsList.add("idOnController=" + idOnController);
                paramsList.add("controllerStrId=" + controllerStrIdValue);
                addStreetLightData("installStatus", InstallStatus.To_be_verified.getValue(), paramsList);
                addStreetLightData("install.date", dateFormat(edgeNote.getCreatedDateTime()), paramsList);
                logger.info("Set Device value Called.");
                SLVTransactionLogs slvTransactionLogsSetDevice = getSLVTransactionLogs(loggingModel);
                int errorCode = setDeviceValues(paramsList, slvTransactionLogsSetDevice);
                if (errorCode == 0) {
                    logger.info("clearing device values completed: " + idOnController);
                } else {
                    logger.error("Error in clearDeviceValues");
                }

            }else{
                String idOnController = loggingModel.getIdOnController();
                String controllerStrIdValue = loggingModel.getControllerSrtId();
                try {
                    checkMacAddressExists(macAddress, idOnController, null, null, loggingModel, slvInterfaceLogEntity);
                } catch (QRCodeAlreadyUsedException e1) {
                    slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
                    slvInterfaceLogEntity.setErrorcategory(MessageConstants.EDGE_VALIDATION_ERROR);
                    slvInterfaceLogEntity.setErrordetails("MacAddress Already use. So this pole not synced with slv");
                    logger.error("MacAddress (" + e1.getMacAddress()
                            + ")  - Already in use. So this pole is not synced with SLV. Note Title :[" + edgeNote.getTitle()
                            + " ]");
                    loggingModel.setStatus(MessageConstants.ERROR);
                    loggingModel.setErrorDetails("MacAddress (" + e1.getMacAddress()
                            + ")  - Already in use. So this pole is not synced with SLV. Note Title :[" + edgeNote.getTitle()
                            + " ]");
                    return;
                }
                logger.info("Captured MAC Address not Assigned to other Fixtures");
                if (deviceAttributes != null && deviceAttributes.getMacAddress() != null) {
                    logger.info("MAC Address is present in SLV. So First Call Empty Replace OLC.");
                    callReplaceOLC(loggingModel,slvInterfaceLogEntity,"",edgeNote);
                    connectionDAO.removeEdgeAllMAC(loggingModel.getIdOnController(),deviceAttributes.getMacAddress());
                    removeEdgeSLVMacAddress(loggingModel.getIdOnController());
                }
                   logger.info("Replace OLC with New MAC Address");
                    callReplaceOLC(loggingModel,slvInterfaceLogEntity,macAddress,edgeNote);

                    List<Object> paramsList = new ArrayList<>();
                    paramsList.add("idOnController=" + idOnController);
                    paramsList.add("controllerStrId=" + controllerStrIdValue);
                    InstallStatus installStatus = InstallStatus.Installed;
                    if(loggingModel.getProposedContext() != null && loggingModel.getProposedContext().trim().contains("Node Only")){
                        installStatus = InstallStatus.Node_Only;
                    }
                    addStreetLightData("installStatus", installStatus.getValue(), paramsList);
                    addStreetLightData("install.date", dateFormat(edgeNote.getCreatedDateTime()), paramsList);
                    addStreetLightData("MacAddress", macAddress, paramsList);

                    SLVTransactionLogs slvTransactionLogsSetDevice = getSLVTransactionLogs(loggingModel);
                    int errorCode = setDeviceValues(paramsList, slvTransactionLogsSetDevice);
                    if (errorCode == 0) {
                        logger.info("clearing device values completed: " + idOnController);
                    } else {
                        logger.error("Error in clearDeviceValues");
                    }

                    loggingModel.setStatus(MessageConstants.SUCCESS);
                    slvInterfaceLogEntity.setStatus(MessageConstants.SUCCESS);

                    logger.info("CNR Success.");

            }
        }catch (Exception e){
            logger.error("Error in cnrWorkFlow",e);
            slvInterfaceLogEntity.setStatus(MessageConstants.ERROR);
            slvInterfaceLogEntity.setErrorcategory(MessageConstants.SLV_VALIDATION_ERROR);
            slvInterfaceLogEntity.setErrordetails(e.getMessage());
            loggingModel.setStatus(MessageConstants.ERROR);
            loggingModel.setErrorDetails(e.getMessage());
        }



    }


    private void callReplaceOLC(InstallMaintenanceLogModel loggingModel,SlvInterfaceLogEntity slvInterfaceLogEntity,String macAddress,EdgeNote edgeNote)throws ReplaceOLCFailedException{
        String idOnController = loggingModel.getIdOnController();
        String controllerStrIdValue = loggingModel.getControllerSrtId();
        SLVTransactionLogs slvTransactionLogs = getSLVTransactionLogs(loggingModel);
        // Only for Testing -- TODO
        try{
            replaceOLC(controllerStrIdValue, idOnController, macAddress, slvTransactionLogs, slvInterfaceLogEntity,loggingModel.getAtlasPhysicalPage(),loggingModel,edgeNote);
        }catch (Exception e){

        }

    }



}
