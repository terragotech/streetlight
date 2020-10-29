package com.slvinterface.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.slvinterface.json.Condition;
import com.slvinterface.json.FormData;
import com.slvinterface.json.FormValues;
import com.slvinterface.json.Mapping;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.List;

public class Utils {
    public static RestTemplate getRestTemplate(){
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        return restTemplate;
    }

    public static boolean checkConditions(JsonObject data, List<Condition> conditions) {
        if(conditions == null || conditions.size() == 0){
            return true;
        }
        for (Condition condition : conditions){
            String sourceComponent = condition.getSourceComponent();
            String sourceObject = condition.getSourceObject();
            String conditionObject = condition.getConditionObject();
            String conditionComponent = condition.getConditionComponent();
            String defaultValue = condition.getDefaultValue();
            String sourceValue = getValue(data, sourceObject, sourceComponent);
            String conditionValue = getValue(data, conditionObject, conditionComponent);
            conditionValue = defaultValue != null ? defaultValue : conditionValue;
            String conditionType = condition.getConditionType();
            switch (conditionType) {
                case "equal":
                    sourceValue = sourceValue == null ? "" : sourceValue;
                    conditionValue = conditionValue == null ? "" : conditionValue;
                    return sourceValue.equals(conditionValue);
                case "notEqual":
                    sourceValue = sourceValue == null ? "" : sourceValue;
                    conditionValue = conditionValue == null ? "" : conditionValue;
                    return !sourceValue.equals(conditionValue);
                case "null":
                    return sourceValue == null ? true : false;
                case "notNull":
                    return sourceValue != null ? true : false;
            }
        }
        return false;
    }

    public static String getValue(JsonObject data, String object,String component){
        String value = null;
        if (component != null && object != null){
            JsonObject objectValue = data.get(object).isJsonNull() ? null : data.get(object).getAsJsonObject();
            if (objectValue != null) {
                value = objectValue.get(component).isJsonNull() ? null : objectValue.get(component).getAsString();
            }
        }else if(component != null){
            value = data.get(component).isJsonNull() ? null : data.get(component).getAsString();
        }
        return value;
    }
    public static String getEdgeValue (JsonObject data, String sourceObject, String sourceComponent, List<FormValues> formValues){
        try {
            if (sourceObject.equals("form")) {
                FormValues formValues1 = new FormValues();
                formValues1.setId(Integer.valueOf(sourceComponent));
                int pos = formValues.indexOf(formValues1);
                if(pos > -1){
                    String value = formValues.get(pos).getValue();
                    value = (value == null || value.contains("null") || value.trim().isEmpty()) ? null : value.trim();
                    return value;
                }
            } else if (sourceObject.equals("note")) {
                return data.get(sourceComponent).isJsonNull() ? null : data.get(sourceComponent).getAsString();
            } else {
                return getValue(data, sourceObject, sourceComponent);
            }
        }catch (Exception e){

        }
        return null;
    }
    public static JsonObject getMappingsValue(JsonObject data, List<Mapping> mappings){
        Gson gson = new Gson();
        JsonObject resultData = new JsonObject();
        for(Mapping mapping : mappings){
            String sourceObject = mapping.getSourceObject();
            String sourceComponent = mapping.getSourceComponent();
            String destinationComponent = mapping.getDestinationComponent();
            String defaultValue = mapping.getDefaultValue();
            if (defaultValue != null){
                resultData.addProperty(destinationComponent,defaultValue);
            }else {
                List<FormValues> formValues  = gson.fromJson(data.get("formdatas").getAsString(),new TypeToken<List<FormValues>>() {}.getType());
                String destinationValue = getEdgeValue(data,sourceObject, sourceComponent,formValues);
                if (destinationValue != null) {
                    resultData.addProperty(destinationComponent, destinationValue);
                }
            }
        }
        return resultData;
    }
}


