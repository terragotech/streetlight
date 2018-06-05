package com.terragoedge.streetlight.json.model;

import java.util.ArrayList;
import java.util.List;

public class ContextList {

    private Object columnLabels;
    private List<String> columns = new ArrayList<>();
    private Integer columnsCount;
    private Properties properties;
    private Integer rowsCount;
    private Object sortCaseSensitive;
    private Object sortColumn;
    private Object sortDesc;
    private List<List<String>> values = new ArrayList<>();

    public Object getColumnLabels() {
        return columnLabels;
    }

    public void setColumnLabels(Object columnLabels) {
        this.columnLabels = columnLabels;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public Integer getColumnsCount() {
        return columnsCount;
    }

    public void setColumnsCount(Integer columnsCount) {
        this.columnsCount = columnsCount;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Integer getRowsCount() {
        return rowsCount;
    }

    public void setRowsCount(Integer rowsCount) {
        this.rowsCount = rowsCount;
    }

    public Object getSortCaseSensitive() {
        return sortCaseSensitive;
    }

    public void setSortCaseSensitive(Object sortCaseSensitive) {
        this.sortCaseSensitive = sortCaseSensitive;
    }

    public Object getSortColumn() {
        return sortColumn;
    }

    public void setSortColumn(Object sortColumn) {
        this.sortColumn = sortColumn;
    }

    public Object getSortDesc() {
        return sortDesc;
    }

    public void setSortDesc(Object sortDesc) {
        this.sortDesc = sortDesc;
    }

    public List<List<String>> getValues() {
        return values;
    }

    public void setValues(List<List<String>> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "ContextList{" +
                "columnLabels=" + columnLabels +
                ", columns=" + columns +
                ", columnsCount=" + columnsCount +
                ", properties=" + properties +
                ", rowsCount=" + rowsCount +
                ", sortCaseSensitive=" + sortCaseSensitive +
                ", sortColumn=" + sortColumn +
                ", sortDesc=" + sortDesc +
                ", values=" + values +
                '}';
    }
}
