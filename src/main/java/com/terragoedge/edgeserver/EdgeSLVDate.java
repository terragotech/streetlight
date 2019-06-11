package com.terragoedge.edgeserver;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "edge_slv_date")
public class EdgeSLVDate {

    @DatabaseField(columnName = "title")
    private String title;
    @DatabaseField(columnName = "edge_date")
    private String edgeDate;
    @DatabaseField(columnName = "dates_type")
    private String datesType;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEdgeDate() {
        return edgeDate;
    }

    public void setEdgeDate(String edgeDate) {
        this.edgeDate = edgeDate;
    }

    public String getDatesType() {
        return datesType;
    }

    public void setDatesType(String datesType) {
        this.datesType = datesType;
    }

    @Override
    public String toString() {
        return "EdgeSLVDate{" +
                "title='" + title + '\'' +
                ", edgeDate='" + edgeDate + '\'' +
                ", datesType='" + datesType + '\'' +
                '}';
    }
}
