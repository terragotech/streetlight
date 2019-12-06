package com.slvinterface.json;


public class Edge2SLVData {

    private String title;
    private String idOnController;
    private String controllerStrId;
    private String installDate;
    private String macAddress;
    private Priority priority;
    private String existingMACAddress;
    private String fixtureQRScan;
    private String geometry = null;


    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }

    public String getFixtureQRScan() {
        return fixtureQRScan;
    }

    public void setFixtureQRScan(String fixtureQRScan) {
        this.fixtureQRScan = fixtureQRScan;
    }

    public String getExistingMACAddress() {
        return existingMACAddress;
    }

    public void setExistingMACAddress(String existingMACAddress) {
        this.existingMACAddress = existingMACAddress;
    }

    public String getIdOnController() {
        return idOnController;
    }

    public void setIdOnController(String idOnController) {
        this.idOnController = idOnController;
    }

    public String getControllerStrId() {
        return controllerStrId;
    }

    public void setControllerStrId(String controllerStrId) {
        this.controllerStrId = controllerStrId;
    }

    public String getInstallDate() {
        return installDate;
    }

    public void setInstallDate(String installDate) {
        this.installDate = installDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "Edge2SLVData{" +
                "title='" + title + '\'' +
                ", idOnController='" + idOnController + '\'' +
                ", controllerStrId='" + controllerStrId + '\'' +
                ", installDate='" + installDate + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", priority=" + priority +
                ", existingMACAddress='" + existingMACAddress + '\'' +
                '}';
    }
}
