package com.terragoedge.automation.enumeration;
public enum Status {
    Success(0), Failure(1),LoadForAssignmentPresent(2),MoreThan_One_FormsPresent(3) ;
    private int statusId;

    private Status(int statusId) {
        this.statusId = statusId;
    }

    public int getStatus() {
        return statusId;
    }
}