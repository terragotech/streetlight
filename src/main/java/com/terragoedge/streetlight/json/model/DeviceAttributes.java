package com.terragoedge.streetlight.json.model;

public class DeviceAttributes {
    private String macAddress;
    private String installStatus;

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getInstallStatus() {
        return installStatus;
    }

    public void setInstallStatus(String installStatus) {
        this.installStatus = installStatus;
    }

    @Override
    public String toString() {
        return "DeviceAttributes{" +
                "macAddress='" + macAddress + '\'' +
                ", installStatus='" + installStatus + '\'' +
                '}';
    }
}
