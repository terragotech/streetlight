package com.slvinterface.json;

import java.util.ArrayList;
import java.util.List;

public class SlvRequestConfig {
    private String url;
    private String method;
    private String paramsType;
    private List<Condition> conditions = new ArrayList<>();
    private List<Mapping> mappings = new ArrayList<>();
    private Response response;
    private SlvRequestConfig checkMac;
    private SlvRequestConfig setDevice;
    private SlvRequestConfig existingMACValidation;

    public SlvRequestConfig getExistingMACValidation() {
        return existingMACValidation;
    }

    public void setExistingMACValidation(SlvRequestConfig existingMACValidation) {
        this.existingMACValidation = existingMACValidation;
    }

    public SlvRequestConfig getCheckMac() {
        return checkMac;
    }

    public void setCheckMac(SlvRequestConfig checkMac) {
        this.checkMac = checkMac;
    }

    public SlvRequestConfig getSetDevice() {
        return setDevice;
    }

    public void setSetDevice(SlvRequestConfig setDevice) {
        this.setDevice = setDevice;
    }

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

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "SlvRequestConfig{" +
                "url='" + url + '\'' +
                ", method='" + method + '\'' +
                ", paramsType='" + paramsType + '\'' +
                ", conditions=" + conditions +
                ", mappings=" + mappings +
                ", response=" + response +
                ", checkMac=" + checkMac +
                ", setDevice=" + setDevice +
                '}';
    }
}
