package com.syncresources;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "EdgeForm")
public class EdgeFormMobile {
    @DatabaseField(columnName = "FormDef")
    private String formDef;

    public String getFormDef() {
        return formDef;
    }

    public void setFormDef(String formDef) {
        this.formDef = formDef;
    }

    @Override
    public String toString() {
        return "EdgeFormMobile{" +
                "formDef='" + formDef + '\'' +
                '}';
    }
}
