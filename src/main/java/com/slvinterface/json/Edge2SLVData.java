package com.slvinterface.json;

import com.slvinterface.enumeration.SLVProcess;

public class Edge2SLVData {

    private String title;
    private String macAddress;
    private Priority priority;

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

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "Edge2SLVData{" +
                "title='" + title + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", priority=" + priority +
                '}';
    }
}
