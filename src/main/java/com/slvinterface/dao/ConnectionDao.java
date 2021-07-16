package com.slvinterface.dao;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.slvinterface.utils.PropertiesReader;

public enum ConnectionDao {
    INSTANCE;
    //private final static String DATABASE_URL = "jdbc:postgresql://127.0.0.1:5432/terragoedge?user=postgres&password=password";

    ConnectionSource connectionSource = null;


    ConnectionDao() {
        String DATABASE_URL = PropertiesReader.getProperties().getProperty("db.url");
        if(DATABASE_URL == null){
            DATABASE_URL = "jdbc:postgresql://127.0.0.1:5432/terragoedge?user=postgres&password=password";
        }
        try {
            connectionSource = new JdbcConnectionSource(DATABASE_URL);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
