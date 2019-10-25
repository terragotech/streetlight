package com.slvinterface.model;


public class HistoryResponse {

    public String controllerStrId;
    public Integer deviceId;
    public Object errorMessage;
    public String eventTime;
    public Object eventTimeAgeInSeconds;
    public String idOnController;
    public Info info;
    public Object meaningLabel;
    public String name;
    public String status;
    public String updateTime;
    public String value;

    public String getControllerStrId() {
        return controllerStrId;
    }

    public void setControllerStrId(String controllerStrId) {
        this.controllerStrId = controllerStrId;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public Object getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(Object errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public Object getEventTimeAgeInSeconds() {
        return eventTimeAgeInSeconds;
    }

    public void setEventTimeAgeInSeconds(Object eventTimeAgeInSeconds) {
        this.eventTimeAgeInSeconds = eventTimeAgeInSeconds;
    }

    public String getIdOnController() {
        return idOnController;
    }

    public void setIdOnController(String idOnController) {
        this.idOnController = idOnController;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public Object getMeaningLabel() {
        return meaningLabel;
    }

    public void setMeaningLabel(Object meaningLabel) {
        this.meaningLabel = meaningLabel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
