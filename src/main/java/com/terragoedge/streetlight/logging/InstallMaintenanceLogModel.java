package com.terragoedge.streetlight.logging;

public class InstallMaintenanceLogModel extends  LoggingModel {

    private boolean isInstallFormPresent;
private boolean isFixtureOnly;

    public boolean isInstallFormPresent() {
        return isInstallFormPresent;
    }

    public void setInstallFormPresent(boolean installFormPresent) {
        isInstallFormPresent = installFormPresent;
    }

    public boolean isFixtureOnly() {
        return isFixtureOnly;
    }

    public void setFixtureOnly(boolean fixtureOnly) {
        isFixtureOnly = fixtureOnly;
    }

    @Override
    public String toString() {
        return "InstallMaintenanceLogModel{" +
                "isInstallFormPresent=" + isInstallFormPresent +
                '}';
    }
}
