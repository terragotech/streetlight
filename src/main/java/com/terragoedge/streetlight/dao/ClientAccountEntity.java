package com.terragoedge.streetlight.dao;

import com.j256.ormlite.table.DatabaseTable;
// create table client_account_name(key text,value text,max int);
@DatabaseTable(tableName = "client_account_name")
public class ClientAccountEntity {
    private String key;
    private String value;
    private int max;

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
                '}';
    }
}
