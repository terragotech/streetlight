package com.terragoedge.streetlight.logging;

public class InstallMaintenanceLogModel extends  LoggingModel {

    private boolean isInstallFormPresent;


    public boolean isInstallFormPresent() {
        return isInstallFormPresent;
    }

    public void setInstallFormPresent(boolean installFormPresent) {
        isInstallFormPresent = installFormPresent;
    }

    @Override
    public String toString() {
        return "InstallMaintenanceLogModel{" +
                "isInstallFormPresent=" + isInstallFormPresent +
                '}';
    }
}
