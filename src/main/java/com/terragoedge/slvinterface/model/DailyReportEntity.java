package com.terragoedge.slvinterface.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "slvdata")
public class DailyReportEntity {
    @DatabaseField(columnName = "title")
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "DailyReportEntity{" +
                "title='" + title + '\'' +
                '}';
    }
}
