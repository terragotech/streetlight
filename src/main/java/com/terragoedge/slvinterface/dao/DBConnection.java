package com.terragoedge.slvinterface.dao;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

public class DBConnection {
    public static ConnectionSource connectionSource = null;
    private final static String DATABASE_URL = "jdbc:postgresql://127.0.0.1:5432/terragoedge?user=postgres&password=password";

    public static ConnectionSource getInstance(){
        try {
            if(connectionSource==null) {
                connectionSource = new JdbcConnectionSource(DATABASE_URL);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return connectionSource;
    }
}