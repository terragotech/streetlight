package com.terragoedge.streetlight.json.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "install_removed_exp_report")
public class InstallationRemovedExceptionReport {
    public static final String TITLE = "idoncontroller";
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(columnName = "idoncontroller")
    private String idOnController;
    @DatabaseField(columnName = "macaddress")
    private String macAddress;
    @DatabaseField(columnName = "communication_status")
    private String communicationStatus;
    @DatabaseField(columnName = "created_date_time")
    private long createdDateTime;
    @DatabaseField(columnName = "event_time")
    private long eventTime;
    @DatabaseField(columnName = "created_by")
    private String createdBy;

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

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getCommunicationStatus() {
        return communicationStatus;
    }

    public void setCommunicationStatus(String communicationStatus) {
        this.communicationStatus = communicationStatus;
    }

    public long getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(long createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String toString() {
        return "InstallationRemovedExceptionReport{" +
                "id=" + id +
                ", idOnController='" + idOnController + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", communicationStatus='" + communicationStatus + '\'' +
                ", createdDateTime=" + createdDateTime +
                ", eventTime=" + eventTime +
                ", createdBy='" + createdBy + '\'' +
                '}';
    }
}
