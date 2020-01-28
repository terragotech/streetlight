package com.terrago.streetlights.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseConnectionUtils {
    private static Connection connection = null;
    private static Connection establishConnection(){
        try {
            connection = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/terragoedge", "postgres", "password");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return  connection;
    }
    public static Connection  getConnection(){
        if(connection == null)
        {
            connection = establishConnection();
        }
        return connection;
    }
    public static void closeConnection(){
        try{
            connection.close();
            connection = null;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

}
