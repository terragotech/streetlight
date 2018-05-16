package com.terragoedge.streetlight.logging;

public class InstallMaintenanceLogModel extends  LoggingModel {

    private boolean isInstallFormPresent;
    private boolean isProcessOtherForm;

    public boolean isProcessOtherForm() {
        return isProcessOtherForm;
    }

    public void setProcessOtherForm(boolean processOtherForm) {
        isProcessOtherForm = processOtherForm;
    }

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
                ", isProcessOtherForm=" + isProcessOtherForm +
                '}';
    }
}
