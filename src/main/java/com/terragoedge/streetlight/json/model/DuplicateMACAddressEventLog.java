package com.terragoedge.streetlight.json.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


@DatabaseTable(tableName = "duplicate_macaddress_eventlog")
public class DuplicateMACAddressEventLog {
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(columnName = "title")
    private String title;

    @DatabaseField(columnName = "macaddress")
    private String macaddress;

    @DatabaseField(columnName = "devices")
    private String deviceList;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMacaddress() {
        return macaddress;
    }

    public void setMacaddress(String macaddress) {
        this.macaddress = macaddress;
    }

    public String getDeviceList() {
        return deviceList;
    }

    public void setDeviceList(String deviceList) {
        this.deviceList = deviceList;
    }

    @Override
    public String toString() {
        return "DuplicateMACAddressEventLog{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", macaddress='" + macaddress + '\'' +
                ", deviceList='" + deviceList + '\'' +
                '}';
    }
}
