package com.terragoedge.slvinterface.maintenanceworkflow;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.slvinterface.exception.InValidBarCodeException;
import com.terragoedge.slvinterface.exception.NoDataChangeException;
import com.terragoedge.slvinterface.exception.NoValueException;
import com.terragoedge.slvinterface.exception.SkipNoteException;
import com.terragoedge.slvinterface.maintenanceworkflow.model.ActionTypeJson;
import com.terragoedge.slvinterface.maintenanceworkflow.model.DataDiffResponse;
import com.terragoedge.slvinterface.maintenanceworkflow.model.DataDiffValueHolder;
import com.terragoedge.slvinterface.model.*;
import com.terragoedge.slvinterface.service.AbstractSlvService;
import com.terragoedge.slvinterface.service.EdgeService;
import com.terragoedge.slvinterface.service.SlvService;
import com.terragoedge.slvinterface.utils.PropertiesReader;
import com.terragoedge.slvinterface.utils.Utils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*

streetlight.edge.Maintenance.formtemplate.guid // TODO

 */

public class MaintenanceWorkflowService extends AbstractSlvService {

    private Gson gson;
    private JsonParser jsonParser;
    private Properties properties;

    public MaintenanceWorkflowService() {
        gson = new Gson();
        jsonParser = new JsonParser();
        this.properties = PropertiesReader.getProperties();
    }

    final Logger logger = Logger.getLogger(MaintenanceWorkflowService.class);

    public void processMaintenanceWorkflow(EdgeNote edgeNote,WorkFlowFormId maintenanceInstallFormId) throws NoDataChangeException, SkipNoteException {
        try {
            logger.info("******* processMaintenanceWorkflow **********");
            List<FormData> formDataListTemp = edgeNote.getFormData();
            List<FormData> formDataList = new ArrayList<>();

            String actionTypeJson = properties.getProperty("streetlight.edge.actiontype");
            if(actionTypeJson == null || actionTypeJson.trim().isEmpty()){
                logger.info("Action Type config is not present.");
                throw  new SkipNoteException("Action Type config is not present.");
            }

           List<ActionTypeJson> actionTypeJsonList = gson.fromJson(actionTypeJson, new TypeToken<List<ActionTypeJson>>() {
            }.getType());

            Map<String, List<Integer>> listMap =new HashMap<>();

            for(ActionTypeJson actionTypeJson1 : actionTypeJsonList){
                listMap.put(actionTypeJson1.getActionType(),actionTypeJson1.getIds());
            }

            String idOnController = edgeNote.getTitle();

            String formTemplateGuid = PropertiesReader.getProperties().getProperty("streetlight.edge.Maintenance.formtemplate.guid");
            logger.info("Maintenance Form Template Guid:" + formTemplateGuid);
            for (FormData formData : formDataListTemp) {
                if (formData.getFormTemplateGuid().equals(formTemplateGuid)) {
                    formDataList.add(formData);
                }

            }
            int size = formDataList.size();
            logger.info("Maintenance Form Template:" + size);
            if (size > 0) {
                if(size > 1){
                    logger.info("More than one form is present.");
                    throw  new SkipNoteException("More than one form is present.");
                }
                DataDiffResponse dataDiffResponse = compareRevisionData(edgeNote.getNoteGuid());
                if (dataDiffResponse != null) {
                    for (FormData formData : formDataList) {
                        List<EdgeFormData> edgeFormDataList = formData.getFormDef();
                        String actionType = getActionType(dataDiffResponse,listMap);
                        logger.info("Action Type:"+actionType);
                        if(actionType == null){
                            throw  new NoDataChangeException("Action Type");
                        }
                        switch (actionType){
                            case "install":
                                logger.info("Install Action....");
                                processMaintenanceInstallForm(formDataList,edgeNote,maintenanceInstallFormId);
                                break;

                            case "replace_led_light":
                                logger.info("serialnumber Action....");
                                if(isDevicePresent(idOnController)){
                                    JPSWorkflowModel jpsWorkflowModel = getJPSWorkflowModel(edgeNote,idOnController);
                                    processLedLight(283,edgeFormDataList,idOnController,jpsWorkflowModel);
                                }else {
                                    logger.info("Device not present.");
                                }

                                break;
                            case "replace_smart_controller":
                                logger.info("ReplaceOLC Action....");
                                if(isDevicePresent(idOnController)){
                                    JPSWorkflowModel jpsWorkflowModel = getJPSWorkflowModel(edgeNote,idOnController);
                                    processReplaceSmartController(271,edgeFormDataList,idOnController,edgeNote,jpsWorkflowModel);
                                }else {
                                    logger.info("Device not present.");
                                }

                                break;
                            case "replace_led_smart_controller":
                                logger.info("serialnumber and ReplaceOLC Action....");
                                if(isDevicePresent(idOnController)){
                                    JPSWorkflowModel jpsWorkflowModel = getJPSWorkflowModel(edgeNote,idOnController);
                                    processLedLight(10102,edgeFormDataList,idOnController,jpsWorkflowModel);
                                    processReplaceSmartController(10096,edgeFormDataList,idOnController,edgeNote,jpsWorkflowModel);
                                }else {
                                    logger.info("Device not present.");
                                }


                                break;
                            case "remove":
                                logger.info("Remove Action....");
                                if(isDevicePresent(idOnController)){
                                    JPSWorkflowModel jpsWorkflowModel = getJPSWorkflowModel(edgeNote,idOnController);
                                    processRemoveWorkFlow(idOnController,edgeNote,jpsWorkflowModel);
                                }else {
                                    logger.info("Device not present.");
                                }
                                break;
                        }
                    }

                }

            }else{
                throw new  NoDataChangeException("");
            }
        } catch (Exception e) {
            logger.error("Error in processMaintenanceWorkflow", e);
            if(e instanceof NoDataChangeException){
                throw new NoDataChangeException(e.getMessage());
            }
            throw new SkipNoteException(e.getMessage());

        }
    }


    private boolean isDevicePresent(String idOnController){
        JsonArray devices = checkDeviceExist(idOnController);
        return  devices != null && devices.size() > 0;
    }


    private void processMaintenanceInstallForm(List<FormData> formDatasList,EdgeNote edgeNote,WorkFlowFormId installWorkflowFormId){
        try {
            // List<FormData> formDatasList should have maintenance forms only
            JPSWorkflowModel jpsWorkflowModel = processWorkFlowForm(formDatasList, edgeNote, installWorkflowFormId);
            jpsWorkflowModel.setInstallStatus("CONVERTED");
            SlvService slvService = new SlvService();
            slvService.processSlv(jpsWorkflowModel, edgeNote);
        }catch (Exception e){
            logger.error("Error in processMaintenanceInstallForm : ",e);
        }
    }

    private void processReplaceSmartController(int formId, List<EdgeFormData> edgeFormDataList, String idOnController,EdgeNote edgeNote,JPSWorkflowModel jpsWorkflowModel) throws NoValueException {
        String macAddress = valueById(edgeFormDataList, formId);
        List<Value> valueList = checkMacAddressExists(macAddress);
        if (valueList != null && valueList.size() > 0) {
            for (Value value : valueList) {
                if (!value.getIdOnController().equals(idOnController)) {
                    logger.info("MAC Address already use.");
                    return;
                }
            }
        }

        try{
            replaceOLC(jpsWorkflowModel.getControllerStrId(), idOnController, "", edgeNote, jpsWorkflowModel);
        }catch (Exception e){
            logger.error("Error in processReplaceSmartController",e);
        }

        try{
            replaceOLC(jpsWorkflowModel.getControllerStrId(), idOnController, macAddress, edgeNote, jpsWorkflowModel);
        }catch (Exception e){
            logger.error("Error in processReplaceSmartController",e);
        }


    }


    private void processRemoveWorkFlow( String idOnController,EdgeNote edgeNote,JPSWorkflowModel jpsWorkflowModel){
        try{
            replaceOLC(jpsWorkflowModel.getControllerStrId(), idOnController, "", edgeNote, jpsWorkflowModel);
        }catch (Exception e){
            logger.error("Error in processReplaceSmartController",e);
        }

        List<Object> paramList = new ArrayList<>();
        paramList.add("idOnController=" + encode(idOnController));
        paramList.add("controllerStrId=" + jpsWorkflowModel.getControllerStrId());
        addStreetLightData("idOnController", idOnController, paramList);
        addStreetLightData("installStatus", "Removed", paramList);
        ResponseEntity<String> responseEntity = setDeviceValues(paramList);
        logger.info("********************** set device values reponse code: " + responseEntity.getStatusCode());
        logger.info("set device values response: " + responseEntity.getBody());
        logger.info("********************** set device values reponse end *********");
    }




    private void processLedLight(int formId, List<EdgeFormData> edgeFormDataList, String idOnController,JPSWorkflowModel jpsWorkflowModel){
            String serialNumber = valueById(edgeFormDataList, formId);
            if(!serialNumber.trim().isEmpty()){
                List<Object> paramList = new ArrayList<>();
                paramList.add("idOnController=" + encode(idOnController));
                paramList.add("controllerStrId=" + jpsWorkflowModel.getControllerStrId());
                addStreetLightData("idOnController", idOnController, paramList);
                addStreetLightData("device.node.serialnumber", serialNumber, paramList);
                ResponseEntity<String> responseEntity = setDeviceValues(paramList);
                logger.info("********************** set device values reponse code: " + responseEntity.getStatusCode());
                logger.info("set device values response: " + responseEntity.getBody());
                logger.info("********************** set device values reponse end *********");
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
        String baseUrl = properties.getProperty("streetlight.edge.url.main");
        String url = PropertiesReader.getProperties().getProperty("streetlight.edge.url.checkrevisiondata");
        String config = PropertiesReader.getProperties().getProperty("streetlight.edge.checkrevisiondata.config");
        url = baseUrl+url;
        JsonObject configJson = (JsonObject) jsonParser.parse(config);
        configJson.addProperty("noteGuid", noteGuid);
        logger.info("Given url is :" + url);
        // Compare Revision data to identify any changes or not.
        ResponseEntity<String> responseEntity = serverCall(url, HttpMethod.POST, configJson.toString());
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

    private String getActionType(DataDiffResponse dataDiffResponse, Map<String, List<Integer>> listMap) {
        Set<String> keySets = listMap.keySet();
        List<DataDiffValueHolder> dataDiff = dataDiffResponse.getDataDiff();
        logger.info("Check Data change Action.....");
        for (String actionType : keySets) {
            List<Integer> idList = listMap.get(actionType);
            for (Integer ids : idList) {
                DataDiffValueHolder temp = new DataDiffValueHolder();
                temp.setId(ids);
                int pos = dataDiff.indexOf(temp);
                if (pos != -1) {
                    logger.info("Action Type....."+actionType);
                    return actionType;
                }
            }
        }
        return null;

    }

    @Override
    public void buildFixtureStreetLightData(String data, List<Object> paramsList, EdgeNote edgeNote) throws InValidBarCodeException {

    }



    public JPSWorkflowModel getJPSWorkflowModel( EdgeNote edgeNote,String idOnController) {
        JPSWorkflowModel jpsWorkflowModel = new JPSWorkflowModel();
        jpsWorkflowModel.setIdOnController(idOnController);
        if (edgeNote.getEdgeNotebook() != null) {
            jpsWorkflowModel.setNotebookName(edgeNote.getEdgeNotebook().getNotebookName());
            jpsWorkflowModel.setDimmingGroupName(edgeNote.getEdgeNotebook().getNotebookName());
        }
        String categoryStrId = properties.getProperty("streetlight.categorystr.id");
        String controllerStrId = properties.getProperty("streetlight.controller.str.id");

        String nodeTypeStrId = properties.getProperty("streetlight.slv.equipment.type");
        jpsWorkflowModel.setControllerStrId(controllerStrId);
        jpsWorkflowModel.setEquipmentType(nodeTypeStrId);
        jpsWorkflowModel.setProvider_name(properties.getProperty("jps.provider.name"));
        jpsWorkflowModel.setLowvoltagethreshold(Integer.valueOf(properties.getProperty("jps.low.voltage.thershold")));
        jpsWorkflowModel.setHighvoltagethreshold(Integer.valueOf(properties.getProperty("jps.high.voltage.thershold")));
        jpsWorkflowModel.setCategoryStrId(categoryStrId);
        jpsWorkflowModel.setLocationtype(properties.getProperty("jps.location.type"));
        jpsWorkflowModel.setModel(properties.getProperty("jps.model"));
        String geozonePaths = "/" + jpsWorkflowModel.getNotebookName() + "/" + jpsWorkflowModel.getAddress1();
        jpsWorkflowModel.setGeozonePath(geozonePaths);
        return jpsWorkflowModel;
    }
}
