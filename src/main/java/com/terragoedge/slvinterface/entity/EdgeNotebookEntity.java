package com.terragoedge.slvinterface.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "edgenotebook")
public class EdgeNotebookEntity {

    public static final String NOTEBOOK_ID = "notebookid";
    public static final String NOTEBOOK_DESC = "notebookDesc";
    public static final String NOTEBOOK_NAME = "notebookName";
    public static final String NOTEBOOK_GUID = "notebookGuid";
    public static final String LASTUPDATED_TIME = "lastUpdatedTime";
    public static final String CREATED_BY = "createdBy";
    public static final String DEFAULT_FORM_ID = "quickNoteFormTemplateID";
    public static final String NOTE_NAME_TYPE = "noteNameType";
    public static final String CUSTOM_NAME = "customName";
    public static final String IS_DELETED = "isDeleted";


    @DatabaseField(columnName = "notebookid")
    private int notebookId;
    @DatabaseField(columnName = "NotebookGuid")
    private String notebookGuid;
    @DatabaseField(columnName = "NotebookDesc")
    private String notebookDesc;
    @DatabaseField(columnName = "NotebookName")
    private String notebookName;
    @DatabaseField(columnName = "lastupdatedtime")
    private long lastUpdatedTime;
    @DatabaseField(columnName = "createdBy")
    public String createdBy;

    @DatabaseField(columnName = "notenametype")
    private String noteNameType;
    @DatabaseField(columnName = "customname")
    private String customName;
    @DatabaseField(columnName = "isDeleted")
    private boolean isDeleted;

    @DatabaseField(columnName = "isIncludeDateTime")
    private boolean isIncludeDateTime;

    public EdgeNotebookEntity() {

    }

    public int getNotebookId() {
        return notebookId;
    }

    public void setNotebookId(int notebookId) {
        this.notebookId = notebookId;
    }

    public String getNotebookDesc() {
        return notebookDesc;
    }

    public void setNotebookDesc(String notebookDesc) {
        this.notebookDesc = notebookDesc;
    }

    public String getNotebookName() {
        return notebookName;
    }

    public void setNotebookName(String notebookName) {
        this.notebookName = notebookName;
    }

    public String getNotebookGuid() {
        return notebookGuid;
    }

    public long getLastupdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastupdatedTime(long lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public void setNotebookGuid(String notebookGuid) {
        this.notebookGuid = notebookGuid;
    }


    public boolean isIncludeDateTime() {
        return isIncludeDateTime;
    }

    public void setIncludeDateTime(boolean isIncludeDateTime) {
        this.isIncludeDateTime = isIncludeDateTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public long getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(long lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public String getNoteNameType() {
        return noteNameType;
    }

    public void setNoteNameType(String noteNameType) {
        this.noteNameType = noteNameType;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }


    public boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    @Override
    public String toString() {
        return "EdgeNotebookEntity{" +
                "notebookId=" + notebookId +
                ", notebookGuid='" + notebookGuid + '\'' +
                ", notebookDesc='" + notebookDesc + '\'' +
                ", notebookName='" + notebookName + '\'' +
                ", lastUpdatedTime=" + lastUpdatedTime +
                ", createdBy='" + createdBy + '\'' +
                ", noteNameType='" + noteNameType + '\'' +
                ", customName='" + customName + '\'' +
                ", isDeleted=" + isDeleted +
                ", isIncludeDateTime=" + isIncludeDateTime +
                '}';
    }
}
