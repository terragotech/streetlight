package com.report.automation.model;

import java.util.Objects;

public class CommonReportModel extends WeeklyReportModel {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeeklyReportModel that = (WeeklyReportModel) o;
         return Objects.equals(fixture_number, that.fixture_number);
    }

    @Override
    public int hashCode() {

        return Objects.hash(fixture_number);
    }

}
