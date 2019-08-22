package com.terrago.jsoncsvconvertor;

public class EdgeData {
    private String title;
    private String macAddress;
    private String exMacAddressRNF;
    private String macAddressRNF;
    private String exMacAddressRN;
    private String macAddressRN;
    private String action;
    private long createDateTime;


    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public long getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(long createDateTime) {
        this.createDateTime = createDateTime;
    }

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

    public String getExMacAddressRNF() {
        return exMacAddressRNF;
    }

    public void setExMacAddressRNF(String exMacAddressRNF) {
        this.exMacAddressRNF = exMacAddressRNF;
    }

    public String getMacAddressRNF() {
        return macAddressRNF;
    }

    public void setMacAddressRNF(String macAddressRNF) {
        this.macAddressRNF = macAddressRNF;
    }

    public String getExMacAddressRN() {
        return exMacAddressRN;
    }

    public void setExMacAddressRN(String exMacAddressRN) {
        this.exMacAddressRN = exMacAddressRN;
    }

    public String getMacAddressRN() {
        return macAddressRN;
    }

    public void setMacAddressRN(String macAddressRN) {
        this.macAddressRN = macAddressRN;
    }

    @Override
    public String toString() {
        return "EdgeData{" +
                "title='" + title + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", exMacAddressRNF='" + exMacAddressRNF + '\'' +
                ", macAddressRNF='" + macAddressRNF + '\'' +
                ", exMacAddressRN='" + exMacAddressRN + '\'' +
                ", macAddressRN='" + macAddressRN + '\'' +
                '}';
    }
}

