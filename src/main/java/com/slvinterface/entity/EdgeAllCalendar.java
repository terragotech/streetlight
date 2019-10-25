package com.slvinterface.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "edge_all_calendar")
public class EdgeAllCalendar {

    public static String TITLE = "title";
    public static String EDGER_CALENDAR = "edge_calendar";


    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = "title")
    private String noteTitle;

    @DatabaseField(columnName = "edge_calendar")
   private String edgeCalendar;

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

    public String getEdgeCalendar() {
        return edgeCalendar;
    }

    public void setEdgeCalendar(String edgeCalendar) {
        this.edgeCalendar = edgeCalendar;
    }

    @Override
    public String toString() {
        return "EdgeAllCalendar{" +
                "id=" + id +
                ", noteTitle='" + noteTitle + '\'' +
                ", edgeCalendar='" + edgeCalendar + '\'' +
                '}';
    }
}
