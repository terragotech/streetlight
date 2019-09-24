package com.terragoedge.streetlight.dao;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
// create table client_account_name(key text,value text,max int,area text);
@DatabaseTable(tableName = "client_account_name")
public class ClientAccountEntity {
    @DatabaseField(columnName = "key")
    private String key;
    @DatabaseField(columnName = "value")
    private String value;
    @DatabaseField(columnName = "max")
    private int max;
    @DatabaseField(columnName = "area")
    private String area;

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

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

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    @Override
    public String toString() {
        return "ClientAccountEntity{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                ", max=" + max +
                ", area='" + area + '\'' +
                '}';
    }
}
