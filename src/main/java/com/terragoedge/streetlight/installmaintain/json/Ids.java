package com.terragoedge.streetlight.installmaintain.json;

public class Ids {
    private int mac;
    private int fix;
    private int exMax;
    private int exFix;

    public int getExMax() {
        return exMax;
    }

    public void setExMax(int exMax) {
        this.exMax = exMax;
    }

    public int getExFix() {
        return exFix;
    }

    public void setExFix(int exFix) {
        this.exFix = exFix;
    }

    public int getMac() {
        return mac;
    }

    public void setMac(int mac) {
        this.mac = mac;
    }

    public int getFix() {
        return fix;
    }

    public void setFix(int fix) {
        this.fix = fix;
    }

    @Override
    public String toString() {
        return "Ids{" +
                "mac=" + mac +
                ", fix=" + fix +
                ", exMax=" + exMax +
                ", exFix=" + exFix +
                '}';
    }
}
