package com.terrago.streetlights.dao;

import java.sql.Connection;
import java.sql.DriverManager;

public class DataBaseConnector {
    private static Connection conn = null;

    private static void establishConnection(){
        try {
            conn = DriverManager.getConnection(
                    "jdbc:postgresql://127.0.0.1:5432/terragoedge", "postgres", "password");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public static Connection getConnection(){
        if(conn == null)
        {
            establishConnection();
        }

        return conn;
    }
    public static void closeConnection(){
        /*if(conn != null)
        {

            try{
                conn.close();
                conn = null;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }*/
    }
}
