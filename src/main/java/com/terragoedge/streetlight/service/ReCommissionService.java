package com.terragoedge.streetlight.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.terragoedge.edgeserver.DeviceMacAddress;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.Value;
import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.dao.CommissionFailureData;
import com.terragoedge.streetlight.dao.ConnectionDAO;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

public class ReCommissionService extends AbstractProcessor {
    private RestService restService;
    private ConnectionDAO connectionDAO;
    private Logger logger = Logger.getLogger(ReCommissionService.class);
    private JsonParser jsonParser;
    private Properties properties;
    public ReCommissionService() {
        properties = PropertiesReader.getProperties();
        jsonParser = new JsonParser();
        restService = new RestService();
        connectionDAO = ConnectionDAO.INSTANCE;
    }

    public void start(){
        String accessToken = getEdgeToken();
        String url = properties.getProperty("streetlight.edge.url.main");

        List<CommissionFailureData> commissionFailureDatas = connectionDAO.getCommissionFailureDatas();

        for(CommissionFailureData commissionFailureData : commissionFailureDatas){
            String idoncontroller = commissionFailureData.getIdoncontroller();
            try {
                List<List<EdgeFormData>> formDefMaps = new ArrayList<>();
                ResponseEntity<String> responseEntity = restService.getRequest(url + "/notesdata/" + idoncontroller, false, accessToken);
                if (responseEntity.getStatusCode() == HttpStatus.OK) {
                    String body = responseEntity.getBody();
                    JsonArray notesArry = jsonParser.parse(body).getAsJsonArray();
                    for (JsonElement noteElement : notesArry) {
                        JsonObject edgeJsonObject = noteElement.getAsJsonObject();
                        String parentNoteGuid = edgeJsonObject.get("baseParentNoteId").isJsonNull() ? null : edgeJsonObject.get("baseParentNoteId").getAsString();
                        if (parentNoteGuid.equals(commissionFailureData.getParentnoteguid())) {
                            JsonArray serverEdgeFormJsonArray = edgeJsonObject.get("formData").getAsJsonArray();
                            int size = serverEdgeFormJsonArray.size();
                            for (int i = 0; i < size; i++) {
                                JsonObject serverEdgeForm = serverEdgeFormJsonArray.get(i).getAsJsonObject();
                                if (serverEdgeForm.get("formTemplateGuid").equals(properties.getProperty("amerescousa.edge.formtemplateGuid"))) {
                                    String formDefJson = serverEdgeForm.get("formDef").toString();
                                    formDefJson = formDefJson.replaceAll("\\\\", "");
                                    List<EdgeFormData> formDataList = getEdgeFormData(formDefJson);
                                    formDefMaps.add(formDataList);
                                }
                            }
                            reCommissionFailureData(formDefMaps, idoncontroller, commissionFailureData);
                        }
                    }
                } else {
                    logger.error("Error while getting this fixture: " + commissionFailureData.getIdoncontroller());
                }
            }catch (Exception e){
                logger.error("Error while processing this fixture: "+idoncontroller, e);
            }
        }
    }

    private List<EdgeFormData> getEdgeFormData(String data) {
        try {
            List<EdgeFormData> edgeFormDatas = gson.fromJson(data, new TypeToken<List<EdgeFormData>>() {
            }.getType());
            return edgeFormDatas;
        } catch (Exception e) {
            data = data.substring(1, data.length() - 1);
            List<EdgeFormData> edgeFormDatas = gson.fromJson(data, new TypeToken<List<EdgeFormData>>() {
            }.getType());
            return edgeFormDatas;
        }
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

    private void reCommissionFailureData(List<List<EdgeFormData>> formDefMaps, String idoncontroller, CommissionFailureData commissionFailureData){
        if(formDefMaps.size() > 1){
            logger.error("There are one or more installtion forms: "+idoncontroller);
            return;
        }else if(formDefMaps.size() == 0){
            logger.error("There are no installtion forms: "+idoncontroller);
            return;
        }
        boolean removeCommissionFailureData =false;
        String currentMacAddress = "";
        List<EdgeFormData> formDataList = formDefMaps.get(0);
        String action = getFormValue(formDataList,17);
        if(action != null && !action.equals("")){
            switch (action){
                case "New":
                    String installStatus = getFormValue(formDataList,22);
                    if(installStatus != null && !installStatus.equals("")){
                        if(installStatus.equals("Complete")) {
                            currentMacAddress = getFormValue(formDataList, 19);
                        } else if(installStatus.equals("Could not complete")){
                            removeCommissionFailureData = true;
                        }else if(installStatus.equals("Button Photocell Installation")){
                            removeCommissionFailureData = true;
                        }
                    }else{
                        logger.error("The fixture has new action but install status not selected: "+idoncontroller);
                        return;
                    }
                    break;
                case "Repairs & Outages":
                    String replaceWorkflow = getFormValue(formDataList,24);
                    if(replaceWorkflow != null && !replaceWorkflow.equals("")){
                        switch (replaceWorkflow){
                            case "Replace Node and Fixture":
                                currentMacAddress = getFormValue(formDataList,26);
                                break;
                            case "Replace Node only":
                                currentMacAddress = getFormValue(formDataList,30);
                                break;
                            case "CIMCON Node Replacements JBCC Only":
                                currentMacAddress = getFormValue(formDataList,180);
                                break;
                            case "COC Replacement":
                                currentMacAddress = getFormValue(formDataList,185);
                                break;
                        }
                    }else{
                        logger.error("the fixture has Repairs & Outages action but workflow is not selected: "+idoncontroller);
                        return;
                    }
                    break;
                case "Remove":
                    removeCommissionFailureData = true;
                    break;
            }
        }else{
            logger.error("No action selected in this fixture: "+idoncontroller);
            return;
        }

        if(removeCommissionFailureData && (currentMacAddress == null || currentMacAddress.equals(""))){
            connectionDAO.deleteCommissionFailure(commissionFailureData);
        } else if(currentMacAddress == null || currentMacAddress.equals("")){// ex: replace fixture only
            //recommission here
            recommission(commissionFailureData.getMacaddress(),idoncontroller,commissionFailureData);

        } else if (currentMacAddress.equals(commissionFailureData.getMacaddress())){// same mac address
            //recommission here
            recommission(commissionFailureData.getMacaddress(),idoncontroller,commissionFailureData);
        } else{// diff mac address
            connectionDAO.deleteCommissionFailure(commissionFailureData);
        }
    }

    private void recommission(String macaddress,String idoncontroller,CommissionFailureData commissionFailureData){
        if(macaddress == null || macaddress.equals("")){
            logger.error("The macaddress to be recommissioned is null or empty. Idoncontroller: "+idoncontroller+" macaddress: "+macaddress);
            return;
        }
        List<Value> values = checkMacAddressExists(macaddress);
        if(values == null){
           logger.error("Error while calling checkMacAddressExists");
           return;
        }else if(values.size() == 0) {
            // mac address not used. call replaceOLC
            replaceOLC(idoncontroller,macaddress,commissionFailureData);
        }else {
            for(Value value : values){
                if(value.getIdOnController().equals(idoncontroller)){
                    logger.info("This macaddress already assigned to same idoncontroller in SLV. Idoncontroller: "+idoncontroller+" macaddress: "+macaddress);
                    //delete commission failure data
                    connectionDAO.deleteCommissionFailure(commissionFailureData);
                    return;
                }else{
                    logger.error("This macaddress is already assigned to another idoncontroller in SLV: Idoncontroller: "+idoncontroller+" macaddress: "+macaddress+" Assigned idoncontroller: "+value.getIdOnController());
                    return;
                }
            }
        }
    }


    private List<Value> checkMacAddressExists(String macAddress){
        List<Value> values = new ArrayList<>();
        try {
            String mainUrl = properties.getProperty("streetlight.slv.url.main");
            String updateDeviceValues = properties.getProperty("streetlight.slv.url.search.device");
            String url = mainUrl + updateDeviceValues;
            List<String> paramsList = new ArrayList<>();
            paramsList.add("attribute=MacAddress");
            paramsList.add("value=" + macAddress);
            paramsList.add("operator=eq-i");
            paramsList.add("recurse=true");
            paramsList.add("ser=json");
            String params = StringUtils.join(paramsList, "&");
            url = url + "?" + params;
            ResponseEntity<String> response = restService.getRequest(url, true, null);
            if (response.getStatusCodeValue() == 200) {
                String responseString = response.getBody();
                logger.info("-------MAC Address----------");
                logger.info(responseString);
                logger.info("-------MAC Address End----------");
                DeviceMacAddress deviceMacAddress = gson.fromJson(responseString, DeviceMacAddress.class);
                values = deviceMacAddress.getValue();
                logger.info("check mac address exist values:" + values);
            } else {
                values = null;
                logger.error("Error while check mac address already used on not. Err: " + response.getBody());
            }
        }catch (Exception e){
            values = null;
            logger.error("Error while calling checkMacAddressExists: ",e);
        }
            return values;
    }


    public void replaceOLC(String idOnController, String macAddress,CommissionFailureData commissionFailureData)
             {
        try {
            String controllerStrIdValue = properties.getProperty("streetlight.slv.controllerstrid");
            logger.info("Replace OLC Process start.");
            // Get Url detail from properties
            String mainUrl = properties.getProperty("streetlight.url.main");
            String dataUrl = properties.getProperty("streetlight.url.replaceolc");
            String replaceOlc = properties.getProperty("streetlight.url.replaceolc.method");
            String url = mainUrl + dataUrl;
            List<Object> paramsList = new ArrayList<Object>();
            paramsList.add("methodName=" + replaceOlc);
            paramsList.add("controllerStrId=" + controllerStrIdValue);
            paramsList.add("idOnController=" + idOnController);
            paramsList.add("newNetworkId=" + macAddress);
            paramsList.add("ser=json");
            String params = StringUtils.join(paramsList, "&");
            url = url + "?" + params;
            ResponseEntity<String> response = restService.getPostRequest(url, null);
            String responseString = response.getBody();
            JsonObject replaceOlcResponse = (JsonObject) jsonParser.parse(responseString);
            String errorStatus = replaceOlcResponse.get("status").getAsString();
            logger.info("Replace OLC Process End.");
            // As per doc, errorcode is 0 for success. Otherwise, its not success.
            if (errorStatus.equals("ERROR")) {// error response
                logger.error("Error response in replace OLC's call. Idoncontroller: "+idOnController+" macaddress: "+macAddress);
            } else {// success response
                connectionDAO.deleteCommissionFailure(commissionFailureData);
            }

        } catch (Exception e) {
            logger.error("Error in replaceOLC", e);
        }
    }
}
