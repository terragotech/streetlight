package com.slvinterface.utils;

import com.slvinterface.entity.SLVSyncTable;

public class SLVInterfaceUtilsModel {
    private String idOnController;
    private String currentGeoZoneName;
    private String lat;
    private String lng;
    private String deviceName;
    private SLVSyncTable slvSyncTable;

    private int geoZoneId;

    public SLVInterfaceUtilsModel(String idOnController, String currentGeoZoneName, String lat, String lng, String deviceName, SLVSyncTable slvSyncTable) {
        this.idOnController = idOnController;
        this.currentGeoZoneName = currentGeoZoneName;
        this.lat = lat;
        this.lng = lng;
        this.deviceName = deviceName;
        this.slvSyncTable = slvSyncTable;
    }

    public String getIdOnController() {
        return idOnController;
    }

    public int getGeoZoneId() {
        return geoZoneId;
    }

    public void setGeoZoneId(int geoZoneId) {
        this.geoZoneId = geoZoneId;
    }

    public void setIdOnController(String idOnController) {
        this.idOnController = idOnController;
    }

    public String getCurrentGeoZoneName() {
        return currentGeoZoneName;
    }

    public void setCurrentGeoZoneName(String currentGeoZoneName) {
        this.currentGeoZoneName = currentGeoZoneName;
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

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public SLVSyncTable getSlvSyncTable() {
        return slvSyncTable;
    }

    public void setSlvSyncTable(SLVSyncTable slvSyncTable) {
        this.slvSyncTable = slvSyncTable;
    }
}
