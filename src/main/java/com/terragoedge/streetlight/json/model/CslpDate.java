package com.terragoedge.streetlight.json.model;

public class CslpDate {

    private String cslpLumDate;
    private String cslpNodeDate;

    public CslpDate(String cslpLumDate, String cslpNodeDate) {
        this.cslpLumDate = cslpLumDate;
        this.cslpNodeDate = cslpNodeDate;
    }

    public CslpDate() {
    }

    public String getCslpLumDate() {
        return cslpLumDate;
    }

    public void setCslpLumDate(String cslpLumDate) {
        this.cslpLumDate = cslpLumDate;
    }

    public String getCslpNodeDate() {
        return cslpNodeDate;
    }

    public void setCslpNodeDate(String cslpNodeDate) {
        this.cslpNodeDate = cslpNodeDate;
    }

    @Override
    public String toString() {
        return "CslpDate{" +
                "cslpLumDate='" + cslpLumDate + '\'' +
                ", cslpNodeDate='" + cslpNodeDate + '\'' +
                '}';
    }


}
