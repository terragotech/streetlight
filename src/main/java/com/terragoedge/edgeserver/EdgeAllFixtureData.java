package com.terragoedge.edgeserver;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "edge_all_fix")
public class EdgeAllFixtureData {
    @DatabaseField(columnName = "title")
    private String title;
    @DatabaseField(columnName = "fixtureqrscan")
    private String fixtureQRScan;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFixtureQRScan() {
        return fixtureQRScan;
    }

    public void setFixtureQRScan(String fixtureQRScan) {
        this.fixtureQRScan = fixtureQRScan;
    }

    @Override
    public String toString() {
        return "EdgeAllFixtureData{" +
                "title='" + title + '\'' +
                ", fixtureQRScan='" + fixtureQRScan + '\'' +
                '}';
    }
}
