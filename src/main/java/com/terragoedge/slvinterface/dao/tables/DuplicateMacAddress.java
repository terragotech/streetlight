package com.terragoedge.slvinterface.dao.tables;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "slv_duplicate_macaddress")
public class DuplicateMacAddress {
    @DatabaseField(columnName = "id",generatedId = true)
    private int id;
    @DatabaseField(columnName = "title")
    private String title;
    @DatabaseField(columnName = "noteguid")
    private String noteguid;
    @DatabaseField(columnName = "pole_number")
    private String poleNumber;
    @DatabaseField(columnName = "existing_pole_number")
    private String existingPoleNumber;
    @DatabaseField(columnName = "macaddress")
    private String macAddress;
    @DatabaseField(columnName = "processed_date_time")
    private long processedDateTime;

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

    public String getNoteguid() {
        return noteguid;
    }

    public void setNoteguid(String noteguid) {
        this.noteguid = noteguid;
    }

    public String getPoleNumber() {
        return poleNumber;
    }

    public void setPoleNumber(String poleNumber) {
        this.poleNumber = poleNumber;
    }

    public String getExistingPoleNumber() {
        return existingPoleNumber;
    }

    public void setExistingPoleNumber(String existingPoleNumber) {
        this.existingPoleNumber = existingPoleNumber;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public long getProcessedDateTime() {
        return processedDateTime;
    }

    public void setProcessedDateTime(long processedDateTime) {
        this.processedDateTime = processedDateTime;
    }

    @Override
    public String toString() {
        return "DuplicateMacAddress{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", noteguid='" + noteguid + '\'' +
                ", poleNumber='" + poleNumber + '\'' +
                ", existingPoleNumber='" + existingPoleNumber + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", processedDateTime=" + processedDateTime +
                '}';
    }
}
