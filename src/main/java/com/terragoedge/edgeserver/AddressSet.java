package com.terragoedge.edgeserver;

import java.util.Objects;

public class AddressSet {
    private String address;
    private String title;



    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
        AddressSet that = (AddressSet) o;
        return Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title);
    }

    @Override
    public String toString() {
        return "AddressSet{" +
                "address='" + address + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
