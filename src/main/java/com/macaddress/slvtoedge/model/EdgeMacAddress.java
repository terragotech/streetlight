package com.macaddress.slvtoedge.model;

import java.util.Objects;

public class EdgeMacAddress {
    private int id;
    private String title;
    String macAddress;
    String noteGuid;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getNoteGuid() {
        return noteGuid;
    }

    public void setNoteGuid(String noteGuid) {
        this.noteGuid = noteGuid;
    }

    @Override
    public String toString() {
        return "EdgeMacAddress{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", noteGuid='" + noteGuid + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EdgeMacAddress that = (EdgeMacAddress) o;
        return Objects.equals(title, that.title) && Objects.equals(macAddress, that.macAddress);
    }

    @Override
    public int hashCode() {

        return Objects.hash(title, macAddress);
    }
}
