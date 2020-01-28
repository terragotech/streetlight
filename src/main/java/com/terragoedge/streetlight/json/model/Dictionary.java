package com.terragoedge.streetlight.json.model;

public class Dictionary {
    private String key = null;
    private String value = null;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Dictionary{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
