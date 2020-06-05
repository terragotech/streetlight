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
        List<CommissionFailureData> commissionFailureDatas = connectionDAO.getCommissionFailureDatas();

        for(CommissionFailureData commissionFailureData : commissionFailureDatas){
            try {
                String idoncontroller = commissionFailureData.getIdoncontroller();
                String macaddress = commissionFailureData.getMacaddress();
                if (macaddress == null || macaddress.equals("")) {
                    connectionDAO.deleteCommissionFailure(commissionFailureData);
                } else {
                    recommission(macaddress, idoncontroller, commissionFailureData);
                }
            }catch (Exception e){
                logger.error("Error in processing recommission: ",e);
            }
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
                logger.error("Error while check mac address already used or not. Err: " + response.getBody());
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
