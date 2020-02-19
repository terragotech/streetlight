package com.terrago.streetlights.dao;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.terrago.streetlights.service.RESTService;
import com.terrago.streetlights.utils.FailureReportModel;
import com.terrago.streetlights.utils.OutageData;
import com.terrago.streetlights.utils.PropertiesReader;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.streetlight.json.model.NoteInfo;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TerragoDAO {
    private static String getReplaceDevui(List<EdgeFormData> formComponents)
    {
        //ubicquia_replacedeveui
        int idReplacedevui = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_replacedeveui"));
        String strReplacedevui = "";
        EdgeFormData repdevui = new EdgeFormData();
        repdevui.setId(idReplacedevui);
        int pos = formComponents.indexOf(repdevui);
        if(pos != -1){
            EdgeFormData tmp1 = formComponents.get(pos);
            strReplacedevui = tmp1.getValue();
        }
        return strReplacedevui;
    }
    public  static NoteInfo getIMEI(String noteTitle){
        NoteInfo noteInfo = new NoteInfo();
        noteInfo.setNoteguid("");
        noteInfo.setIMEI("");

        String result = "";
        String nguid = getNoteGUID(noteTitle);
        noteInfo.setNoteguid(nguid);
        if(!nguid.equals(""))
        {
            String notesJson =  RESTService.getNoteDetails(nguid);
            Type listType = new TypeToken<ArrayList<EdgeNote>>() {
            }.getType();
            Gson gson = new Gson();
            List<EdgeNote> edgeNoteList = new ArrayList<>();
            //    List<EdgeNote> edgeNoteList = gson.fromJson(notesJson, listType);
            EdgeNote restEdgeNote = gson.fromJson(notesJson, EdgeNote.class);
            JsonObject edgenoteJson = new JsonParser().parse(notesJson).getAsJsonObject();
            JsonArray serverForms = edgenoteJson.get("formData").getAsJsonArray();
            int size = serverForms.size();
            for (int i = 0; i < size; i++) {
                JsonObject serverEdgeForm = serverForms.get(i).getAsJsonObject();
                String formDefJson = serverEdgeForm.get("formDef").getAsString();
                String formTemplate = serverEdgeForm.get("formTemplateGuid").getAsString();
                //formDefJson = formDefJson.replaceAll("\\\\", "");
                //formDefJson = formDefJson.replace("u0026","\\u0026");
                List<EdgeFormData> formComponents = gson.fromJson(formDefJson, new TypeToken<List<EdgeFormData>>() {
                }.getType());
                if (formTemplate.equals(PropertiesReader.getProperties().getProperty("formtemplatetoprocess"))) {
                    EdgeFormData cur = new EdgeFormData();
                    String idDev = PropertiesReader.getProperties().getProperty("ubicquia_deveui");
                    int nidDev = Integer.parseInt(idDev);
                    cur.setId(nidDev);
                    int pos = formComponents.indexOf(cur);
                    if (pos != -1) {
                        EdgeFormData f1 = formComponents.get(pos);
                        String dev_eui = f1.getValue();
                        String repEUI = getReplaceDevui(formComponents);
                        if(repEUI != null)
                        {
                            if(repEUI.equals(""))
                            {
                                dev_eui = repEUI;
                            }
                        }
                        if (dev_eui == null) {
                            dev_eui = "";
                            result = "";
                        }
                        if (!dev_eui.equals("")) {
                            result = dev_eui;
                            noteInfo.setIMEI(result);
                        }
                    }
                }
            }
        }
        return  noteInfo;
    }
    public static String getNoteGUID(String noteTitle)
    {
        Connection conn = DataBaseConnector.getConnection();
        Statement statement = null;
        ResultSet resultSet = null;
        String noteGUID = "";
        try{
            String queryString = "select noteguid from edgenote where iscurrent=true and isdeleted=false and title='" + noteTitle + "'";
            statement = conn.createStatement();
            resultSet = statement.executeQuery(queryString);
            while(resultSet.next())
            {
                noteGUID = resultSet.getString("noteguid");
            }

        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        finally {
            if(statement != null)
            {
                try {
                    statement.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            if(resultSet != null)
            {
                try {
                    resultSet.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        DataBaseConnector.closeConnection();
        return noteGUID;
    }
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
