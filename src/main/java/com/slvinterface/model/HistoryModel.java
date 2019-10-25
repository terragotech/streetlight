package com.slvinterface.model;


import com.opencsv.bean.CsvBindByName;

public class HistoryModel {
    @CsvBindByName(column = "IdOnController")
    private String idOnController;
    @CsvBindByName(column = "DeviceId")
    private int deviceId;
    @CsvBindByName(column = "User")
    private String user;
    @CsvBindByName(column = "Comment")
    private String comment;
    @CsvBindByName(column = "EventTime")
    private String eventTime;
    @CsvBindByName(column = "updateTime")
    private String updateTime;
    @CsvBindByName(column = "SlvMacAddress")
    private String value;
    @CsvBindByName(column = "HistoryJson")
    private String historyJson;

    public String getIdOnController() {
        return idOnController;
    }

    public void setIdOnController(String idOnController) {
        this.idOnController = idOnController;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getHistoryJson() {
        return historyJson;
    }

    public void setHistoryJson(String historyJson) {
        this.historyJson = historyJson;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
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

    @Override
    public String toString() {
        return "HistoryModel{" +
                "idOnController='" + idOnController + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", historyJson='" + historyJson + '\'' +
                '}';
    }
}

