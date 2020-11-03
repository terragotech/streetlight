package com.slvinterface.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.slvinterface.exception.NoValueException;
import com.slvinterface.exception.QRCodeAlreadyUsedException;
import com.slvinterface.json.*;
import com.slvinterface.utils.PropertiesReader;
import com.slvinterface.utils.ResourceDetails;
import com.slvinterface.utils.Utils;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.wololo.geojson.Feature;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.jts2geojson.GeoJSONReader;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.*;

public class ReplaceFormService {

    private Logger logger = Logger.getLogger(GenericProcess.class);
    private Properties properties;
    private JsonParser jsonParser;
    private SLVRestService slvRestService;
    private Gson gson;


    public ReplaceFormService() {
        jsonParser = new JsonParser();
        slvRestService = new SLVRestService();
        gson = new Gson();
        properties = PropertiesReader.getProperties();
    }


    public void processReplaceForm(EdgeNote edgeNote) {
        try {
            logger.info("Replace Form Called...");
            List<SlvConfig> slvConfigList = getSlvRequestConfigs();
            int count = 0;
            for (SlvConfig slvConfig : slvConfigList) {
                HashMap data = new HashMap();
                logger.info("Current Count:" + count);
                count = count + 1;

                String formTemplateGuid = slvConfig.getFormTemplateGuid();
                loadFormData(edgeNote, formTemplateGuid, data);
                loadNoteData(edgeNote, data);

                boolean isConditionPassed = Utils.checkConditions(data, slvConfig.getConditions());
                if (isConditionPassed) {
                    try {
                        String existingMACValidation = processCheckMACUsedConfig(data, slvConfig.getReplaceOLC().getExistingMACValidation());
                        logger.info("Existing MAC Address Response:"+existingMACValidation);
                        if(existingMACValidation != null){
                            String idOnController = getIdOnController(existingMACValidation);
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.addProperty("idOnController", idOnController);
                            data.put("slvData", jsonObject);

                            String responseString = processCheckMACUsedConfig(data, slvConfig.getReplaceOLC().getCheckMac());
                            idOnController = getIdOnController(responseString);
                            logger.info("Current  MAC Address already used Response:"+idOnController);
                            if(idOnController == null){
                                processReplaceOLC(data, slvConfig.getReplaceOLC(), true);
                                processReplaceOLC(data, slvConfig.getReplaceOLC(), false);
                                processReplaceOLC(data, slvConfig.getReplaceOLC().getSetDevice(), false);
                            }else{
                                logger.info("MAC Address already Used.");
                            }
                        }else{
                            logger.info("Existing MAC Address not Present.");
                        }

                    } catch (NoValueException e1) {
                        logger.error("Error in processReplaceForm",e1);
                    }

                }
            }
        } catch (Exception e) {
            logger.error("Error in processReplaceForm",e);
        }
    }


    private String getIdOnController(String responseString) {
        DeviceMacAddress deviceMacAddress = gson.fromJson(responseString, DeviceMacAddress.class);
        List<Value> values = deviceMacAddress.getValue();
        if (values == null || values.size() == 0) {
            return null;
        } else {
            for (Value value : values) {
                return value.getIdOnController();
            }
        }
        return null;
    }


    private void processReplaceOLC(HashMap data, SlvRequestConfig slvRequestConfig, boolean isEmptyReplaceOLC) {
        if (slvRequestConfig != null) {
            String url = slvRequestConfig.getUrl();
            List<Condition> conditions = slvRequestConfig.getConditions();
            List<Mapping> mappings = slvRequestConfig.getMappings();
            String method = slvRequestConfig.getMethod();
            String paramsType = slvRequestConfig.getParamsType();
            boolean isConditionPassed = Utils.checkConditions(data, conditions);
            if (isConditionPassed) {
                JsonObject requestData = Utils.getMappingsValue(data, mappings);
                List<String> queryParams = new ArrayList<>();
                LinkedMultiValueMap<String, String> linkedMultiValueMap = new LinkedMultiValueMap<>();
                for (Map.Entry<String, JsonElement> entry : requestData.entrySet()) {
                    String value = entry.getValue().isJsonNull() ? "" : entry.getValue().getAsString();
                    String key = entry.getKey();
                    queryParams.add(key + "=" + value);
                    Mapping mapping = new Mapping();
                    mapping.setDestinationComponent(key);
                    int pos = mappings.indexOf(mapping);
                    if (pos != -1) {
                        mapping = mappings.get(pos);
                    }
                    if (mapping.isValueParam()) {
                        if (value != null && !value.trim().equals("")) {
                            linkedMultiValueMap.add("valueName", key);
                            linkedMultiValueMap.add("value", value);
                        }
                    } else {
                        if (value != null && !value.trim().equals("")) {
                            if (isEmptyReplaceOLC && key.equals("newNetworkId")) {
                                value = "";
                            }
                            linkedMultiValueMap.add(key, value);

                        }
                    }
                }
                ResponseEntity<String> responseEntity = null;
                switch (method) {
                    case "get":
                        String queryUrl = "";
                        try {
                            queryUrl = url + "?" + StringUtils.join(queryParams, "&");
                            String mainUrl = properties.getProperty("streetlight.slv.base.url");
                            url = mainUrl + url;
                            responseEntity = slvRestService.callGetMethod(url + "?" + StringUtils.join(queryParams, "&"));
                        } catch (Exception e) {
                            logger.error("Error while calling get method: " + queryUrl + ": ", e);
                        }
                        break;
                    case "post":
                        String mainUrl = properties.getProperty("streetlight.slv.base.url");
                        url = mainUrl + url;
                        responseEntity = slvRestService.getPostRequest(url, null, linkedMultiValueMap);
                        break;
                }


            }
        }
    }


    private List<SlvConfig> getSlvRequestConfigs() {
        try {
            FileReader reader = new FileReader(ResourceDetails.REPLACE_CONFIG);
            Type listType = new TypeToken<List<SlvConfig>>() {
            }.getType();

            return gson.fromJson(reader, listType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private String processCheckMACUsedConfig(HashMap data, SlvRequestConfig slvRequestConfig) throws NoValueException {
        if (slvRequestConfig != null) {
            List<Condition> conditions = slvRequestConfig.getConditions();
            String url = slvRequestConfig.getUrl();
            List<Mapping> mappings = slvRequestConfig.getMappings();
            boolean isConditionPassed = Utils.checkConditions(data, conditions);
            if (isConditionPassed) {
                LinkedMultiValueMap<String, String> linkedMultiValueMap = new LinkedMultiValueMap<>();
                JsonObject requestData = Utils.getMappingsValue(data, mappings);
                for (Map.Entry<String, JsonElement> entry : requestData.entrySet()) {
                    String value = entry.getValue().isJsonNull() ? "" : entry.getValue().getAsString();
                    String key = entry.getKey();
                    linkedMultiValueMap.add(key, value);
                }

                String mainUrl = properties.getProperty("streetlight.slv.base.url");
                url = mainUrl + url;
                ResponseEntity<String> responseEntity = slvRestService.getPostRequest(url, null, linkedMultiValueMap);
                if (responseEntity.getStatusCodeValue() == 200) {
                    return responseEntity.getBody();
                }
            } else {
                throw new NoValueException("Value not present");
            }

        }
        return null;
    }


    private void loadGeometryObject(EdgeNote edgeNote, HashMap data) {
        Feature feature = (Feature) GeoJSONFactory.create(edgeNote.getGeometry());

        // parse Geometry from Feature
        GeoJSONReader reader = new GeoJSONReader();
        Geometry geom = reader.read(feature.getGeometry());

        JsonObject geometryJson = new JsonObject();
        geometryJson.addProperty("lat", String.valueOf(geom.getCoordinate().y));
        geometryJson.addProperty("lng", String.valueOf(geom.getCoordinate().x));
        data.put("geometry", geometryJson);
    }

    private void loadNoteData(EdgeNote edgeNote, HashMap data) {
        data.put("note", gson.toJsonTree(edgeNote));
    }


    private void loadFormData(EdgeNote edgeNote, String formTemplateGuid, HashMap data) {
        List<FormData> formDataList = edgeNote.getFormData();
        for (FormData formData : formDataList) {
            if (formData.getFormTemplateGuid().equals(formTemplateGuid)) {
                data.put("form", formData.getFormDef());
            }
        }
    }

}
