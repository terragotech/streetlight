package com.terragoedge.automation.enumeration;

public enum ServerType {
    Install, Inventory;
    private String serverTypeId;

    public String getServerTypeId() {
        return serverTypeId;
    }

    public void setServerTypeId(String serverTypeId) {
        this.serverTypeId = serverTypeId;
    }
}
