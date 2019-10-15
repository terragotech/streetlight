package com.terragoedge.slvinterface.dao;

import com.terragoedge.slvinterface.utils.PropertiesReader;
import org.apache.log4j.Logger;

import java.sql.*;

public class CsvConnectionDao {
    private static Logger logger = Logger.getLogger(CsvConnectionDao.class);
    public static void importCsv(String query){
        Statement statement = null;
        Connection connection = null;
        ResultSet resultSet = null;
        if (connection == null) {
            try {
                String host = PropertiesReader.getProperties().getProperty("com.slv.database.base.url");
                String username = PropertiesReader.getProperties().getProperty("com.slv.database.username");
                String password = PropertiesReader.getProperties().getProperty("com.slv.database.password");
                connection = DriverManager.getConnection(host, username, password);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        try{
            System.out.println(query);
            logger.info(query);
            statement = connection.createStatement();
            statement.execute(query);
        }catch (Exception e){
            logger.info("Error: "+e);
            e.printStackTrace();
        }finally {
            if(statement != null){
                try {
                    statement.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            if(resultSet != null){
                try{
                    resultSet.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            if(connection != null){
                try {
                    connection.close();
                    connection = null;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

        }

    }
}
