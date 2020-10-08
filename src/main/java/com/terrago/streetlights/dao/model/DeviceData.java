package com.terrago.streetlights.dao.model;

public class DeviceData {
    String parentnoteguid;
    String dev_eui;
    String status;

    public String getParentnoteguid() {
        return parentnoteguid;
    }

    public void setParentnoteguid(String parentnoteguid) {
        this.parentnoteguid = parentnoteguid;
    }

    public String getDev_eui() {
        return dev_eui;
    }

    public void setDev_eui(String dev_eui) {
        this.dev_eui = dev_eui;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
