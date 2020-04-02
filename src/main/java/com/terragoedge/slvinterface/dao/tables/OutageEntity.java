package com.terragoedge.slvinterface.dao.tables;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "outagedata")
public class OutageEntity{
        public final static String FIXTUREID = "fixtureid";
        @DatabaseField(columnName = "fixtureid")
        private String fixtureid;

        public final static String LABEL = "label";
        @DatabaseField(columnName = "label")
        private String label;

        public final static String DEVICE_CATEGORY = "devicecategory";
        @DatabaseField(columnName = "devicecategory")
        private String devicecategory;

        public final static String DEVICE_ID = "deviceid";
        @DatabaseField(columnName = "deviceid")
        private String deviceid;

        public final static String WARNING = "WARNING";
        @DatabaseField(columnName = "warning")
        private String warning;

        public final static String OUTAGE = "OUTAGE";
        @DatabaseField(columnName = "outage")
        private String outage;

        public final static String IS_COMPLETE = "iscomplete";
        @DatabaseField(columnName = "iscomplete")
        private String iscomplete;

        public final static String FAILURE_REASON = "failurereason";
        @DatabaseField(columnName = "failurereason",dataType = DataType.LONG_STRING)
        private String failurereason;

        public final static String LAST_UPDATE = "lastupdate";
        @DatabaseField(columnName = "lastupdate")
        private String lastupdate;

        public final static String FAILED_SINCE = "failedsince";
        @DatabaseField(columnName = "failedsince",dataType = DataType.LONG_STRING)
        private String failedsince;

        public final static String BURNING_HOURS = "burninghours";
        @DatabaseField(columnName = "burninghours")
        private String burninghours;

        public final static String LIFE_TIME = "lifetime";
        @DatabaseField(columnName = "lifetime")
        private String lifetime;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDevicecategory() {
        return devicecategory;
    }

    public void setDevicecategory(String devicecategory) {
        this.devicecategory = devicecategory;
    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    public String getOutage() {
        return outage;
    }

    public void setOutage(String outage) {
        this.outage = outage;
    }

    public String getIscomplete() {
        return iscomplete;
    }

    public void setIscomplete(String iscomplete) {
        this.iscomplete = iscomplete;
    }

    public String getFailurereason() {
        return failurereason;
    }

    public void setFailurereason(String failurereason) {
        this.failurereason = failurereason;
    }

    public String getLastupdate() {
        return lastupdate;
    }

    public void setLastupdate(String lastupdate) {
        this.lastupdate = lastupdate;
    }

    public String getFailedsince() {
        return failedsince;
    }

    public void setFailedsince(String failedsince) {
        this.failedsince = failedsince;
    }

    public String getBurninghours() {
        return burninghours;
    }

    public void setBurninghours(String burninghours) {
        this.burninghours = burninghours;
    }

    public String getFixtureid() {
        return fixtureid;
    }

    public void setFixtureid(String fixtureid) {
        this.fixtureid = fixtureid;
    }

    public String getLifetime() {
        return lifetime;
    }

    public void setLifetime(String lifetime) {
        this.lifetime = lifetime;
    }
}
