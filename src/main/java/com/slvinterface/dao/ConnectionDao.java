package com.slvinterface.dao;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

public enum ConnectionDao {
    INSTANCE;
    private final static String DATABASE_URL = "jdbc:postgresql://127.0.0.1:5432/terragoedge?user=postgres&password=password";

    ConnectionSource connectionSource = null;


    ConnectionDao() {

        try {
            connectionSource = new JdbcConnectionSource(DATABASE_URL);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
