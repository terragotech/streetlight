package com.terragoedge.slvinterface.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "inventorycustodyexceptionreport")
public class InventoryReport {
    @DatabaseField(columnName = "id", generatedId = true)
    private int id;
    @DatabaseField(columnName = "sourcenoteguid")
    private String sourcenoteguid;
    @DatabaseField(columnName = "macaddress")
    private String macaddress;
    @DatabaseField(columnName = "processingnoteguid")
    private String processingnoteguid;
    @DatabaseField(columnName = "currentlocation")
    private String currentlocation;
    @DatabaseField(columnName = "expectedlocation")
    private String expectedlocation;
    @DatabaseField(columnName = "destinationlocation")
    private String destinationlocation;
    @DatabaseField(columnName = "action")
    private String action;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSourcenoteguid() {
        return sourcenoteguid;
    }

    public void setSourcenoteguid(String sourcenoteguid) {
        this.sourcenoteguid = sourcenoteguid;
    }

    public String getMacaddress() {
        return macaddress;
    }

    public void setMacaddress(String macaddress) {
        this.macaddress = macaddress;
    }

    public String getProcessingnoteguid() {
        return processingnoteguid;
    }

    public void setProcessingnoteguid(String processingnoteguid) {
        this.processingnoteguid = processingnoteguid;
    }

    public String getCurrentlocation() {
        return currentlocation;
    }

    public void setCurrentlocation(String currentlocation) {
        this.currentlocation = currentlocation;
    }

    public String getExpectedlocation() {
        return expectedlocation;
    }

    public void setExpectedlocation(String expectedlocation) {
        this.expectedlocation = expectedlocation;
    }

    public String getDestinationlocation() {
        return destinationlocation;
    }

    public void setDestinationlocation(String destinationlocation) {
        this.destinationlocation = destinationlocation;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "InventoryReport{" +
                "id=" + id +
                ", sourcenoteguid='" + sourcenoteguid + '\'' +
                ", macaddress='" + macaddress + '\'' +
                ", processingnoteguid='" + processingnoteguid + '\'' +
                ", currentlocation='" + currentlocation + '\'' +
                ", expectedlocation='" + expectedlocation + '\'' +
                ", destinationlocation='" + destinationlocation + '\'' +
                ", action='" + action + '\'' +
                '}';
    }
}
