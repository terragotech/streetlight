package com.terragoedge.edgeserver;

public class EdgeAllMacData {

    private String title;
    private String macAddress;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    @Override
    public String toString() {
        return "EdgeAllMacData{" +
                "title='" + title + '\'' +
                ", macAddress='" + macAddress + '\'' +
                '}';
    }
}
