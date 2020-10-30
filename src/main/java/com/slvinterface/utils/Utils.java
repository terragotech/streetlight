package com.slvinterface.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.slvinterface.enumeration.SourceObjectType;
import com.slvinterface.json.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public class Utils {
    public static RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        return restTemplate;
    }

    public static boolean checkConditions(HashMap data, List<Condition> conditions) {
        if (conditions == null || conditions.size() == 0) {
            return true;
        }
        for (Condition condition : conditions) {
            String conditionObject = condition.getConditionObject();
            String conditionComponent = condition.getConditionComponent();
            String defaultValue = condition.getDefaultValue();
            String sourceValue = getValue(data,condition);
            JsonObject conditionData = (JsonObject) data.get(conditionObject);
            String conditionValue = getValue(conditionData, conditionComponent);
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
                case "isNull":
                    return sourceValue == null ? true : false;
                case "isNotNull":
                    return sourceValue != null ? true : false;
            }
        }
        return false;
    }

    public static String getValue(JsonObject data, String component) {
        String value = null;
        if (component != null) {
            value = data.get(component).isJsonNull() ? null : data.get(component).getAsString();
        }
        return value;
    }

    public static String getEdgeValue(HashMap data, String sourceObject, String sourceComponent) {
        try {
            if (sourceObject.equals("form")) {
                List<FormValues> formValues = (List<FormValues>) data.get(sourceObject);

                FormValues formValues1 = new FormValues();
                formValues1.setId(Integer.valueOf(sourceComponent));
                int pos = formValues.indexOf(formValues1);
                if (pos > -1) {
                    String value = formValues.get(pos).getValue();
                    value = (value == null || value.contains("null") || value.trim().isEmpty()) ? null : value.trim();
                    return value;
                }
            } else {
                JsonObject jsonObject = (JsonObject) data.get(sourceObject);
                return getValue(jsonObject, sourceComponent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JsonObject getMappingsValue(HashMap data, List<Mapping> mappings) {
        JsonObject resultData = new JsonObject();
        for (Mapping mapping : mappings) {
            String destinationComponent = mapping.getDestinationComponent();
            String destinationValue = getValue(data,mapping);
            if (destinationValue != null) {
                if(mapping.getDateConfig() != null){
                    destinationValue =  dateFormat(Long.valueOf(destinationValue),mapping.getDateConfig());
                }
                resultData.addProperty(destinationComponent, destinationValue);
            }
        }
        return resultData;
    }

    protected static String dateFormat(Long dateTime, DateConfig dateConfig) {
        Date date = new Date(Long.valueOf(dateTime));
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateConfig.getFormat());
        String timeZone = dateConfig.getTimeZone();
        if(timeZone != null && !timeZone.trim().isEmpty()){
            dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
        }else{
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        }

        String dff = dateFormat.format(date);
        return dff;
    }


    public static String getValue(HashMap data, Mapping mapping) {
        String sourceObject = mapping.getSourceObject();
        SourceObjectType sourceObjectType = mapping.getSourceType();
        if (sourceObject != null) {
            if (sourceObjectType != null) {
                String value = null;
                switch (sourceObjectType) {
                    case FORM:
                        List<FormValues> formValuesList = (List<FormValues>) data.get(sourceObject);
                        value = getFormValue(formValuesList, mapping.getSourceComponent());
                        break;

                    case JSON_ARRAY:
                        JsonArray jsonArray = (JsonArray) data.get(sourceObject);
                        if(jsonArray != null && jsonArray.size() > 0){

                        }
                        break;


                    case JSON_OBJECT:
                        JsonObject jsonObject = (JsonObject) data.get(sourceObject);
                        value = getValueFromJson(jsonObject,mapping.getSourceComponent());
                        break;
                }
                if (value == null) {
                    value = mapping.getDefaultValue();
                }
                if (value != null && mapping.getConcat() != null && mapping.getConcat().getDefaultValue() != null) {
                    value = value + mapping.getConcat().getConcatString() + mapping.getConcat().getDefaultValue();
                }
                return value;
            }

        }else{
            return mapping.getDefaultValue();
        }
        return null;
    }


    private static String getValueFromJson(JsonObject jsonObject,String componentId){
        if(jsonObject != null && jsonObject.get(componentId) != null){
           return jsonObject.get(componentId).getAsString();
        }
        return null;
    }


    private static String getFormValue(List<FormValues> formValuesList,String componentId){
        if(formValuesList != null){
            FormValues formValues1 = new FormValues();
            formValues1.setId(Integer.valueOf(componentId));
            int pos = formValuesList.indexOf(formValues1);
            if (pos > -1) {
                String value = formValuesList.get(pos).getValue();
                value = (value == null || value.contains("null") || value.trim().isEmpty()) ? null : value.trim();
                return value;
            }
        }

        return null;
    }
}


