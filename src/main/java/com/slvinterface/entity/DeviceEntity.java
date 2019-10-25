package com.slvinterface.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


public class DeviceEntity {
    private int deviceId;
    private String dimmingGroup;

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getDimmingGroup() {
        return dimmingGroup;
    }

    public void setDimmingGroup(String dimmingGroup) {
        this.dimmingGroup = dimmingGroup;
    }

    @Override
    public String toString() {
        return "DeviceEntity{" +
                "deviceId=" + deviceId +
                ", dimmingGroup='" + dimmingGroup + '\'' +
                '}';
    }
}
