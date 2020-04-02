package com.terragoedge.slvinterface.dao;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.terragoedge.slvinterface.model.OutageData;

public enum OutageDAO {
    INSTANCE;
    private final static String DATABASE_URL = "jdbc:postgresql://127.0.0.1:5432/terragoedge?user=postgres&password=password";

    ConnectionSource connectionSource = null;


    OutageDAO() {

        try {
            connectionSource = new JdbcConnectionSource(DATABASE_URL);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ConnectionSource getConnectionSource() {
        return connectionSource;
    }
}
