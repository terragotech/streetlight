package com.slvinterface.json;

import java.util.ArrayList;
import java.util.List;

public class SlvConfig {
    private SlvRequestConfig getDevice;
    private SlvRequestConfig createDevice;
    private SlvRequestConfig setDevice;
    private SlvRequestConfig replaceOLC;
    private String formTemplateGuid;
    private List<Condition> conditions = new ArrayList<>();

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public String getFormTemplateGuid() {
        return formTemplateGuid;
    }

    public void setFormTemplateGuid(String formTemplateGuid) {
        this.formTemplateGuid = formTemplateGuid;
    }

    public SlvRequestConfig getGetDevice() {
        return getDevice;
    }

    public void setGetDevice(SlvRequestConfig getDevice) {
        this.getDevice = getDevice;
    }

    public SlvRequestConfig getCreateDevice() {
        return createDevice;
    }

    public void setCreateDevice(SlvRequestConfig createDevice) {
        this.createDevice = createDevice;
    }

    public SlvRequestConfig getSetDevice() {
        return setDevice;
    }

    public void setSetDevice(SlvRequestConfig setDevice) {
        this.setDevice = setDevice;
    }

    public SlvRequestConfig getReplaceOLC() {
        return replaceOLC;
    }

    public void setReplaceOLC(SlvRequestConfig replaceOLC) {
        this.replaceOLC = replaceOLC;
    }

    @Override
    public String toString() {
        return "SlvConfig{" +
                "getDevice=" + getDevice +
                ", createDevice=" + createDevice +
                ", setDevice=" + setDevice +
                ", replaceOLC=" + replaceOLC +
                '}';
    }
}
