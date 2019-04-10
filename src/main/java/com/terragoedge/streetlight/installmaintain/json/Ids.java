package com.terragoedge.streetlight.installmaintain.json;

public class Ids {
    private int mac;
    private int fix;
    private int exMac;
    private int exFix;
    private int remove;
    private int issue;
    private int comment;
    private int scanifwrong;
    private int unabletorepairissue;
    private int installstatus;


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

    public int getIssue() {
        return issue;
    }

    public void setIssue(int issue) {
        this.issue = issue;
    }

    public int getComment() {
        return comment;
    }

    public void setComment(int comment) {
        this.comment = comment;
    }

    public int getScanifwrong() {
        return scanifwrong;
    }

    public void setScanifwrong(int scanifwrong) {
        this.scanifwrong = scanifwrong;
    }

    public int getUnabletorepairissue() {
        return unabletorepairissue;
    }

    public void setUnabletorepairissue(int unabletorepairissue) {
        this.unabletorepairissue = unabletorepairissue;
    }

    public int getInstallstatus() {
        return installstatus;
    }

    public void setInstallstatus(int installstatus) {
        this.installstatus = installstatus;
    }

    @Override
    public String toString() {
        return "Ids{" +
                "mac=" + mac +
                ", fix=" + fix +
                ", exMac=" + exMac +
                ", exFix=" + exFix +
                ", remove=" + remove +
                ", issue=" + issue +
                ", comment=" + comment +
                ", scanifwrong=" + scanifwrong +
                ", unabletorepairissue=" + unabletorepairissue +
                ", installstatus=" + installstatus +
                '}';
    }
}
