package com.slvinterface.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "edge_all_mac_phi")
public class EdgeAllMac {

    public static String TITLE = "title";
    public static String MAC_ADDRESS = "macaddress";

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = "title")
    private String noteTitle;

    @DatabaseField(columnName = "macaddress")
    private String macAddress;

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

    @Override
    public String toString() {
        return "EdgeAllMac{" +
                "id=" + id +
                ", noteTitle='" + noteTitle + '\'' +
                ", macAddress='" + macAddress + '\'' +
                '}';
    }
}
