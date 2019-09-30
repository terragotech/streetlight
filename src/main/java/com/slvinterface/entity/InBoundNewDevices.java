package com.slvinterface.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "tmp_newdevices")
public class InBoundNewDevices {
    @DatabaseField(columnName = "idoncontroller")
    private String idoncontroller;
}
