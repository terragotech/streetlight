package com.terragoedge.streetlight.json.model;

public class PromotedFormData {

    private String idOnController;
    private String installDate;
    private  String cslpNodeInstallDate;
    private String cslpLumInstallDate;
    private String lumInstallDate;


    public String getIdOnController() {
        return idOnController;
    }

    public void setIdOnController(String idOnController) {
        this.idOnController = idOnController;
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
                "idOnController='" + idOnController + '\'' +
                ", installDate='" + installDate + '\'' +
                ", cslpNodeInstallDate='" + cslpNodeInstallDate + '\'' +
                ", cslpLumInstallDate='" + cslpLumInstallDate + '\'' +
                ", lumInstallDate='" + lumInstallDate + '\'' +
                '}';
    }
}
