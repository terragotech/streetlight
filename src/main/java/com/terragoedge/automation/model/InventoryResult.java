package com.terragoedge.automation.model;

public class InventoryResult {
    String noteGuid;
    String title;
    String scanning;
    String palletWorkflow;
    String action;
    String assignedToUser;
    String deliveryLocation;

    public String getNoteGuid() {
        return noteGuid;
    }

    public void setNoteGuid(String noteGuid) {
        this.noteGuid = noteGuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getScanning() {
        return scanning;
    }

    public void setScanning(String scanning) {
        this.scanning = scanning;
    }

    public String getPalletWorkflow() {
        return palletWorkflow;
    }

    public void setPalletWorkflow(String palletWorkflow) {
        this.palletWorkflow = palletWorkflow;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAssignedToUser() {
        return assignedToUser;
    }

    public void setAssignedToUser(String assignedToUser) {
        this.assignedToUser = assignedToUser;
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }
}
