package com.terragoedge.slvinterface.model;

import com.terragoedge.slvinterface.dao.tables.GeozoneEntity;

public class GeozoneModel {
    int id;
    int parentId;
    String type;
    private int childrenCount;
    private String name;
    private String namesPath;

    public GeozoneModel() {

    }

    public GeozoneModel(GeozoneEntity geozoneEntity) {
        this.id = geozoneEntity.getParishzoneId();
        this.parentId = geozoneEntity.getChildgeozoneId();
    }

    public int getChildrenCount() {
        return childrenCount;
    }

    public void setChildrenCount(int childrenCount) {
        this.childrenCount = childrenCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamesPath() {
        return namesPath;
    }

    public void setNamesPath(String namesPath) {
        this.namesPath = namesPath;
    }
}
