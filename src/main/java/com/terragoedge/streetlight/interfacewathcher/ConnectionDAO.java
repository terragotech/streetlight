package com.terragoedge.streetlight.interfacewathcher;

import com.terragoedge.streetlight.PropertiesReader;
import org.apache.log4j.Logger;

import java.sql.*;

public enum ConnectionDAO {

    INSTANCE;

    private static final Logger logger = Logger.getLogger(ConnectionDAO.class);

    private static Connection connection;

    ConnectionDAO() {
        openConnection();
    }

    private void openConnection() {
        try {
            String dbUrl = PropertiesReader.getProperties().getProperty("edge.db.url");
            //String host = "jdbc:postgresql://localhost:5432/terragoedge";
            //String host = "jdbc:postgresql://localhost:5432/terragoedge_staging";
            String username = "postgres";
            String password = "password";
            connection = DriverManager.getConnection(dbUrl, username, password);
        } catch (SQLException ex) {
            logger.error("Error in openConnection",ex);
        }
    }


    public Long getSlvInterfaceLastRunTime() {
        Statement statement = null;
        Connection connection = null;
        ResultSet resultSet = null;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery("select eventtime from slvinterfacestatus");
            while (resultSet.next()) {
                Long res = resultSet.getLong(1);
                return res;
            }
        } catch (Exception e) {
            logger.error("Error in exceuteSequence", e);
        } finally {
closeResultSet(resultSet);
closeStatement(statement);
        }
        return null;
    }


    private void closeResultSet(ResultSet resultSet){
        try{
            if(resultSet != null){
                resultSet.close();
            }
        }catch (Exception e){

        }
    }


    private void closeStatement(Statement preparedStatement){
        try {
            if(preparedStatement != null){
                preparedStatement.close();
            }
        }catch (Exception e){

        }
    }
}
