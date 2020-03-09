package com.terrago.streetlights.dao.model;

public class UbiTransactionLog {
    String notegui;
    String title;
    String action;
    String deviceStatus;
    String devui;
    long synctime;
    long eventtime;

    public String getNotegui() {
        return notegui;
    }

    public void setNotegui(String notegui) {
        this.notegui = notegui;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public String getDevui() {
        return devui;
    }

    public void setDevui(String devui) {
        this.devui = devui;
    }

    public long getSynctime() {
        return synctime;
    }

    public void setSynctime(long synctime) {
        this.synctime = synctime;
    }

    public long getEventtime() {
        return eventtime;
    }

    public void setEventtime(long eventtime) {
        this.eventtime = eventtime;
    }
}
