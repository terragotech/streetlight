package com.terragoedge.streetlight.json.model;

public class SLVDates {

    private String cslpLumDate;
    private String cslpNodeDate;
    private String nodeInstallDate;
    private String lumInstallDate;



    public SLVDates(String cslpLumDate, String cslpNodeDate) {
        this.cslpLumDate = cslpLumDate;
        this.cslpNodeDate = cslpNodeDate;
    }

    public SLVDates() {
    }

    public String getNodeInstallDate() {
        return nodeInstallDate;
    }

    public void setNodeInstallDate(String nodeInstallDate) {
        this.nodeInstallDate = nodeInstallDate;
    }

    public String getLumInstallDate() {
        return lumInstallDate;
    }

    public void setLumInstallDate(String lumInstallDate) {
        this.lumInstallDate = lumInstallDate;
    }

    public String getCslpLumDate() {
        return cslpLumDate;
    }

    public void setCslpLumDate(String cslpLumDate) {
        this.cslpLumDate = cslpLumDate;
    }

    public String getCslpNodeDate() {
        return cslpNodeDate;
    }

    public void setCslpNodeDate(String cslpNodeDate) {
        this.cslpNodeDate = cslpNodeDate;
    }

    

    @Override
    public String toString() {
        return "SLVDates{" +
                "cslpLumDate='" + cslpLumDate + '\'' +
                ", cslpNodeDate='" + cslpNodeDate + '\'' +
                ", nodeInstallDate='" + nodeInstallDate + '\'' +
                ", lumInstallDate='" + lumInstallDate + '\'' +
                '}';
    }
}
