package com.terragoedge.streetlight.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.JsonObject;
import com.terragoedge.edgeserver.EdgeNotebook;
import com.terragoedge.edgeserver.FullEdgeNotebook;
import com.terragoedge.edgeserver.SlvData;
import com.terragoedge.edgeserver.SlvDataDub;
import com.terragoedge.streetlight.json.model.FailureFormDBmodel;
import com.terragoedge.streetlight.logging.InstallMaintenanceLogModel;
import org.apache.log4j.Logger;

import com.terragoedge.streetlight.StreetlightDaoConnection;
import com.terragoedge.streetlight.logging.LoggingModel;
import org.springframework.util.StringUtils;

public class StreetlightDao extends UtilDao {

    static final Logger logger = Logger.getLogger(StreetlightDao.class);

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
            logger.error("Error in exceuteSequence", e);
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
    public List<String> getFixtureId() {
        Statement queryStatement = null;
        ResultSet queryResponse = null;
        List<String> noteIds = new ArrayList<>();
        try {
            queryStatement = connection.createStatement();
            queryResponse = queryStatement.executeQuery("Select notename from errorformsyncdetails where polestatus != 'FIXED';");

            while (queryResponse.next()) {
                noteIds.add(queryResponse.getString("notename"));
            }

        } catch (Exception e) {
            logger.error("Error in getNoteIds", e);
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
            logger.error("Error in insert errorformdb", e);
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
            preparedStatement.setString(3, loggingModel.getCreatedDatetime());
            preparedStatement.setString(4, loggingModel.getNoteName());
            preparedStatement.setString(5, loggingModel.getProcessDateTime());
            preparedStatement.setString(6, loggingModel.getNewNoteGuid());
            preparedStatement.setString(7, loggingModel.getPoleStatus());
            preparedStatement.setString(8, loggingModel.getNoteName());
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            logger.error("Error in update", e);
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
            logger.error("Error in deleteProcessedNotes", e);
        } finally {
            closeStatement(preparedStatement);
        }
    }


}
