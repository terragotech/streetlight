package com.terragoedge.streetlight.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.google.gson.JsonObject;
import com.terragoedge.edgeserver.EdgeNotebook;
import com.terragoedge.edgeserver.FullEdgeNotebook;
import com.terragoedge.edgeserver.SlvData;
import com.terragoedge.edgeserver.SlvDataDub;
import com.terragoedge.streetlight.logging.InstallMaintenanceLogModel;
import org.apache.log4j.Logger;

import com.terragoedge.streetlight.StreetlightDaoConnection;
import com.terragoedge.streetlight.logging.LoggingModel;
import org.springframework.util.StringUtils;

public class StreetlightDao extends UtilDao {

    static final Logger logger = Logger.getLogger(StreetlightDao.class);

    public StreetlightDao() {
        super();
    }


    public List<SlvData> getNoteDetails(String title) {
        Statement queryStatement = null;
        ResultSet queryResponse = null;
        List<SlvData> slvDataList = new ArrayList<>();
        try {
            queryStatement = connection.createStatement();
            queryResponse = queryStatement.executeQuery("Select noteguid, title from edgenote where iscurrent = true and isdeleted = false and title = '" + title + "';");

            while (queryResponse.next()) {
                SlvData slvData = new SlvData();
                slvData.setTitle(queryResponse.getString("title"));
                slvData.setGuid(queryResponse.getString("noteguid"));
                slvDataList.add(slvData);
            }

        } catch (Exception e) {
            logger.error("Error in getNotebookGuid", e);
        } finally {
            closeResultSet(queryResponse);
            closeStatement(queryStatement);
        }
        return slvDataList;
    }

    public void updateNoteDetails(long createddatetime, String createdby, String noteGuid) {
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            connection = StreetlightDaoConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(
                    "UPDATE edgenote SET createddatetime = ?,createdby=? where noteguid =?;");
            preparedStatement.setLong(1, createddatetime);
            preparedStatement.setString(2, createdby);
            preparedStatement.setString(3, noteGuid);
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            logger.error("Error in update", e);
        } finally {
            closeStatement(preparedStatement);
        }
    }


}
