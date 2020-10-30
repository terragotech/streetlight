package com.slvinterface.json;

public class DateConfig {
    private String format;
    private String timeZone;

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public String toString() {
        return "DateConfig{" +
                "format='" + format + '\'' +
                ", timeZone='" + timeZone + '\'' +
                '}';
    }
}
