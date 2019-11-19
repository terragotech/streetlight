package com.terragoedge.streetlight.json.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "installation_removed_failure_log")
public class InstallationRemovedFailureLog {

    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(columnName = "title")
    private String title;

    @DatabaseField(columnName = "slv_mac")
    private String slvMAC;

    @DatabaseField(columnName = "reason")
    private String reason;

    @DatabaseField(columnName = "noteguid")
    private String noteGuid;

    @DatabaseField(columnName = "create_date_time")
    private long createDateTime;

    @DatabaseField(columnName = "event_time")
    private long evenTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlvMAC() {
        return slvMAC;
    }

    public void setSlvMAC(String slvMAC) {
        this.slvMAC = slvMAC;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNoteGuid() {
        return noteGuid;
    }

    public void setNoteGuid(String noteGuid) {
        this.noteGuid = noteGuid;
    }

    public long getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(long createDateTime) {
        this.createDateTime = createDateTime;
    }

    public long getEvenTime() {
        return evenTime;
    }

    public void setEvenTime(long evenTime) {
        this.evenTime = evenTime;
    }

    @Override
    public String toString() {
        return "InstallationRemovedFailureLog{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", slvMAC='" + slvMAC + '\'' +
                ", reason='" + reason + '\'' +
                ", noteGuid='" + noteGuid + '\'' +
                ", createDateTime=" + createDateTime +
                ", evenTime=" + evenTime +
                '}';
    }
}

