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
                '}';
    }
}
