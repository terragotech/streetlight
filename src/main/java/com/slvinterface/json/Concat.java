package com.slvinterface.json;

public class Concat {
    private String concatString;
    private String defaultValue;

    public String getConcatString() {
        return concatString;
    }

    public void setConcatString(String concatString) {
        this.concatString = concatString;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        return "Concat{" +
                "concatString='" + concatString + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                '}';
    }
}
