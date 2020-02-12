package com.slvinterface.entity;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "promotedformdata")
public class PromotedFormDataEntity {

    public static String PROMOTED_ID = "promotedid";
    public static String PROMOTED_VALUE = "promotedvalue";
    public static String PARENT_NOTE_GUID = "parentnoteguid";
    public static String NOTEBOOK_GUID = "notebookguid";
    public static String LAST_UPDATED_DATE_TIME = "lastupdateddatetime";

    @DatabaseField(columnName = "promotedid",generatedId = false)
    private long promotedId;
    @DatabaseField(columnName = "promotedvalue")
    private String promotedvalue;
    @DatabaseField(columnName = "parentnoteguid")
    private String parentnoteguid;
    @DatabaseField(columnName = "notebookguid")
    private String notebookguid;
    @DatabaseField(columnName = "lastupdateddatetime")
    private long lastupdateddatetime;


    public long getPromotedId() {
        return promotedId;
    }

    public void setPromotedId(long promotedId) {
        this.promotedId = promotedId;
    }

    public String getPromotedvalue() {
        return promotedvalue;
    }

    public void setPromotedvalue(String promotedvalue) {
        this.promotedvalue = promotedvalue;
    }

    public String getParentnoteguid() {
        return parentnoteguid;
    }

    public void setParentnoteguid(String parentnoteguid) {
        this.parentnoteguid = parentnoteguid;
    }

    public String getNotebookguid() {
        return notebookguid;
    }

    public void setNotebookguid(String notebookguid) {
        this.notebookguid = notebookguid;
    }

    public long getLastupdateddatetime() {
        return lastupdateddatetime;
    }

    public void setLastupdateddatetime(long lastupdateddatetime) {
        this.lastupdateddatetime = lastupdateddatetime;
    }

    @Override
    public String toString() {
        return "PromotedFormDataEntity{" +
                "promotedId=" + promotedId +
                ", promotedvalue='" + promotedvalue + '\'' +
                ", parentnoteguid='" + parentnoteguid + '\'' +
                ", notebookguid='" + notebookguid + '\'' +
                ", lastupdateddatetime=" + lastupdateddatetime +
                '}';
    }
}
