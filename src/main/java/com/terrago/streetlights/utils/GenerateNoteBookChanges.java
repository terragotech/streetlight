package com.terrago.streetlights.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class GenerateNoteBookChanges {
    private String getNoteBookGUID(String noteBookName)
    {
        String notebookGUID = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try{
            Connection connection = DataBaseConnectionUtils.getConnection();
            statement = connection.createStatement();
            String sqlQuery = "select notebookguid from edgenotebook where isdeleted=false and notebookname=" + "'" + noteBookName + "'";
            resultSet = statement.executeQuery(sqlQuery);
            while(resultSet.next())
            {
                notebookGUID = resultSet.getString("notebookguid");
            }

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally {
            if(statement != null)
            {
                try{
                    statement.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
            if(resultSet != null)
            {
                try{
                    resultSet.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return notebookGUID;
    }
    private String getNoteGUID(String noteTitle){
        String noteGUID = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try{
            Connection connection = DataBaseConnectionUtils.getConnection();
            statement = connection.createStatement();
            String sqlQuery = "select noteguid from edgenote where iscurrent=true and isdeleted=false and title=" + "'" + noteTitle + "'";
            resultSet = statement.executeQuery(sqlQuery);
            while(resultSet.next())
            {
                noteGUID = resultSet.getString("noteguid");
            }

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally {
            if(statement != null)
            {
                try{
                    statement.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
            if(resultSet != null)
            {
                try{
                    resultSet.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return noteGUID;
    }
    private String getData(String strData)
    {
        if(strData == null)
        {
            return "";
        }
        return strData;
    }
    public void processInputFile(String pathToCsv,String writePath){
        DataBaseConnectionUtils.getConnection();
        BufferedReader csvReader = null;
        BufferedWriter csvWriter = null;

        try {
            csvReader = new BufferedReader(new FileReader(pathToCsv));
            csvWriter = new BufferedWriter(new FileWriter(writePath));
            String row = null;
            while ((row = csvReader.readLine()) != null) {
                String []columns = row.split(",");
                if(columns.length > 1)
                {
                    String strTitle = columns[0];
                    String strBookName = columns[1];
                    String noteguid = getNoteGUID(strTitle);
                    String notebookguid = getNoteBookGUID(strBookName);
                    String outputData = getData(noteguid) + "," + getData(notebookguid) + "\r\n";
                    csvWriter.write(outputData);
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if(csvReader != null)
            {
                try {
                    csvReader.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            if(csvWriter != null)
            {
                try {
                    csvWriter.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            DataBaseConnectionUtils.closeConnection();
        }
    }
}
