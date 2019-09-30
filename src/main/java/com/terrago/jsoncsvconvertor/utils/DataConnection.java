package com.terrago.jsoncsvconvertor.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataConnection {
    private static Connection connection;
    public static Connection getConnetion(){
        if(connection == null)
        {
            try {
                connection = DriverManager.getConnection(
                        "jdbc:postgresql://127.0.0.1:5432/terragoedge", "postgres", "password");
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return connection;
    }
}
