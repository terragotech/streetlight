package com.terragoedge.edgeserver;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "edge_all_serial_number")
public class EdgeAllSerialNumber {

    public static final String TITLE = "title";
    @DatabaseField(columnName = "title")
    private String title;
    @DatabaseField(columnName = "serialnumber")
    private String serialNumber;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override
    public String toString() {
        return "EdgeAllSerialNumber{" +
                "title='" + title + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                '}';
    }
}
