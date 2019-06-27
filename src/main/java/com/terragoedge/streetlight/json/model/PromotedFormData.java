package com.terragoedge.streetlight.json.model;

public class PromotedFormData {

    private String idonController;
    private String installDate;
    private  String cslpNodeInstallDate;
    private String cslpLumInstallDate;
    private String lumInstallDate;


    public String getIdonController() {
        return idonController;
    }

    public void setIdonController(String idonController) {
        this.idonController = idonController;
    }

    public String getInstallDate() {
        return installDate;
    }

    public void setInstallDate(String installDate) {
        this.installDate = installDate;
    }

    public String getCslpNodeInstallDate() {
        return cslpNodeInstallDate;
    }

    public void setCslpNodeInstallDate(String cslpNodeInstallDate) {
        this.cslpNodeInstallDate = cslpNodeInstallDate;
    }

    public String getCslpLumInstallDate() {
        return cslpLumInstallDate;
    }

    public void setCslpLumInstallDate(String cslpLumInstallDate) {
        this.cslpLumInstallDate = cslpLumInstallDate;
    }

    public String getLumInstallDate() {
        return lumInstallDate;
    }

    public void setLumInstallDate(String lumInstallDate) {
        this.lumInstallDate = lumInstallDate;
    }

    @Override
    public String toString() {
        return "PromotedFormData{" +
                "idonController='" + idonController + '\'' +
                ", installDate='" + installDate + '\'' +
                ", cslpNodeInstallDate='" + cslpNodeInstallDate + '\'' +
                ", cslpLumInstallDate='" + cslpLumInstallDate + '\'' +
                ", lumInstallDate='" + lumInstallDate + '\'' +
                '}';
    }
}
