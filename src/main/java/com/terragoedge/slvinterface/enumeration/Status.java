package com.terragoedge.slvinterface.enumeration;
public enum Status {
    Success(0), Failure(1);
    private int statusId;

    private Status(int statusId) {
        this.statusId = statusId;
    }

    public int getStatus() {
        return statusId;
    }
}