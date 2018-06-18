package com.terragoedge.edgeserver;

import java.util.Objects;

public class SlvData {
    private String guid;
    private String location;
    private String title;

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlvData slvData = (SlvData) o;
        return Objects.equals(location, slvData.location) &&
                Objects.equals(title, slvData.title);
    }

    @Override
    public int hashCode() {

        return Objects.hash(location, title);
    }
}
