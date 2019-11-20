package com.slvinterface.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.slvinterface.dao.tables.SlvDevice;
import com.slvinterface.dao.tables.SlvSyncDetails;
import com.slvinterface.enumeration.Status;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public enum ConnectionDAO {

    INSTANCE;

    private final static String DATABASE_URL = "jdbc:postgresql://127.0.0.1:5432/terragoedge?user=postgres&password=password";

    ConnectionSource connectionSource = null;
    private Dao<SlvSyncDetails, String> slvSyncDetailsDao;
    public Dao<SlvDevice, String> slvDeviceDao = null;
    private static final Logger logger = Logger.getLogger(ConnectionDAO.class);

    ConnectionDAO() {

        try {
            connectionSource = new JdbcConnectionSource(DATABASE_URL);
            try {
                TableUtils.createTable(connectionSource, SlvSyncDetails.class);
                TableUtils.createTable(connectionSource, SlvDevice.class);
            }catch (Exception e){
                //  logger.error("Error",e)
            }
            slvSyncDetailsDao = DaoManager.createDao(connectionSource, SlvSyncDetails.class);
            slvDeviceDao = DaoManager.createDao(connectionSource, SlvDevice.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setupDatabase() {

    }

    public void saveSlvDevices(SlvDevice slvDevice) {
        try {
            slvDevice.setProcessedDateTime(System.currentTimeMillis());
            slvDeviceDao.create(slvDevice);
        } catch (Exception e) {
            logger.error("Error",e);
        }
    }

    public SlvDevice getSlvDevices(String deviceId) {
        try {
            return slvDeviceDao.queryBuilder().where().eq(SlvDevice.SLV_DEVICE_ID, deviceId).queryForFirst();
        } catch (Exception e) {
            logger.error("Error",e);
        }
        return null;
    }

    public SlvSyncDetails getSlvSyncDetailWithoutTalq(String deviceId) {
        try {
            return slvSyncDetailsDao.queryBuilder().where().eq(SlvSyncDetails.NOTE_GUID, deviceId).and().isNull(SlvSyncDetails.TALQ_ADDRESS).queryForFirst();
        } catch (Exception e) {
            logger.error("Error",e);
        }
        return null;
    }

    public void saveSlvSyncDetails(SlvSyncDetails slvSyncDetails) {
        try {
            slvSyncDetailsDao.createOrUpdate(slvSyncDetails);
        } catch (Exception e) {
            logger.error("Error",e);
        }
    }

    public List<String> getEdgeNoteGuid(String formTemplateGuid) {
        try {
            List<String> noteGuids = slvSyncDetailsDao.queryRaw("select noteguid from edgenote, edgeform where edgenote.isdeleted = false  and edgenote.iscurrent = true  and  edgenote.noteid =  edgeform.edgenoteentity_noteid and edgenote.createddatetime > 1569906000000 and edgeform.formtemplateguid = '" + formTemplateGuid + "' order by edgenote.synctime asc;", new RawRowMapper<String>() {
                @Override
                public String mapRow(String[] columnNames, String[] resultColumns) throws SQLException {
                    return resultColumns[0];
                }
            }).getResults();
            return noteGuids;
        } catch (Exception e) {
            logger.error("Error in getNoteGuids",e);
        }
        return  new ArrayList<>();

    }

    /**
     * Get List of NoteIds which is assigned to given formtemplate
     *
     * @param
     * @return
     */
    public List<String> getNoteIds() {
        Statement queryStatement = null;
        ResultSet queryResponse = null;
        List<String> noteIds = new ArrayList<>();
        try {
            //     queryStatement = connection.createStatement();
            queryResponse = queryStatement.executeQuery("Select processednoteid from notesyncdetails;");

            while (queryResponse.next()) {
                noteIds.add(queryResponse.getString("processednoteid"));
            }

        } catch (Exception e) {
            //   logger.error("Error in getNoteIds", e);
        } finally {
            //   closeResultSet(queryResponse);
            //  closeStatement(queryStatement);
        }
        return noteIds;
    }

    public List<SlvSyncDetails> getUnSyncedTalqaddress() {
        try {
            return slvSyncDetailsDao.queryBuilder().where().isNull(SlvSyncDetails.TALQ_ADDRESS).and().eq(SlvSyncDetails.STATUS, Status
                    .Success.toString()).query();
        } catch (Exception e) {
            logger.error("Error",e);
        }
        return new ArrayList<>();

    }

    public List<SlvSyncDetails> getSyncEntityList() {
        try {
            return slvSyncDetailsDao.queryBuilder().query();
        } catch (Exception e) {
            logger.error("Error",e);
        }
        return new ArrayList<>();

    }

    public void deleteProcessedNotes(String noteGuid) {
        try {
            DeleteBuilder<SlvSyncDetails, String> deleteBuilder = slvSyncDetailsDao.deleteBuilder();
            deleteBuilder.where().eq(SlvSyncDetails.NOTE_GUID, noteGuid);
            slvSyncDetailsDao.delete(deleteBuilder.prepare());
        } catch (Exception e) {
            logger.error("Error",e);
        }
    }

    public void updateSlvSyncdetails(SlvSyncDetails slvSyncDetails) {
        try {
            slvSyncDetailsDao.update(slvSyncDetails);
        } catch (Exception e) {
            logger.error("Error",e);
        }
    }

    public void updateSlvDevice(String idOnController, String macAddress) {
        try {
            UpdateBuilder<SlvDevice, String> updateBuilder = slvDeviceDao.updateBuilder();
            updateBuilder.where().eq(SlvDevice.SLV_DEVICE_ID, idOnController);
            updateBuilder.updateColumnValue(SlvDevice.MACADDRESS, macAddress);
            updateBuilder.updateColumnValue(SlvDevice.PROCESSED_DATE_TIME,System.currentTimeMillis());
            updateBuilder.update();
        } catch (Exception e) {
            logger.error("Error",e);
        }
    }

    public SlvSyncDetails getFixtureSyncDetails(String fixtureId) {
        try {
            return slvSyncDetailsDao.queryBuilder().where().eq(SlvSyncDetails.NOTENAME, fixtureId).and().isNull(SlvSyncDetails.TALQ_ADDRESS).queryForFirst();
        } catch (Exception e) {
            logger.error("Error",e);
        }
        return null;
    }

    public ConnectionSource getConnection() {
        return connectionSource;
    }
    public long getLastSyncTime(){
        long lastSyncTime = 0;
        try {
            String queryString = "select max(processeddatetime) from slvsyncinfo";
            GenericRawResults<String[]> rawResults = slvSyncDetailsDao.queryRaw(queryString);
            List<String[]> results = rawResults.getResults();
            if(results.size() > 0 )
            {
                String []resultValues = results.get(0);
                if(resultValues[0] != null)
                {
                    lastSyncTime = Long.parseLong(resultValues[0]);
                }
                else
                {
                    lastSyncTime = 0;
                }
            }
            else
            {
                lastSyncTime = 0;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return lastSyncTime;
    }
}

