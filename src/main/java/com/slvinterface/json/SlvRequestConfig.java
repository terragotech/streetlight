package com.slvinterface.json;

import java.util.ArrayList;
import java.util.List;

public class SlvRequestConfig {
    private String url;
    private String method;
    private String paramsType;
    private List<Condition> conditions = new ArrayList<>();
    private List<Mapping> mappings = new ArrayList<>();
    private String objectKey;


    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getParamsType() {
        return paramsType;
    }

    public void setParamsType(String paramsType) {
        this.paramsType = paramsType;
    }

    public List<Mapping> getMappings() {
        return mappings;
    }

    public void setMappings(List<Mapping> mappings) {
        this.mappings = mappings;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    @Override
    public String toString() {
        return "SlvRequestConfig{" +
                "url='" + url + '\'' +
                ", method='" + method + '\'' +
                ", paramsType='" + paramsType + '\'' +
                ", conditions=" + conditions +
                ", mappings=" + mappings +
                ", objectKey='" + objectKey + '\'' +
                '}';
    }
}
