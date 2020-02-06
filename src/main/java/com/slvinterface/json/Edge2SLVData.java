package com.slvinterface.json;

import com.slvinterface.enumeration.SLVProcess;

public class Edge2SLVData {

    private String title;
    private String idOnController;
    private String controllerStrId;
    private String installDate;
    private String macAddress;
    private Priority priority;
    private String existingMACAddress;

    private String currentGeoZone;
    private String lat;
    private String lng;

    private String clientNumber;
    private String centralAssetId;
    private String featureId;
    private String siteName;
    private String featureLocation;

    private String fixtureQRScan;

    private String calendar;

    public String getCalendar() {
        return calendar;
    }

    public void setCalendar(String calendar) {
        this.calendar = calendar;
    }

    public String getFixtureQRScan() {
        return fixtureQRScan;
    }

    public void setFixtureQRScan(String fixtureQRScan) {
        this.fixtureQRScan = fixtureQRScan;
    }

    public String getCurrentGeoZone() {
        return currentGeoZone;
    }

    public void setCurrentGeoZone(String currentGeoZone) {
        this.currentGeoZone = currentGeoZone;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
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


    public String getClientNumber() {
        return clientNumber;
    }

    public void setClientNumber(String clientNumber) {
        this.clientNumber = clientNumber;
    }

    public String getCentralAssetId() {
        return centralAssetId;
    }

    public void setCentralAssetId(String centralAssetId) {
        this.centralAssetId = centralAssetId;
    }

    public String getFeatureId() {
        return featureId;
    }

    public void setFeatureId(String featureId) {
        this.featureId = featureId;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getFeatureLocation() {
        return featureLocation;
    }

    public void setFeatureLocation(String featureLocation) {
        this.featureLocation = featureLocation;
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
                ", currentGeoZone='" + currentGeoZone + '\'' +
                ", lat='" + lat + '\'' +
                ", lng='" + lng + '\'' +
                ", clientNumber='" + clientNumber + '\'' +
                ", centralAssetId='" + centralAssetId + '\'' +
                ", featureId='" + featureId + '\'' +
                ", siteName='" + siteName + '\'' +
                ", featureLocation='" + featureLocation + '\'' +
                ", fixtureQRScan='" + fixtureQRScan + '\'' +
                ", calendar='" + calendar + '\'' +
                '}';
    }
}
