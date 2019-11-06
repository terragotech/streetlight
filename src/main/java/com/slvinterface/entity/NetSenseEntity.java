package com.slvinterface.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "netsensedata")
public class NetSenseEntity {
    @DatabaseField(columnName = "lastupdatedtime")
    private long lastUpdateTime;
}
