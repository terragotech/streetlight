package com.terragoedge.streetlight.installmaintain.json;

public class Ids {
    private int mac;
    private int fix;
    private int exMac;
    private int exFix;
    private int remove;

    public int getRemove() {
        return remove;
    }

    public void setRemove(int remove) {
        this.remove = remove;
    }

    public int getExMac() {
        return exMac;
    }

    public void setExMac(int exMac) {
        this.exMac = exMac;
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
                ", exMac=" + exMac +
                ", exFix=" + exFix +
                ", remove=" + remove +
                '}';
    }
}
