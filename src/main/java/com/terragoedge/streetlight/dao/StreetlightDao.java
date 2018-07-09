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

        String sql = "CREATE TABLE IF NOT EXISTS notesyncdetails (streetlightsyncid integer NOT NULL,"
                + " processednoteid text, status text, errordetails text,singleformerrordetails text, singleformstatus text,createddatetime bigint, notename text,existingnodemacaddress text,newnodemacaddress text,isReplaceNode text,isQuickNote text,idOnController text,macAddress text, CONSTRAINT notesyncdetails_pkey PRIMARY KEY (streetlightsyncid));";
        executeStatement(sql);
        String query = "CREATE TABLE IF NOT EXISTS errorFormSyncdetails (notesyncid integer NOT NULL,"
                + " noteid text, status text, errordetails text, createddatetime bigint, notename text,modelJson text,processDateTime text,newNoteGuid text, CONSTRAINT errorFormSyncdetails_pkey PRIMARY KEY (notesyncid));";
        executeStatement(query);
    }
    public List<String> getFixtureId() {
        Statement queryStatement = null;
        ResultSet queryResponse = null;
        List<String> noteIds = new ArrayList<>();
        try {
            queryStatement = connection.createStatement();
            queryResponse = queryStatement.executeQuery("Select notename from errorformsyncdetails;");

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
                            + "createddatetime, notename,modelJson,processDateTime,newNoteGuid) values (?,?,?,?,?,?,?,?,?) ;");
            preparedStatement.setLong(1, id);
            preparedStatement.setString(2, loggingModel.getNoteid());
            preparedStatement.setString(3, loggingModel.getStatus());
            preparedStatement.setString(4, loggingModel.getErrorDetails());
            preparedStatement.setLong(5, Long.valueOf(loggingModel.getCreatedDatetime()));
            preparedStatement.setString(6, loggingModel.getNoteName());
            preparedStatement.setString(7, loggingModel.getModelJson());
            preparedStatement.setString(8, loggingModel.getProcessDateTime());
            preparedStatement.setString(9, loggingModel.getNewNoteGuid());
            preparedStatement.execute();
        } catch (Exception e) {
            logger.error("Error in insert errorformdb", e);
        } finally {
            closeStatement(preparedStatement);
        }
    }


    public long getLastSyncTime() {
        String sql = "select lastsynctime from lastsyncstatus;";
        return exceuteSql(sql);
    }


    public void updateSyncTime(String syncTime) {
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            connection = StreetlightDaoConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(
                    "UPDATE lastsyncstatus SET lastsynctime = ? ;");
            preparedStatement.setString(1, syncTime);
            preparedStatement.execute();
        } catch (Exception e) {
            logger.error("Error in updateSyncTime", e);
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


    public void insertProcessedNotes(LoggingModel loggingModel, InstallMaintenanceLogModel installMaintenanceLogModel) {
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            String sql = "SELECT max(streetlightsyncid) + 1 from  notesyncdetails";
            long id = exceuteSql(sql);
            if (id == -1 || id == 0) {
                id = 1;
            }

            connection = StreetlightDaoConnection.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(
                    "INSERT INTO notesyncdetails (streetlightsyncid , processednoteid, status,errordetails,"
                            + "createddatetime, notename,existingnodemacaddress, newnodemacaddress,isReplaceNode,isQuickNote"
                            + ",idOnController,macAddress,singleformerrordetails,singleformstatus,synctime) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ;");
            preparedStatement.setLong(1, id);
            preparedStatement.setString(2, loggingModel.getProcessedNoteId());
            preparedStatement.setString(3, loggingModel.getStatus());
            preparedStatement.setString(4, loggingModel.getErrorDetails());
            preparedStatement.setLong(5, Long.valueOf(loggingModel.getCreatedDatetime()));
            preparedStatement.setString(6, loggingModel.getNoteName());
            preparedStatement.setString(7, loggingModel.getExistingNodeMACaddress());
            preparedStatement.setString(8, loggingModel.getNewNodeMACaddress());
            preparedStatement.setString(9, loggingModel.getIsReplaceNode());
            preparedStatement.setString(10, loggingModel.getIsQuickNote() + "");
            preparedStatement.setString(11, loggingModel.getIdOnController());
            preparedStatement.setString(12, loggingModel.getMacAddress());
            String singleFormErrorDetails = null;
            String singleFormStatus = null;
            if (installMaintenanceLogModel != null) {
                singleFormErrorDetails = installMaintenanceLogModel.getErrorDetails();
                singleFormStatus = installMaintenanceLogModel.getStatus();
            }
            preparedStatement.setString(13, singleFormErrorDetails);
            preparedStatement.setString(14, singleFormStatus);
            preparedStatement.setLong(15, System.currentTimeMillis());
            preparedStatement.execute();
        } catch (Exception e) {
            logger.error("Error in insertParentNoteId", e);
        } finally {
            closeStatement(preparedStatement);
        }
    }
	
	
	/*public void insertProcessedNoteGuids(String noteGuid,String status,String errorDetails,long createdDateTime,String noteName,boolean isQuickNote,String existingNodeMACAddress,String newNodeMACAddress){
		PreparedStatement preparedStatement = null;
		Connection connection = null;
		try {
			String sql = "SELECT max(streetlightsyncid) + 1 from  notesyncdetails";
			long id = exceuteSql(sql);
			if(id == -1 || id == 0){
				id = 1; 
			}
			connection = StreetlightDaoConnection.getInstance().getConnection();
			preparedStatement = connection.prepareStatement(
					"INSERT INTO notesyncdetails (streetlightsyncid , processednoteid, status,errordetails,createddatetime, notename,existingnodemacaddress, newnodemacaddress) values (?,?,?,?,?,?,?,?) ;");
			preparedStatement.setLong(1, id);
			preparedStatement.setString(2, noteGuid);
			preparedStatement.setString(3, status);
			preparedStatement.setString(4, errorDetails);
			preparedStatement.setLong(5, createdDateTime);
			preparedStatement.setString(6, noteName);
			preparedStatement.setBoolean(7, isQuickNote);
			preparedStatement.setString(8, existingNodeMACAddress);
			preparedStatement.setString(9, newNodeMACAddress);
			preparedStatement.execute();
		} catch (Exception e) {
			logger.error("Error in insertParentNoteId",e);
		} finally {
			closeStatement(preparedStatement);
		}
	}*/


    /**
     * Get List of NoteIds which is assigned to given formtemplate
     *
     * @return
     */
    public List<String> getNoteIds() {
        Statement queryStatement = null;
        ResultSet queryResponse = null;
        List<String> noteIds = new ArrayList<>();
        try {
            queryStatement = connection.createStatement();
            queryResponse = queryStatement.executeQuery("Select processednoteid from notesyncdetails;");

            while (queryResponse.next()) {
                noteIds.add(queryResponse.getString("processednoteid"));
            }

        } catch (Exception e) {
            logger.error("Error in getNoteIds", e);
        } finally {
            closeResultSet(queryResponse);
            closeStatement(queryStatement);
        }
        return noteIds;
    }


    public List<LoggingModel> getSyncStatus() {
        Statement queryStatement = null;
        ResultSet queryResponse = null;
        List<LoggingModel> noteIds = new ArrayList<>();
        try {
            queryStatement = connection.createStatement();
            queryResponse = queryStatement.executeQuery("Select processednoteid,status,errordetails from notesyncdetails;");

            while (queryResponse.next()) {
                LoggingModel loggingModel = new LoggingModel();
                loggingModel.setStatus(queryResponse.getString("status"));
                loggingModel.setProcessedNoteId(queryResponse.getString("processednoteid"));
                loggingModel.setErrorDetails(queryResponse.getString("errordetails"));
                //
                noteIds.add(loggingModel);
            }

        } catch (Exception e) {
            logger.error("Error in getNoteIds", e);
        } finally {
            closeResultSet(queryResponse);
            closeStatement(queryStatement);
        }
        return noteIds;
    }


    public List<LoggingModel> getSyncError() {
        Statement queryStatement = null;
        ResultSet queryResponse = null;
        List<LoggingModel> noteIds = new ArrayList<>();
        try {
            queryStatement = connection.createStatement();
            queryResponse = queryStatement.executeQuery("Select processednoteid,status,errordetails from notesyncdetails where status = 'Error';");

            while (queryResponse.next()) {
                LoggingModel loggingModel = new LoggingModel();
                loggingModel.setStatus(queryResponse.getString("status"));
                loggingModel.setErrorDetails(queryResponse.getString("errordetails"));
                loggingModel.setProcessedNoteId(queryResponse.getString("processednoteid"));
                noteIds.add(loggingModel);
            }

        } catch (Exception e) {
            logger.error("Error in getNoteIds", e);
        } finally {
            closeResultSet(queryResponse);
            closeStatement(queryStatement);
        }
        return noteIds;
    }


    public JsonObject getNotebookGuid(String noteGuid) {
        Statement queryStatement = null;
        ResultSet queryResponse = null;
        JsonObject jsonObject = new JsonObject();
        try {
            queryStatement = connection.createStatement();
            queryResponse = queryStatement.executeQuery("Select notebookguid, notebookname from edgenote,edgenotebook where  edgenotebook.notebookid = edgenote.notebookid  and noteguid='" + noteGuid + "';");

            while (queryResponse.next()) {
                jsonObject.addProperty("notebookguid", queryResponse.getString("notebookguid"));
                jsonObject.addProperty("notebookname", queryResponse.getString("notebookname"));
            }

        } catch (Exception e) {
            logger.error("Error in getNotebookGuid", e);
        } finally {
            closeResultSet(queryResponse);
            closeStatement(queryStatement);
        }
        return jsonObject;
    }


    public List<SlvData> getNoteDetails() {
        Statement queryStatement = null;
        ResultSet queryResponse = null;
        List<SlvData> slvDataList = new ArrayList<>();
        try {
            queryStatement = connection.createStatement();
            queryResponse = queryStatement.executeQuery("Select locationdescription, noteguid, title,groupname from edgenoteview where iscurrent = true and isdeleted = false;");

            while (queryResponse.next()) {
                SlvData slvData = new SlvData();

                slvData.setTitle(queryResponse.getString("title"));
                slvData.setGuid(queryResponse.getString("noteguid"));
                slvData.setLocation(queryResponse.getString("locationdescription"));
                String locDes = slvData.getLocation();
                if (locDes != null) {
                    locDes = locDes.replace(",", "");
                    String[] vals = locDes.split("\\|");
                    slvData.setLocation(vals[0].trim());
                    try {
                        slvData.setLayerName(vals[1].trim());
                    } catch (Exception e) {
                        slvData.setLayerName(queryResponse.getString("groupname"));
                    }

                }
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


    public List<SlvData> getSLVData() {
        Statement queryStatement = null;
        ResultSet queryResponse = null;
        List<SlvData> slvDataList = new ArrayList<>();
        try {
            queryStatement = connection.createStatement();
            queryResponse = queryStatement.executeQuery("Select location_proposedcontext, idoncontroller from slvdata_june19;");

            while (queryResponse.next()) {
                SlvData slvData = new SlvData();
                slvData.setTitle(queryResponse.getString("idoncontroller"));
                slvData.setLocation(queryResponse.getString("location_proposedcontext"));
                if (slvData.getTitle() != null && !slvData.getTitle().trim().isEmpty()) {
                    if (slvData.getLocation() != null) {
                        slvData.setLocation(slvData.getLocation().replace(",", ""));
                    }

                    slvDataList.add(slvData);
                }

            }

        } catch (Exception e) {
            logger.error("Error in getNotebookGuid", e);
        } finally {
            closeResultSet(queryResponse);
            closeStatement(queryStatement);
        }
        return slvDataList;
    }


    public FullEdgeNotebook getNotebook(String notebookGuid) {
        Statement queryStatement = null;
        ResultSet queryResponse = null;
        try {
            queryStatement = connection.createStatement();
            queryResponse = queryStatement.executeQuery("Select * from edgenotebook where notebookguid='" + notebookGuid + "';");

            while (queryResponse.next()) {
                FullEdgeNotebook edgeNotebook = new FullEdgeNotebook();
                edgeNotebook.setCreatedBy("admin");
                edgeNotebook.setQuickNoteCustomName(queryResponse.getString("customname"));
                edgeNotebook.setIncludeDateTime(queryResponse.getBoolean("isincludedatetime"));
                edgeNotebook.setLastUpdatedTime(queryResponse.getLong("lastupdatedtime"));
                edgeNotebook.setNotebookDescription(queryResponse.getString("notebookdesc"));
                edgeNotebook.setNotebookName(queryResponse.getString("notebookname"));
                edgeNotebook.setQuickNoteNameType(queryResponse.getString("notenametype"));
                return edgeNotebook;
            }

        } catch (Exception e) {
            logger.error("Error in getNotebookGuid", e);
        } finally {
            closeResultSet(queryResponse);
            closeStatement(queryStatement);
        }
        return null;
    }


    public String getNotebookByName(String notebookName) {
        Statement queryStatement = null;
        ResultSet queryResponse = null;
        try {
            queryStatement = connection.createStatement();
            queryResponse = queryStatement.executeQuery("Select notebookguid from edgenotebook where notebookname='" + notebookName + "';");

            while (queryResponse.next()) {
                return queryResponse.getString("notebookguid");
            }

        } catch (Exception e) {
            logger.error("Error in getNotebookGuid", e);
        } finally {
            closeResultSet(queryResponse);
            closeStatement(queryStatement);
        }
        return null;
    }
}
