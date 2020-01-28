package com.terrago.streetlights.dao;

import com.google.gson.Gson;
import com.terrago.streetlights.utils.FailureReportModel;
import com.terrago.streetlights.utils.OutageData;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TerragoDAO {
    public static void updateModifiedUserName(String noteGUID)
    {
        Connection conn = DataBaseConnector.getConnection();
        Statement statement = null;
        ResultSet resultSet = null;
        try{
            String queryString = "update edgenote set createdby='slvinterface' where noteguid='" + noteGUID + "'";
            statement = conn.createStatement();
            statement.execute(queryString);
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        DataBaseConnector.closeConnection();
    }
    public static List<OutageData> getAllOutageData(){
        List<OutageData> result = new ArrayList<>();
        Connection conn = DataBaseConnector.getConnection();
        Statement statement = null;
        ResultSet resultSet = null;
        Gson gson = new Gson();
        try{
            String queryString = "select fixtureid,outagejson from outagedata";
            statement = conn.createStatement();

            resultSet = statement.executeQuery(queryString);
            while(resultSet.next())
            {
                //result = resultSet.getString("noteguid");
                String fixtureID = resultSet.getString("fixtureid");
                String outageJSON = resultSet.getString("outagejson");
                FailureReportModel failureReportModel = gson.fromJson(outageJSON, FailureReportModel.class);
                OutageData outageData = new OutageData();
                outageData.setTitle(fixtureID);
                outageData.setFailureReportModel(failureReportModel);
                result.add(outageData);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        if(statement != null)
        {
            try {
                statement.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        if(resultSet != null)
        {
            try {
                resultSet.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }

        DataBaseConnector.closeConnection();
        return result;
    }
    public static String getNoteGUIDForTitle(String title)
    {
        String result = "";
        Connection conn = DataBaseConnector.getConnection();
        Statement statement = null;
        ResultSet resultSet = null;
        try{
            String queryString = "select noteguid from edgenote where iscurrent=true and isdeleted=false and title='" + title + "'";
            statement = conn.createStatement();

            resultSet = statement.executeQuery(queryString);
            while(resultSet.next())
            {
                result = resultSet.getString("noteguid");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        if(statement != null)
        {
            try {
                statement.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        if(resultSet != null)
        {
            try {
                resultSet.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }

        DataBaseConnector.closeConnection();
        return result;
    }
    public static long writeLastUpdateTime(long timeValue){
        long result = 0;
        Connection conn = DataBaseConnector.getConnection();
        Statement statement = null;

        try{
            statement = conn.createStatement();
            String queryString = "update tmp_florlights set lastmaxtime=" + timeValue + " where id=1";
            statement.execute(queryString);


        }
        catch(SQLException e){
            e.printStackTrace();
        }
        finally {

            if(statement != null)
            {
                try {
                    statement.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }


        DataBaseConnector.closeConnection();
        return result;
    }
    public static long readLastUpdatedTime(){
        long result = 0;
        Connection conn = DataBaseConnector.getConnection();
        Statement statement = null;
        ResultSet resultSet = null;
        try{
            statement = conn.createStatement();
            resultSet = statement.executeQuery("select lastmaxtime from tmp_florlights");
            while(resultSet.next())
            {
                result = resultSet.getLong("lastmaxtime");
            }

        }
        catch(SQLException e){
            e.printStackTrace();
        }
        finally {
            if(resultSet != null)
            {
                try {
                    resultSet.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
            if(statement != null)
            {
                try {
                    statement.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }


        DataBaseConnector.closeConnection();
        return result;
    }
    public static String getCurrentNoteGUID(String fixtureID)
    {
        Connection conn = DataBaseConnector.getConnection();
        Statement statement = null;
        ResultSet resultSet = null;
        String result="";
        try {
            statement = conn.createStatement();
            String query = "select title,noteguid from edgenote where iscurrent=true and isdeleted=false and title='" + fixtureID + "'";
            System.out.println(query);
            resultSet = statement.executeQuery(query);
            while(resultSet.next())
            {
                String noteguid = resultSet.getString("noteguid");
                result = noteguid;
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
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
        }
        DataBaseConnector.closeConnection();
        return result;
    }
}
