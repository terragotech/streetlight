package com.terragoedge.streetlight.json.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "slv_duplicate_macaddress")
public class DuplicateMacAddress {
    @DatabaseField(columnName = "title")
    private String title;
    @DatabaseField(columnName = "noteguid")
    private String noteguid;
    @DatabaseField(columnName = "macaddress")
    private String macaddress;

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

    public String getMacaddress() {
        return macaddress;
    }

    public void setMacaddress(String macaddress) {
        this.macaddress = macaddress;
    }

    @Override
    public String toString() {
        return "DuplicateMacAddress{" +
                "title='" + title + '\'' +
                ", noteguid='" + noteguid + '\'' +
                ", macaddress='" + macaddress + '\'' +
                '}';
    }
}
