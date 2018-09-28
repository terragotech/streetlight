package com.terragoedge.automation.model;

public class CustodyResultModel {
   private String macaddress;
   private String currentinventorylocation;
   private String userselectedcurrentlocation;
   private  String destinationlocation;
   private String notedatetime;
   private String username;
   private String workflow;
   private String action;

    public String getMacaddress() {
        return macaddress;
    }

    public void setMacaddress(String macaddress) {
        this.macaddress = macaddress;
    }

    public String getCurrentinventorylocation() {
        return currentinventorylocation;
    }

    public void setCurrentinventorylocation(String currentinventorylocation) {
        this.currentinventorylocation = currentinventorylocation;
    }

    public String getUserselectedcurrentlocation() {
        return userselectedcurrentlocation;
    }

    public void setUserselectedcurrentlocation(String userselectedcurrentlocation) {
        this.userselectedcurrentlocation = userselectedcurrentlocation;
    }

    public String getDestinationlocation() {
        return destinationlocation;
    }

    public void setDestinationlocation(String destinationlocation) {
        this.destinationlocation = destinationlocation;
    }

    public String getNotedatetime() {
        return notedatetime;
    }

    public void setNotedatetime(String notedatetime) {
        this.notedatetime = notedatetime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getWorkflow() {
        return workflow;
    }

    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "CustodyResultModel{" +
                "macaddress='" + macaddress + '\'' +
                ", currentinventorylocation='" + currentinventorylocation + '\'' +
                ", userselectedcurrentlocation='" + userselectedcurrentlocation + '\'' +
                ", destinationlocation='" + destinationlocation + '\'' +
                ", notedatetime='" + notedatetime + '\'' +
                ", username='" + username + '\'' +
                ", workflow='" + workflow + '\'' +
                ", action='" + action + '\'' +
                '}';
    }
}
