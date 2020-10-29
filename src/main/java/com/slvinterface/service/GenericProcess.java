package com.slvinterface.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.slvinterface.json.*;
import com.slvinterface.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GenericProcess {
    private SLVRestService slvRestService;
    private SlvConfig slvConfig = new SlvConfig();
    private Logger logger = Logger.getLogger(GenericProcess.class);
    private JsonParser jsonParser;
    public GenericProcess() {
        jsonParser = new JsonParser();
        slvRestService = new SLVRestService();
    }

    public void process(){
        SlvRequestConfig getDeviceConfig = slvConfig.getGetDevice();
    }

    private void processRequestConfig(JsonObject data, SlvRequestConfig slvRequestConfig, List<FormValues> formValues){
        if (slvRequestConfig != null) {
            String url = slvRequestConfig.getUrl();
            List<Condition> conditions = slvRequestConfig.getConditions();
            List<Mapping> mappings = slvRequestConfig.getMappings();
            String method = slvRequestConfig.getMethod();
            String objectKey = slvRequestConfig.getObjectKey();
            String paramsType = slvRequestConfig.getParamsType();
            boolean isConditionPassed = Utils.checkConditions(data,conditions);
            if(isConditionPassed){
                JsonObject requestData = Utils.getMappingsValue(data,mappings,formValues);
                List<String> queryParams = new ArrayList<>();
                LinkedMultiValueMap<String,String> linkedMultiValueMap = new LinkedMultiValueMap<>();
                for(Map.Entry<String, JsonElement> entry: requestData.entrySet()){
                    String value = entry.getValue().isJsonNull() ? "" : entry.getValue().getAsString();
                    String key = entry.getKey();
                    queryParams.add(key+"="+value);
                    linkedMultiValueMap.add(key, value);
                }
                ResponseEntity<String> responseEntity = null;
                switch (method){
                    case "get":
                        String queryUrl = "";
                        try {
                            queryUrl = url + "?" + StringUtils.join(queryParams, "&");
                            responseEntity = slvRestService.callGetMethod(url + "?" + StringUtils.join(queryParams, "&"));
                        }catch (Exception e){
                            logger.error("Error while calling get method: "+queryUrl+": ",e);
                        }
                        break;
                    case "post":
                        responseEntity = slvRestService.getPostRequest(url,null, linkedMultiValueMap);
                        break;
                }

                if (responseEntity != null && objectKey != null){
                    if (responseEntity.getStatusCode() == HttpStatus.OK){
                        data.add(objectKey,jsonParser.parse(responseEntity.getBody()));
                    }
                }
            }
        }
    }

}
