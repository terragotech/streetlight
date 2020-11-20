package com.slvinterface.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.slvinterface.json.*;
import com.slvinterface.json.Dictionary;
import com.slvinterface.utils.PropertiesReader;
import com.slvinterface.utils.ResourceDetails;
import com.slvinterface.utils.Utils;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.wololo.geojson.Feature;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.jts2geojson.GeoJSONReader;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

public class GenericProcess {
    private SLVRestService slvRestService;
    private Logger logger = Logger.getLogger(GenericProcess.class);
    private JsonParser jsonParser;
    private Gson gson;
    private Properties properties;
    private EdgeRestService edgeRestService;

    public GenericProcess() {
        jsonParser = new JsonParser();
        slvRestService = new SLVRestService();
        gson = new Gson();
        properties = PropertiesReader.getProperties();
        edgeRestService = new EdgeRestService();
    }


    private void loadGeometryObject(EdgeNote edgeNote,HashMap  data){
        Feature feature = (Feature) GeoJSONFactory.create(edgeNote.getGeometry());

        // parse Geometry from Feature
        GeoJSONReader reader = new GeoJSONReader();
        Geometry geom = reader.read(feature.getGeometry());

        JsonObject geometryJson = new JsonObject();
        geometryJson.addProperty("lat",String.valueOf(geom.getCoordinate().y));
        geometryJson.addProperty("lng",String.valueOf(geom.getCoordinate().x));
        data.put("geometry",geometryJson);
    }

    private void loadNoteData(EdgeNote edgeNote,HashMap data){
        data.put("note",gson.toJsonTree(edgeNote));
    }


    private void loadFormData(EdgeNote edgeNote,String formTemplateGuid,HashMap  data){
        List<FormData> formDataList = edgeNote.getFormData();
        for(FormData formData : formDataList){
            if(formData.getFormTemplateGuid().equals(formTemplateGuid)){
                data.put("form",formData.getFormDef());
            }
        }
    }


    private List<SlvConfig> getSlvRequestConfigs(){
        try{
            FileReader reader = new FileReader(ResourceDetails.GENERIC_CONFIG);
            Type listType = new TypeToken<List<SlvConfig>>() {
            }.getType();

           return gson.fromJson(reader,listType);
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void process(EdgeNote edgeNote) {


        List<SlvConfig> slvConfigList = getSlvRequestConfigs();
        int count = 0;
        for (SlvConfig slvConfig : slvConfigList) {
            HashMap data = new HashMap();
            loadGeometryObject(edgeNote, data);
            logger.info("Current Count:"+count);
            count = count + 1;

            String formTemplateGuid = slvConfig.getFormTemplateGuid();
            loadFormData(edgeNote, formTemplateGuid, data);
            loadNoteData(edgeNote,data);

            boolean isConditionPassed = Utils.checkConditions(data,slvConfig.getConditions());
            if(isConditionPassed){
                try{

                    if(edgeNote.getEdgeNotebook().getNotebookName().equals("CPS Medical Center")){
                        JsonObject geoZoneJson = new JsonObject();
                        geoZoneJson.addProperty("id","13203");
                        data.put("geoZone",geoZoneJson);
                    }else if(edgeNote.getEdgeNotebook().getNotebookName().equals("CPS Downtown")){
                        JsonObject geoZoneJson = new JsonObject();
                        geoZoneJson.addProperty("id","13196");
                        data.put("geoZone",geoZoneJson);
                    }
                    SlvRequestConfig getDeviceConfig = slvConfig.getGetDevice();
                    SlvRequestConfig createDeviceConfig = slvConfig.getCreateDevice();
                    SlvRequestConfig setDeviceConfig = slvConfig.getSetDevice();
                    SlvRequestConfig replaceOLCConfig = slvConfig.getReplaceOLC();
                    processDeviceConfig(data, getDeviceConfig);
                    processDeviceConfig(data, createDeviceConfig);
                    processRequestConfig(data, setDeviceConfig, false);
                    processReplaceOLC(data, replaceOLCConfig);
                    processDeCommission(data,slvConfig.getDeCommission());
                }catch (Exception e){
                    logger.error("Error",e);
                }
            }


        }

    }


    private void processReplaceOLC(HashMap data, SlvRequestConfig slvRequestConfig){
        if(slvRequestConfig != null){
            processCheckMACUsedConfig(data,slvRequestConfig.getCheckMac());
            processRequestConfig(data, slvRequestConfig, true);
            processRequestConfig(data, slvRequestConfig, false);
            processRequestConfig(data, slvRequestConfig.getSetDevice(), false);
        }

    }


    private void processDeCommission(HashMap data, SlvRequestConfig slvRequestConfig){
        if(slvRequestConfig != null){
            processRequestConfig(data, slvRequestConfig, true);
            processRequestConfig(data, slvRequestConfig.getSetDevice(), false);
        }

    }


    private void processCheckMACUsedConfig(HashMap data, SlvRequestConfig slvRequestConfig){
        if(slvRequestConfig != null){
            List<Condition> conditions = slvRequestConfig.getConditions();
            String url = slvRequestConfig.getUrl();
            List<Mapping> mappings = slvRequestConfig.getMappings();
            boolean isConditionPassed = Utils.checkConditions(data,conditions);
            if(isConditionPassed){
                LinkedMultiValueMap<String,String> linkedMultiValueMap = new LinkedMultiValueMap<>();
                JsonObject requestData = Utils.getMappingsValue(data,mappings);
                for(Map.Entry<String, JsonElement> entry: requestData.entrySet()){
                    String value = entry.getValue().isJsonNull() ? "" : entry.getValue().getAsString();
                    String key = entry.getKey();
                    linkedMultiValueMap.add(key, value);
                }

                String mainUrl = properties.getProperty("streetlight.slv.base.url");
                url = mainUrl + url;
                ResponseEntity<String> responseEntity = slvRestService.getPostRequest(url,null, linkedMultiValueMap);
                Response response =  slvRequestConfig.getResponse();
                if(response != null && responseEntity.getStatusCodeValue() == response.getStatusCode()){

                    JsonObject jsonObject =  (JsonObject)jsonParser.parse(responseEntity.getBody());
                    List<Filter> filterList =   response.getFilters();
                    for (Filter filter : filterList){
                        JsonArray jsonArray =  jsonObject.get(filter.getSourceObject()).getAsJsonArray();
                        if(jsonArray.size() > 0){
                            JsonObject json = new JsonObject();
                            json.addProperty(filter.getDestinationComponent(),filter.getDefaultValue());
                            data.put(filter.getObjectKey(),json);
                        }
                    }
                }
            }
        }
    }



    private void processDeviceConfig(HashMap data, SlvRequestConfig slvRequestConfig){
        if(slvRequestConfig != null){
            String url = slvRequestConfig.getUrl();
            List<Condition> conditions = slvRequestConfig.getConditions();
            List<Mapping> mappings = slvRequestConfig.getMappings();
            boolean isConditionPassed = Utils.checkConditions(data,conditions);
            if(isConditionPassed){
                JsonObject requestData = Utils.getMappingsValue(data,mappings);
                LinkedMultiValueMap<String,String> linkedMultiValueMap = new LinkedMultiValueMap<>();
                for(Map.Entry<String, JsonElement> entry: requestData.entrySet()){
                    String value = entry.getValue().isJsonNull() ? "" : entry.getValue().getAsString();
                    String key = entry.getKey();
                    linkedMultiValueMap.add(key, value);
                }

                String mainUrl = properties.getProperty("streetlight.slv.base.url");
                url = mainUrl + url;
                ResponseEntity<String> responseEntity = slvRestService.getPostRequest(url,null, linkedMultiValueMap);
                Response response =  slvRequestConfig.getResponse();
                if(response != null && responseEntity.getStatusCodeValue() == response.getStatusCode()){

                  JsonObject jsonObject =  (JsonObject)jsonParser.parse(responseEntity.getBody());
                    List<Filter> filterList =   response.getFilters();
                    for (Filter filter : filterList){
                      JsonArray jsonArray =  jsonObject.get(filter.getSourceObject()).getAsJsonArray();
                      if(jsonArray.size() > 0){
                         JsonObject deviceResponse = (JsonObject) jsonArray.get(0);
                          data.put(filter.getObjectKey(),deviceResponse);
                      }

                    }
                }
            }
        }
    }

    private void processRequestConfig(HashMap data, SlvRequestConfig slvRequestConfig, boolean isEmptyReplaceOLC){
        if (slvRequestConfig != null) {
            String url = slvRequestConfig.getUrl();
            List<Condition> conditions = slvRequestConfig.getConditions();
            List<Mapping> mappings = slvRequestConfig.getMappings();
            String method = slvRequestConfig.getMethod();
            String paramsType = slvRequestConfig.getParamsType();
            boolean isConditionPassed = Utils.checkConditions(data,conditions);
            if(isConditionPassed){
                JsonObject requestData = Utils.getMappingsValue(data,mappings);
                List<String> queryParams = new ArrayList<>();
                LinkedMultiValueMap<String,String> linkedMultiValueMap = new LinkedMultiValueMap<>();
                for(Map.Entry<String, JsonElement> entry: requestData.entrySet()){
                    String value = entry.getValue().isJsonNull() ? "" : entry.getValue().getAsString();
                    String key = entry.getKey();
                    queryParams.add(key+"="+value);
                    Mapping mapping = new Mapping();
                    mapping.setDestinationComponent(key);
                   int pos =  mappings.indexOf(mapping);
                   if(pos != -1){
                       mapping =  mappings.get(pos);
                   }
                    if(mapping.isValueParam()){
                        if (value != null && !value.trim().equals("")) {
                            linkedMultiValueMap.add("valueName", key);
                            linkedMultiValueMap.add("value", value);
                        }
                    }else {
                        if (value != null && !value.trim().equals("")) {
                            if(isEmptyReplaceOLC && key.equals("newNetworkId")){
                                value = "";
                            }
                            linkedMultiValueMap.add(key, value);

                        }
                    }
                }
                ResponseEntity<String> responseEntity = null;
                switch (method){
                    case "get":
                        String queryUrl = "";
                        try {
                            queryUrl = url + "?" + StringUtils.join(queryParams, "&");
                            String mainUrl = properties.getProperty("streetlight.slv.base.url");
                            url = mainUrl + url;
                            responseEntity = slvRestService.callGetMethod(url + "?" + StringUtils.join(queryParams, "&"));
                        }catch (Exception e){
                            logger.error("Error while calling get method: "+queryUrl+": ",e);
                        }
                        break;
                    case "post":
                        String mainUrl = properties.getProperty("streetlight.slv.base.url");
                        url = mainUrl + url;
                        responseEntity = slvRestService.getPostRequest(url,null, linkedMultiValueMap);
                        break;
                }


            }
        }
    }
    private void addDictionary(List<Dictionary> dictionaries, String layerGuid){
        Dictionary dictionary = new Dictionary();
        dictionary.setKey("groupguid");
        dictionary.setValue(layerGuid);
        if (dictionaries == null){
            dictionaries = new ArrayList<>();
        }
        dictionaries.add(dictionary);
    }
    private boolean processLayer(EdgeNote edgeNote,String layerGuid){
        boolean isLayerChanged = false;
        List<Dictionary> dictionaries = edgeNote.getDictionary();
        if (dictionaries == null || dictionaries.size() == 0){// no dictionaries present in edgenote, so we can directly add groupguid dictionary
            addDictionary(dictionaries, layerGuid);
            isLayerChanged = true;
        } else{
            boolean isLayerAlreadyPresent = false;
            for(Dictionary dictionary : dictionaries){
                if(dictionary.getKey().equals("groupguid")){// already groupguid dictionary present
                    if (!dictionary.getValue().equals(layerGuid)){// if given layerguid already present in edgenote, then skip it
                        dictionary.setValue(layerGuid);
                        isLayerChanged = true;
                    }
                    isLayerAlreadyPresent = true;
                }
            }
            if (!isLayerAlreadyPresent){// dictionaries present, but groupguid dictionary not there, just add groupguid dictionary
                addDictionary(dictionaries, layerGuid);
                isLayerChanged = true;
            }
        }
        return isLayerChanged;
    }

public void updateEdgenoteService(EdgeNote edgeNote,List<FieldUpdate> fieldUpdates) {
        boolean isEdgeNoteChanged = false;
        for (FieldUpdate fieldUpdate : fieldUpdates) {
            String type = fieldUpdate.getType();
            String value = fieldUpdate.getValue();
            switch (type) {
                case "layer":
                    boolean isLayerChanged = processLayer(edgeNote, value);
                    if(!isEdgeNoteChanged){// if isEdgeNoteChanged already true,need to create new revision. so no need to update isEdgeNoteChanged.
                        isEdgeNoteChanged = isLayerChanged;
                    }
                    break;
            }
        }
        if(isEdgeNoteChanged){// if any edgenote value changed based of fieldUpdates, need to call revision
            EdgeNotebook edgeNotebook = edgeNote.getEdgeNotebook();
            if(edgeNotebook == null){
                logger.error("no notebook present for this note: "+edgeNote.getNoteGuid());
                return;
            }
            List<FormData> formDatas = edgeNote.getFormData();
            if(formDatas != null){
                for(FormData formData : formDatas){
                    formData.setFormGuid(UUID.randomUUID().toString());
                }
            }
            String oldGuid = edgeNote.getNoteGuid();
            edgeNote.setNoteGuid(UUID.randomUUID().toString());
            edgeNote.setCreatedDateTime(System.currentTimeMillis());
            String urlNew = properties.getProperty("streetlight.edge.url.main") + "/rest/notebooks/" + edgeNotebook.getNotebookGuid() + "/notes/" + oldGuid;
            edgeRestService.serverCall(urlNew, HttpMethod.PUT, gson.toJson(edgeNote));
        }
}


}
