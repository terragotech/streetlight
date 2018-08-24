package com.report.automation.model;

import java.util.Objects;

public class WeeklyReportModel {
    String fixture_number;
    String macAddress;
    String install_date;
    int repeatedSize;

    public String getFixture_number() {
        return fixture_number;
    }

    public void setFixture_number(String fixture_number) {
        this.fixture_number = fixture_number;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getInstall_date() {
        return install_date;
    }

    public void setInstall_date(String install_date) {
        this.install_date = install_date;
    }

    public int getRepeatedSize() {
        return repeatedSize;
    }

    public void setRepeatedSize(int repeatedSize) {
        this.repeatedSize = repeatedSize;
    }
/*@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeeklyReportModel that = (WeeklyReportModel) o;
        boolean isExist = macAddress.equals(that.macAddress);
        if (fixture_number.isEmpty() && isExist)
            return true;
        else if(fixture_number.equals(that.fixture_number))
            return true;
        return false;
        // return Objects.equals(macAddress, that.macAddress);
    }*/
/*

    @Override
    public int hashCode() {

        return Objects.hash(macAddress, fixture_number);
    }
*/

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeeklyReportModel that = (WeeklyReportModel) o;
        return Objects.equals(macAddress, that.macAddress);
    }

    @Override
    public int hashCode() {

        return Objects.hash(macAddress);
    }

    @Override
    public String toString() {
        return "WeeklyReportModel{" +
                "fixture_number='" + fixture_number + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", install_date='" + install_date + '\'' +
                '}';
    }
}
