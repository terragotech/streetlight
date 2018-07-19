package com.terragoedge.slvinterface.dao;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

public enum ConnectionDAO {

    INSTANCE;

    private final static String DATABASE_URL = "jdbc:h2:mem:account";

    ConnectionSource connectionSource = null;

    ConnectionDAO(){

        try{
            connectionSource = new JdbcConnectionSource(DATABASE_URL);
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    public void setupDatabase(){

    }


    public ConnectionSource getConnection() {
        return connectionSource;
    }
}
