package com.terrago.streetlights.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.terrago.streetlights.dao.DataBaseConnector;
import com.terrago.streetlights.dao.TerragoDAO;
import com.terrago.streetlights.dao.model.UbiTransactionLog;
import com.terrago.streetlights.utils.LastUpdated;
import com.terrago.streetlights.utils.PropertiesReader;
import com.terragoedge.edgeserver.EdgeFormData;
import com.terragoedge.edgeserver.EdgeNote;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MonitorChanges2 {
    private List<LastUpdated> getUpdatedNotes(String lastProcessedTime)
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

    private String getDevicControlStatus(List<EdgeFormData> formComponents)
    {
        //ubicquia_replacedeveui
        int idReplacedevui = Integer.parseInt(PropertiesReader.getProperties().getProperty("ubicquia_devicecontrol"));
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
    private String getComponentValue(List<EdgeFormData> formComponents, String propertyName)
    {
        int idReplacedevui = Integer.parseInt(PropertiesReader.getProperties().getProperty(propertyName));
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
    public void startMonitoring(){
        String nnguid = "";
        do {
            ////////////////////////////////////////////////////////
            long lastMaxUpdatedTime = TerragoDAO.readLastUpdatedTime2();
            if(lastMaxUpdatedTime == 0)
            {
                lastMaxUpdatedTime = System.currentTimeMillis() - 300000;
            }
            System.out.println("Looking for Changes ...");
            List<LastUpdated> lstUpdated = getUpdatedNotes(Long.toString(lastMaxUpdatedTime));
            for (LastUpdated lstCur : lstUpdated) {
                UbiTransactionLog ubiTransactionLog = new UbiTransactionLog();
                ubiTransactionLog.setNotegui(lstCur.getNoteguid());
                ubiTransactionLog.setTitle(lstCur.getTitle());
                ubiTransactionLog.setSynctime(lstCur.getSynctime());

                System.out.println("Processing Changes ...");
                lastMaxUpdatedTime = Math.max(lastMaxUpdatedTime, lstCur.getSynctime());
                nnguid = lstCur.getNoteguid();
                String notesJson = RESTService.getNoteDetails(nnguid);
                Type listType = new TypeToken<ArrayList<EdgeNote>>() {
                }.getType();
                Gson gson = new Gson();
                List<EdgeNote> edgeNoteList = new ArrayList<>();
                //    List<EdgeNote> edgeNoteList = gson.fromJson(notesJson, listType);
                EdgeNote restEdgeNote = gson.fromJson(notesJson, EdgeNote.class);
                JsonObject edgenoteJson = new JsonParser().parse(notesJson).getAsJsonObject();
                JsonArray serverForms = edgenoteJson.get("formData").getAsJsonArray();
                int size = serverForms.size();
                String ignoreUser = PropertiesReader.getProperties().getProperty("ignoreuser");
                boolean ispresent = false;
                boolean isDCPresent = false;
                boolean isInvalidUser = false;
                for (int i = 0; i < size; i++) {
                    JsonObject serverEdgeForm = serverForms.get(i).getAsJsonObject();
                    String formDefJson = serverEdgeForm.get("formDef").getAsString();
                    String formTemplate = serverEdgeForm.get("formTemplateGuid").getAsString();
                    List<EdgeFormData> formComponents = null;

                    if (formTemplate.equals(PropertiesReader.getProperties().getProperty("formtemplatetoprocess"))) {
                        ispresent = true;
                    }
                    if(restEdgeNote.getCreatedBy().equals(ignoreUser))
                    {
                        isInvalidUser = true;
                    }
                }
                if(ispresent)
                {
                    if(isInvalidUser)
                    {
                        ubiTransactionLog.setAction("Cannot process this user");
                    }
                    else
                    {
                        for (int i = 0; i < size; i++) {
                            JsonObject serverEdgeForm = serverForms.get(i).getAsJsonObject();
                            String formDefJson = serverEdgeForm.get("formDef").getAsString();
                            String formTemplate = serverEdgeForm.get("formTemplateGuid").getAsString();
                            List<EdgeFormData> formComponents = null;

                            if (formTemplate.equals(PropertiesReader.getProperties().getProperty("formtemplatetoprocess"))) {
                                formComponents = gson.fromJson(formDefJson, new TypeToken<List<EdgeFormData>>() {
                                }.getType());
                                String dcStatus = getDevicControlStatus(formComponents);
                                String strSingleDC = getComponentValue(formComponents,"ubicquia_sdc");
                                String strMDC = getComponentValue(formComponents,"ubicquia_mdc");

                                if (strSingleDC == null)
                                {
                                    strSingleDC = "";
                                }
                                if (strMDC == null)
                                {
                                    strMDC = "";
                                }

                                if (!strSingleDC.equals("Yes") && !strMDC.equals("Yes"))
                                {
                                    DoLocationUpdate doLocationUpdate = new DoLocationUpdate(lstCur);
                                    doLocationUpdate.setNoteguid(lstCur.getNoteguid());
                                    doLocationUpdate.processLocationChange(ubiTransactionLog);
                                }
                            }
                        }
                    }
                }
                else
                {
                    ubiTransactionLog.setAction("Not valid formtemplate");
                }
                ubiTransactionLog.setEventtime(System.currentTimeMillis());
                TerragoDAO.addUbiTransactionLog2(ubiTransactionLog);
            }

            if (lstUpdated.size() > 0) {
                /*long lntime = TerragoDAO.readLastUpdatedTime2();
                if(lntime >= lastMaxUpdatedTime)
                {
                    lastMaxUpdatedTime = lntime;
                }
                TerragoDAO.writeLastUpdateTime2(lastMaxUpdatedTime);*/
            }
            ////////////////////////////////////////////////////////
            try{
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }while(true);
    }
}
