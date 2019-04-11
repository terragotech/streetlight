package com.terragoedge.streetlight.json.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


@DatabaseTable(tableName = "duplicate_macaddress_eventlog")
public class DuplicateMACAddressEventLog {
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(columnName = "idoncontroller")
    private String idOnController;

    @DatabaseField(columnName = "macaddress")
    private String macaddress;

    @DatabaseField(columnName = "devices")
    private String deviceList;

    @DatabaseField(columnName = "eventtime")
    private long eventTime;

    @DatabaseField(columnName = "noteguid")
    private String noteGuid;

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

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    public String getNoteGuid() {
        return noteGuid;
    }

    public void setNoteGuid(String noteGuid) {
        this.noteGuid = noteGuid;
    }

    @Override
    public String toString() {
        return "DuplicateMACAddressEventLog{" +
                "id=" + id +
                ", idOnController='" + idOnController + '\'' +
                ", macaddress='" + macaddress + '\'' +
                ", deviceList='" + deviceList + '\'' +
                ", eventTime=" + eventTime +
                ", noteGuid='" + noteGuid + '\'' +
                '}';
    }
}
