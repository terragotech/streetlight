package com.terragoedge.slvinterface.enumeration;
public enum Status {
    Success(0), Failure(1), Error(2) ;
    private int statusId;

    private Status(int statusId) {
        this.statusId = statusId;
    }

    public int getStatus() {
        return statusId;
    }
}