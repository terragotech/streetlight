package com.terragoedge.streetlight.installmaintain;

import java.util.HashSet;
import java.util.Set;

public class DuplicateModel {
    private String title;
    private String macAddress;
    private Set<String> titles = new HashSet<>();

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

    public Set<String> getTitles() {
        return titles;
    }

    public void setTitles(Set<String> titles) {
        this.titles = titles;
    }

    @Override
    public String toString() {
        return "DuplicateModel{" +
                "title='" + title + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", titles=" + titles +
                '}';
    }
}
