package com.terragoedge.streetlight.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.terragoedge.edgeserver.EdgeNotebook;
import com.terragoedge.edgeserver.FullEdgeNotebook;
import com.terragoedge.edgeserver.SlvData;
import com.terragoedge.edgeserver.SlvDataDub;
import com.terragoedge.streetlight.json.model.FailureFormDBmodel;
import com.terragoedge.streetlight.json.model.FailureReportModel;
import com.terragoedge.streetlight.logging.InstallMaintenanceLogModel;
import org.apache.log4j.Logger;

import com.terragoedge.streetlight.StreetlightDaoConnection;
import com.terragoedge.streetlight.logging.LoggingModel;
import org.springframework.util.StringUtils;

public class StreetlightDao extends UtilDao {

  //  static final Logger logger = Logger.getLogger(StreetlightDao.class);

    public StreetlightDao() {
        super();
        createStreetLightSyncTable();
    }


    private void executeStatement(String sql) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeStatement(preparedStatement);
        }
    }


    private long exceuteSql(String sql) {
        Statement statement = null;
        Connection connection = null;
        try {
            connection = StreetlightDaoConnection.getInstance().getConnection();
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                long res = resultSet.getLong(1);
                return res;
            }
        } catch (Exception e) {
    //        logger.error("Error in exceuteSequence", e);
        } finally {
            closeStatement(statement);
        }
        return -1;
    }

    private void createStreetLightSyncTable() {
        String query = "CREATE TABLE IF NOT EXISTS errorFormSyncdetails (notesyncid integer NOT NULL,"
                + " noteid text, status text, errordetails text, createddatetime bigint, notename text,modelJson text,processDateTime text,newNoteGuid text, polestatus text, CONSTRAINT errorFormSyncdetails_pkey PRIMARY KEY (notesyncid));";
        executeStatement(query);
    }

    public FailureReportModel getProcessedReportsByFixtureId(String fixtureId) {
        Statement queryStatement = null;
        ResultSet queryResponse = null;
        FailureReportModel failureReportModel = null;
        Gson gson = new Gson();
        try {
            String sql = "select * from errorFormSyncdetails where notename='" + fixtureId + "' order by processDateTime desc limit 1";
            System.out.println(sql);
            queryStatement = connection.createStatement();
            queryResponse = queryStatement.executeQuery(sql);
            if (queryResponse.next()) {
                String modelJson = queryResponse.getString("modelJson");
                if (modelJson != null && !modelJson.isEmpty()) {
                    failureReportModel = gson.fromJson(modelJson, FailureReportModel.class);
                }
            }
            return failureReportModel;
        } catch (Exception e) {
      //      logger.info("error while processedreportBy fixtureId", e);
        }
        return null;
    }

    public void getFailureModelList() {
        Statement queryStatement = null;
        ResultSet queryResponse = null;
        List<FailureFormDBmodel> failureFormDBmodels = new ArrayList<>();
        Gson gson = new Gson();
        try {
            String sql = "select * from errorFormSyncdetails";
            System.out.println(sql);
            queryStatement = connection.createStatement();
            queryResponse = queryStatement.executeQuery(sql);
            while (queryResponse.next()) {
                String noteName = queryResponse.getString("notename");
                System.out.println(noteName);
                String modelJson = queryResponse.getString("modelJson");
                if (modelJson != null && !modelJson.isEmpty()) {
                    String data = "FailureReportModel";
                    String insertJson = modelJson.replaceAll(data, "");
                    FailureReportModel failureReportModel = gson.fromJson(insertJson, FailureReportModel.class);
                    updateModelJson(noteName, gson.toJson(failureReportModel));
                }
            }
        } catch (Exception e) {
          //  logger.info("error while processedreportBy fixtureId", e);
        }
    }

    public void updateModelJson(String fixtureId, String modelJson) {
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            connection = StreetlightDaoConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(
                    "UPDATE errorFormSyncdetails SET modeljson = ? where notename = ?;");
            preparedStatement.setString(1, modelJson);
            preparedStatement.setString(2, fixtureId);

            preparedStatement.executeUpdate();
        } catch (Exception e) {
           // logger.error("Error in update", e);
        } finally {
            closeStatement(preparedStatement);
        }
    }

    public Set<String> getFixtureId() {
        Statement queryStatement = null;
        ResultSet queryResponse = null;
        Set<String> noteIds = new TreeSet<>();
        try {
            queryStatement = connection.createStatement();
            queryResponse = queryStatement.executeQuery("Select notename from errorformsyncdetails where polestatus != 'FIXED';");

            while (queryResponse.next()) {
                noteIds.add(queryResponse.getString("notename"));
            }

        } catch (Exception e) {
         //   logger.error("Error in getNoteIds", e);
        } finally {
            closeResultSet(queryResponse);
            closeStatement(queryStatement);
        }
        return noteIds;
    }

    public void insertErrorFormNotes(FailureFormDBmodel loggingModel) {
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            String sql = "SELECT max(notesyncid) + 1 from  errorFormSyncdetails";
            long id = exceuteSql(sql);
            if (id == -1 || id == 0) {
                id = 1;
            }
            connection = StreetlightDaoConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(
                    "INSERT INTO errorFormSyncdetails (notesyncid , noteid, status,errordetails,"
                            + "createddatetime, notename,modelJson,processDateTime,newNoteGuid,polestatus) values (?,?,?,?,?,?,?,?,?,?) ;");
            preparedStatement.setLong(1, id);
            preparedStatement.setString(2, loggingModel.getNoteid());
            preparedStatement.setString(3, loggingModel.getStatus());
            preparedStatement.setString(4, loggingModel.getErrorDetails());
            preparedStatement.setLong(5, Long.valueOf(loggingModel.getCreatedDatetime()));
            preparedStatement.setString(6, loggingModel.getNoteName());
            preparedStatement.setString(7, loggingModel.getModelJson());
            preparedStatement.setString(8, loggingModel.getProcessDateTime());
            preparedStatement.setString(9, loggingModel.getNewNoteGuid());
            preparedStatement.setString(10, loggingModel.getPoleStatus());
            preparedStatement.execute();
        } catch (Exception e) {
           // logger.error("Error in insert errorformdb", e);
        } finally {
            closeStatement(preparedStatement);
        }
    }


    public void update(FailureFormDBmodel loggingModel) {
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            connection = StreetlightDaoConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(
                    "UPDATE errorFormSyncdetails SET noteid = ?, status = ?,createddatetime = ?, notename = ?,processDateTime = ?,newNoteGuid = ?,polestatus = ? where notename = ?;");
            preparedStatement.setString(1, loggingModel.getNoteid());
            preparedStatement.setString(2, loggingModel.getStatus());
            preparedStatement.setLong(3, Long.valueOf(loggingModel.getCreatedDatetime()));
            preparedStatement.setString(4, loggingModel.getNoteName());
            preparedStatement.setString(5, loggingModel.getProcessDateTime());
            preparedStatement.setString(6, loggingModel.getNewNoteGuid());
            preparedStatement.setString(7, loggingModel.getPoleStatus());
            preparedStatement.setString(8, loggingModel.getNoteName());
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            //logger.error("Error in update", e);
        } finally {
            closeStatement(preparedStatement);
        }
    }


    public void deleteProcessedNotes(String processednoteid) {
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            connection = StreetlightDaoConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(
                    "DELETE FROM notesyncdetails WHERE  processednoteid = ? ;");
            preparedStatement.setString(1, processednoteid);
            preparedStatement.execute();
        } catch (Exception e) {
            //logger.error("Error in deleteProcessedNotes", e);
        } finally {
            closeStatement(preparedStatement);
        }
    }

    public void updateLicense(String expireDate) {
        Statement queryStatement = null;
        Connection connection = null;
        try {
            connection = StreetlightDaoConnection.getInstance().getConnection();
            String query="UPDATE license SET expirationdate ='"+expireDate+"';";
            queryStatement = connection.createStatement();
            queryStatement.execute(query);

        } catch (Exception e) {
            //logger.error("Error in update", e);
        } finally {
            closeStatement(queryStatement);
        }


    }
}
