package com.terragoedge.slvinterface.dao.tables;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.terragoedge.slvinterface.enumeration.Status;

@DatabaseTable(tableName = "slvdevice")
public class SlvDevice {
    @DatabaseField(columnName = "id",generatedId = true)
    private int id;
    @DatabaseField(columnName = "title")
    private String title;
    @DatabaseField(columnName = "noteguid")
    private String noteguid;
    @DatabaseField(columnName = "pole_number")
    private String poleNumber;
    @DatabaseField(columnName = "old_pole_number")
    private String oldPoleNumber;
    @DatabaseField(columnName = "created_date_time")
    private long createdDateTime;
    @DatabaseField(columnName = "processed_date_time")
    private long processedDateTime;
    @DatabaseField(columnName = "status",dataType = DataType.ENUM_STRING)
    private Status status;
    @DatabaseField(columnName = "slv_reponse",dataType = DataType.LONG_STRING)
    private String slvResponse;

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

    public String getNoteguid() {
        return noteguid;
    }

    public void setNoteguid(String noteguid) {
        this.noteguid = noteguid;
    }

    public String getPoleNumber() {
        return poleNumber;
    }

    public void setPoleNumber(String poleNumber) {
        this.poleNumber = poleNumber;
    }

    public String getOldPoleNumber() {
        return oldPoleNumber;
    }

    public void setOldPoleNumber(String oldPoleNumber) {
        this.oldPoleNumber = oldPoleNumber;
    }

    public long getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(long createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public long getProcessedDateTime() {
        return processedDateTime;
    }

    public void setProcessedDateTime(long processedDateTime) {
        this.processedDateTime = processedDateTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getSlvResponse() {
        return slvResponse;
    }

    public void setSlvResponse(String slvResponse) {
        this.slvResponse = slvResponse;
    }

    @Override
    public String toString() {
        return "SlvDevice{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", noteguid='" + noteguid + '\'' +
                ", poleNumber='" + poleNumber + '\'' +
                ", oldPoleNumber='" + oldPoleNumber + '\'' +
                ", createdDateTime=" + createdDateTime +
                ", processedDateTime=" + processedDateTime +
                ", status='" + status + '\'' +
                ", slvResponse='" + slvResponse + '\'' +
                '}';
    }
}
