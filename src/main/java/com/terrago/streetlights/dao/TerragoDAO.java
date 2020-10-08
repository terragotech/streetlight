package com.terrago.streetlights.dao;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.terrago.streetlights.dao.model.DeviceData;
import com.terrago.streetlights.dao.model.UbiInterfaceLog;
import com.terrago.streetlights.dao.model.UbiTransactionLog;
import com.terrago.streetlights.service.RESTService;
import com.terrago.streetlights.utils.FailureReportModel;
import com.terrago.streetlights.utils.LastUpdated;
import com.terrago.streetlights.utils.OutageData;
import com.terrago.streetlights.utils.PropertiesReader;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;
import com.terragoedge.streetlight.json.model.NoteInfo;

import java.lang.reflect.Type;
import java.sql.*;
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
                            if(!repEUI.equals(""))
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
                        else
                        {
                            noteInfo.setIMEI(result);
                        }
                    }
                }
            }
        }
        return  noteInfo;
    }
    public static List<String> getNearByFixtures(String latitude,String longitude, String distance)
    {
        String query = "SELECT noteguid FROM edgenote WHERE iscurrent=true and isdeleted=false and " +
                "ST_GeometryType(geometry::geometry)='ST_Point' and ST_Distance_Sphere(geometry::geometry, ST_MakePoint("
                + longitude + "," + latitude + ")) <= " + distance;
        Connection conn = DataBaseConnector.getConnection();
        Statement statement = null;
        ResultSet resultSet = null;
        String noteGUID = "";
        List<String> lstNoteGUID = new ArrayList<String>();
        try {
            statement = conn.createStatement();
            resultSet = statement.executeQuery(query);
            while(resultSet.next())
            {
                noteGUID = resultSet.getString("noteguid");
                lstNoteGUID.add(noteGUID);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return lstNoteGUID;
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
    public static long updateUser(String noteguid, String usrname){
        long result = 0;
        Connection conn = DataBaseConnector.getConnection();
        Statement statement = null;

        try{
            statement = conn.createStatement();
            String queryString = "update edgenote set createdby='" + usrname + "' where noteguid='" + noteguid + "'";
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
    public static long writeLastUpdateTime2(long timeValue){
        long result = 0;
        Connection conn = DataBaseConnector.getConnection();
        Statement statement = null;

        try{
            statement = conn.createStatement();
            String queryString = "update tmp_florlights2 set lastmaxtime=" + timeValue + " where id=1";
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
            resultSet = statement.executeQuery("select max(lastmaxtime) as utime from tmp_florlights");
            while(resultSet.next())
            {
                result = resultSet.getLong("utime");
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
    public static long readLastUpdatedTime2(){
        long result = 0;
        Connection conn = DataBaseConnector.getConnection();
        Statement statement = null;
        ResultSet resultSet = null;
        try{
            statement = conn.createStatement();
            resultSet = statement.executeQuery("select max(synctime) as utime from ubitransactionlog2");
            while(resultSet.next())
            {
                result = resultSet.getLong("utime");
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
    public static String isCurrentNote(String noteGUID)
    {
        Connection conn = DataBaseConnector.getConnection();
        Statement statement = null;
        ResultSet resultSet = null;
        String result="";
        try {
            statement = conn.createStatement();
            String query = "select title,noteguid from edgenote where iscurrent=true and isdeleted=false and noteguid='" +
                    noteGUID + "'";
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
    public static String getCurrentNoteFromParentGUID(String parentGUID)
    {
        Connection conn = DataBaseConnector.getConnection();
        Statement statement = null;
        ResultSet resultSet = null;
        String result="";
        try {
            statement = conn.createStatement();
            String query = "select title,noteguid from edgenote where iscurrent=true and isdeleted=false and parentnoteid='" +
                    parentGUID + "'";
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
    public static LastUpdated getNoteInfo(String noteguid){
        LastUpdated lastUpdated = null;

        Connection conn = DataBaseConnector.getConnection();
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            statement = conn.createStatement();
            resultSet = statement.executeQuery("select title,noteguid,synctime,parentnoteid from edgenote where noteguid='" + noteguid + "'" );
            while(resultSet.next())
            {
                String notetitle = resultSet.getString("title");
                Long syncTime = resultSet.getLong("synctime");
                String strParentguid = resultSet.getString("parentnoteid");
                lastUpdated = new LastUpdated();
                lastUpdated.setNoteguid(noteguid);
                lastUpdated.setTitle(notetitle);
                lastUpdated.setSynctime(syncTime);
                lastUpdated.setParentnoteguid(strParentguid);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            DataBaseConnector.closeConnection();
        }
        return lastUpdated;
    }
    public static List<String> getUpdatedTitles(String lastProcessedTime)
    {
        List<String> lstString = new ArrayList<String>();
        Connection conn = DataBaseConnector.getConnection();
        Statement statement = null;
        ResultSet resultSet = null;
        //logger.info("Checking for updates");
        try {
            statement = conn.createStatement();
            resultSet = statement.executeQuery("select title,noteguid,createddatetime from edgenote where iscurrent=true and isdeleted=false and createddatetime > " + lastProcessedTime );
            while(resultSet.next())
            {
                String notetitle = resultSet.getString("title");
                lstString.add(notetitle);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
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
            DataBaseConnector.closeConnection();
        }
        return lstString;
    }
    public static void addUbiTransactionLog(UbiTransactionLog ubiTransactionLog)
    {
        PreparedStatement preparedStatement = null;
        String SQL = "INSERT INTO ubitransactionlog(notegui,title,action,deviceStatus,devui,synctime,eventtime) "
                + "VALUES(?,?,?,?,?,?,?)";
        try {
            Connection conn = DataBaseConnector.getConnection();
            preparedStatement = conn.prepareStatement(SQL);
            preparedStatement.setString(1,ubiTransactionLog.getNotegui());
            preparedStatement.setString(2,ubiTransactionLog.getTitle());
            preparedStatement.setString(3,ubiTransactionLog.getAction());
            preparedStatement.setString(4,ubiTransactionLog.getDeviceStatus());
            preparedStatement.setString(5,ubiTransactionLog.getDevui());
            preparedStatement.setLong(6,ubiTransactionLog.getSynctime());
            preparedStatement.setLong(7,ubiTransactionLog.getEventtime());
            preparedStatement.executeUpdate();

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(preparedStatement != null)
            {
                try {
                    preparedStatement.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void addUbiTransactionLog2(UbiTransactionLog ubiTransactionLog)
    {
        PreparedStatement preparedStatement = null;
        String SQL = "INSERT INTO ubitransactionlog2(notegui,title,action,deviceStatus,devui,synctime,eventtime) "
                + "VALUES(?,?,?,?,?,?,?)";
        try {
            Connection conn = DataBaseConnector.getConnection();
            preparedStatement = conn.prepareStatement(SQL);
            preparedStatement.setString(1,ubiTransactionLog.getNotegui());
            preparedStatement.setString(2,ubiTransactionLog.getTitle());
            preparedStatement.setString(3,ubiTransactionLog.getAction());
            preparedStatement.setString(4,ubiTransactionLog.getDeviceStatus());
            preparedStatement.setString(5,ubiTransactionLog.getDevui());
            preparedStatement.setLong(6,ubiTransactionLog.getSynctime());
            preparedStatement.setLong(7,ubiTransactionLog.getEventtime());
            preparedStatement.executeUpdate();

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(preparedStatement != null)
            {
                try {
                    preparedStatement.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void addUbiInterfaceLog(UbiInterfaceLog ubiInterfaceLog)
    {
        PreparedStatement preparedStatement = null;
        String SQL = "INSERT INTO ubiinterfacelog(notegui,title,urlrequest,requestBody,requestResponse,eventtime) "
                + "VALUES(?,?,?,?,?,?)";
        try {
            Connection conn = DataBaseConnector.getConnection();
            preparedStatement = conn.prepareStatement(SQL);
            preparedStatement.setString(1,ubiInterfaceLog.getNotegui());
            preparedStatement.setString(2,ubiInterfaceLog.getTitle());
            preparedStatement.setString(3,ubiInterfaceLog.getUrlrequest());
            preparedStatement.setString(4,ubiInterfaceLog.getRequestBody());
            preparedStatement.setString(5,ubiInterfaceLog.getRequestResponse());
            preparedStatement.setLong(6,ubiInterfaceLog.getEventtime());
            preparedStatement.executeUpdate();

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(preparedStatement != null)
            {
                try {
                    preparedStatement.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
    public static LastUpdated getUpdateInfo(String noteguid)
    {
        LastUpdated lastUpdated = null;
        Connection conn = DataBaseConnector.getConnection();
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = conn.createStatement();
            resultSet = statement.executeQuery(
                    "select title,noteguid,synctime,parentnoteid from edgenote where noteguid='" + noteguid + "'");
            while(resultSet.next())
            {
                lastUpdated = new LastUpdated();
                lastUpdated.setNoteguid(noteguid);
                lastUpdated.setSynctime(resultSet.getLong("synctime"));
                lastUpdated.setTitle(resultSet.getString("title"));
                lastUpdated.setParentnoteguid(resultSet.getString("parentnoteid"));

            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
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
            DataBaseConnector.closeConnection();
        }
        return  lastUpdated;
    }
    public List<LastUpdated> getUpdatedNotes(String lastProcessedTime)
    {
        List<LastUpdated> lstString = new ArrayList<LastUpdated>();
        Connection conn = DataBaseConnector.getConnection();
        Statement statement = null;
        ResultSet resultSet = null;
        //logger.info("Checking for updates");
        try {
            statement = conn.createStatement();
            resultSet = statement.executeQuery("select title,noteguid,synctime from edgenote where iscurrent=true and isdeleted=false and synctime > " + lastProcessedTime + " order by synctime ");
            while(resultSet.next())
            {
                String noteguid = resultSet.getString("noteguid");
                LastUpdated lastUpdated = new LastUpdated();
                lastUpdated.setNoteguid(noteguid);
                lastUpdated.setSynctime(resultSet.getLong("synctime"));
                lastUpdated.setTitle(resultSet.getString("title"));
                lstString.add(lastUpdated);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
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
            DataBaseConnector.closeConnection();
        }
        return lstString;
    }
    public static List<DeviceData> getAllDeviceData(){
        Connection conn = DataBaseConnector.getConnection();
        Statement statement = null;
        ResultSet resultSet = null;
        List<DeviceData> result = new ArrayList<DeviceData>();
        try {
            statement = conn.createStatement();
            resultSet = statement.executeQuery("select parentnoteguid,dev_eui,status from " +
                    "pendingdevice where status='NOT_FOUND'");
            while(resultSet.next())
            {

                String strParentNoteGUID = resultSet.getString("parentnoteguid");
                String strDev_UI = resultSet.getString("dev_eui");
                String strStatus = resultSet.getString("status");
                DeviceData deviceData = new DeviceData();
                deviceData.setParentnoteguid(strParentNoteGUID);
                deviceData.setDev_eui(strDev_UI);
                deviceData.setStatus(strStatus);
                result.add(deviceData);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
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
            DataBaseConnector.closeConnection();
        }
        return result;
    }
    public static void addDeviceData(DeviceData deviceData){
        PreparedStatement preparedStatement = null;
        String SQL = "INSERT INTO pendingdevice(parentnoteguid,dev_eui,status) "
                + "VALUES(?,?,?)";
        try {
            Connection conn = DataBaseConnector.getConnection();
            preparedStatement = conn.prepareStatement(SQL);
            preparedStatement.setString(1,deviceData.getParentnoteguid());
            preparedStatement.setString(2,deviceData.getDev_eui());
            preparedStatement.setString(3,deviceData.getStatus());

            preparedStatement.executeUpdate();

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(preparedStatement != null)
            {
                try {
                    preparedStatement.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void updateDeviceData(DeviceData deviceData)
    {
        Connection conn = DataBaseConnector.getConnection();
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            statement = conn.createStatement();
            statement.executeUpdate("update pendingdevice set status='PROCESSED' where " +
                    "parentnoteguid=" + "'" + deviceData.getParentnoteguid() + "'");


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally {
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
            DataBaseConnector.closeConnection();
        }
    }
}
