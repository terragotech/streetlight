package com.terragoedge.slvinterface.dao.tables;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "slvdevice")
public class SlvDevice {
    public static final String SLV_DEVICE_ID = "deviceid";
    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    private int id;
    @DatabaseField(columnName = "devicename")
    private String deviceName;
    @DatabaseField(columnName = "deviceid")
    private String deviceId;
    @DatabaseField(columnName = "processeddatetime")
    private long processedDateTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public long getProcessedDateTime() {
        return processedDateTime;
    }

    public void setProcessedDateTime(long processedDateTime) {
        this.processedDateTime = processedDateTime;
    }

    @Override
    public String toString() {
        return "SlvDevice{" +
                "id=" + id +
                ", deviceName='" + deviceName + '\'' +
                ", deviceId=" + deviceId +
                ", processedDateTime=" + processedDateTime +
                '}';
    }
}
