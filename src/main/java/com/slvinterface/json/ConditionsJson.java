package com.slvinterface.json;

import com.slvinterface.enumeration.SLVProcess;

import java.util.ArrayList;
import java.util.List;

public class ConditionsJson {
    private List<Priority> priority;
    private DeviceCreation deviceCreation;
    private List<Config> configList = new ArrayList<>();

    public List<Priority> getPriority() {
        return priority;
    }

    public void setPriority(List<Priority> priority) {
        this.priority = priority;
    }

    public DeviceCreation getDeviceCreation() {
        return deviceCreation;
    }

    public void setDeviceCreation(DeviceCreation deviceCreation) {
        this.deviceCreation = deviceCreation;
    }

    public List<Config> getConfigList() {
        return configList;
    }

    public void setConfigList(List<Config> configList) {
        this.configList = configList;
    }

    @Override
    public String toString() {
        return "ConditionsJson{" +
                "priority=" + priority +
                ", deviceCreation=" + deviceCreation +
                ", configList=" + configList +
                '}';
    }
}
