package com.terragoedge.streetlight.installmaintain.json;

public class Ids {
    private int mac;
    private int fix;

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
                '}';
    }
}
