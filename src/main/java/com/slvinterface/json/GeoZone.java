package com.slvinterface.json;


public class GeoZone {
    private int activityCardsCount;
    private int childrenCount;
    private int devicesCount;
    private int hierarchyActivityCardsCount;
    private int hierarchyDevicesCount;
    private int id;
    private String idsPath;
    private double latMax;
    private double latMin;
    private double lngMax;
    private double lngMin;
    private String name;
    private String namesPath;
    private int parentId;
    private String properties;
    private String type;

    public int getActivityCardsCount() {
        return activityCardsCount;
    }

    public void setActivityCardsCount(int activityCardsCount) {
        this.activityCardsCount = activityCardsCount;
    }

    public int getChildrenCount() {
        return childrenCount;
    }

    public void setChildrenCount(int childrenCount) {
        this.childrenCount = childrenCount;
    }

    public int getDevicesCount() {
        return devicesCount;
    }

    public void setDevicesCount(int devicesCount) {
        this.devicesCount = devicesCount;
    }

    public int getHierarchyActivityCardsCount() {
        return hierarchyActivityCardsCount;
    }

    public void setHierarchyActivityCardsCount(int hierarchyActivityCardsCount) {
        this.hierarchyActivityCardsCount = hierarchyActivityCardsCount;
    }

    public int getHierarchyDevicesCount() {
        return hierarchyDevicesCount;
    }

    public void setHierarchyDevicesCount(int hierarchyDevicesCount) {
        this.hierarchyDevicesCount = hierarchyDevicesCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdsPath() {
        return idsPath;
    }

    public void setIdsPath(String idsPath) {
        this.idsPath = idsPath;
    }

    public double getLatMax() {
        return latMax;
    }

    public void setLatMax(double latMax) {
        this.latMax = latMax;
    }

    public double getLatMin() {
        return latMin;
    }

    public void setLatMin(double latMin) {
        this.latMin = latMin;
    }

    public double getLngMax() {
        return lngMax;
    }

    public void setLngMax(double lngMax) {
        this.lngMax = lngMax;
    }

    public double getLngMin() {
        return lngMin;
    }

    public void setLngMin(double lngMin) {
        this.lngMin = lngMin;
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

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "GeoZone{" +
                "activityCardsCount=" + activityCardsCount +
                ", childrenCount=" + childrenCount +
                ", devicesCount=" + devicesCount +
                ", hierarchyActivityCardsCount=" + hierarchyActivityCardsCount +
                ", hierarchyDevicesCount=" + hierarchyDevicesCount +
                ", id=" + id +
                ", idsPath='" + idsPath + '\'' +
                ", latMax=" + latMax +
                ", latMin=" + latMin +
                ", lngMax=" + lngMax +
                ", lngMin=" + lngMin +
                ", name='" + name + '\'' +
                ", namesPath='" + namesPath + '\'' +
                ", parentId=" + parentId +
                ", properties='" + properties + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}

