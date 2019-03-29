package com.terragoedge.streetlight.json.model;
public class Info {

    public String user;
    public String comment;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "Info{" +
                "user='" + user + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}