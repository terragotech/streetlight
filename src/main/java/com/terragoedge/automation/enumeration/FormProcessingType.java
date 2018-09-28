package com.terragoedge.automation.enumeration;

public enum FormProcessingType {
    MAC_ADDRESS, PALLET_NUMBER;
    private int propsType;

    public int getPropsType() {
        return propsType;
    }

    public void setPropsType(int propsType) {
        this.propsType = propsType;
    }
}
