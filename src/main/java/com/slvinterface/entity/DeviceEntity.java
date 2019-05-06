package com.slvinterface.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "device")
public class DeviceEntity {
    public static String TITLE = "title";
    public static String MAC_ADDRESS = "macaddress";
    public static String DIMMING_GROUP = "dimminggroup";
    public static String INSTALL_STATUS = "installstatus";
    public static String INSTALL_DATE = "installdate";


    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = "title")
    private String noteTitle;

    @DatabaseField(columnName = "macaddress")
    private String macAddress;

    @DatabaseField(columnName = "dimminggroup")
    private String dimmingGroup;

    @DatabaseField(columnName = "installstatus")
    private String installStatus;

    @DatabaseField(columnName = "installdate")
    private String installDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    public void setNoteTitle(String noteTitle) {
        this.noteTitle = noteTitle;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getDimmingGroup() {
        return dimmingGroup;
    }

    public void setDimmingGroup(String dimmingGroup) {
        this.dimmingGroup = dimmingGroup;
    }

    public String getInstallStatus() {
        return installStatus;
    }

    public void setInstallStatus(String installStatus) {
        this.installStatus = installStatus;
    }

    public String getInstallDate() {
        return installDate;
    }

    public void setInstallDate(String installDate) {
        this.installDate = installDate;
    }
}
