package com.terragoedge.streetlight.json.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "deviceattributes")
public class DeviceAttributes {
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(columnName = "idoncontroller")
    private String idOnController;
    @DatabaseField(columnName = "macaddress")
    private String macAddress;
    @DatabaseField(columnName = "installstatus")
    private String installStatus;

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getInstallStatus() {
        return installStatus;
    }

    public void setInstallStatus(String installStatus) {
        this.installStatus = installStatus;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdOnController() {
        return idOnController;
    }

    public void setIdOnController(String idOnController) {
        this.idOnController = idOnController;
    }

    @Override
    public String toString() {
        return "DeviceAttributes{" +
                "id=" + id +
                ", idOnController='" + idOnController + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", installStatus='" + installStatus + '\'' +
                '}';
    }
}
