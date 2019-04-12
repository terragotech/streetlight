package com.terragoedge.edgeserver;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "edge_all_mac")
public class EdgeAllMacData {

    @DatabaseField(columnName = "title")
    private String title;
    @DatabaseField(columnName = "macaddress")
    private String macAddress;

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

    @Override
    public String toString() {
        return "EdgeAllMacData{" +
                "title='" + title + '\'' +
                ", macAddress='" + macAddress + '\'' +
                '}';
    }
}
