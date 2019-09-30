package com.slvinterface.entity;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "slvdata")
public class InBoundSLVData {
    @DatabaseField(columnName = "idoncontroller")
    private String idoncontroller;

}
