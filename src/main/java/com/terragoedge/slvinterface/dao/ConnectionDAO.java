package com.terragoedge.slvinterface.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.terragoedge.slvinterface.dao.tables.SlvDevice;
import com.terragoedge.slvinterface.dao.tables.SlvSyncDetails;

import java.util.List;

public enum ConnectionDAO {

    INSTANCE;

    private final static String DATABASE_URL = "jdbc:h2:mem:account";

    ConnectionSource connectionSource = null;
    private Dao<SlvSyncDetails, String> slvSyncDetailsDao;

    ConnectionDAO(){

        try{
            connectionSource = new JdbcConnectionSource(DATABASE_URL);
            slvSyncDetailsDao = DaoManager.createDao(connectionSource, SlvSyncDetails.class);
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setupDatabase(){

    }

    public SlvSyncDetails getSlvSyncDetailWithoutTalq(String deviceId){
        try{
            return slvSyncDetailsDao.queryBuilder().where().eq(SlvSyncDetails.NOTE_GUID, deviceId).and().isNull(SlvSyncDetails.TALQ_ADDRESS).queryForFirst();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void saveSlvSyncDetails(SlvSyncDetails slvSyncDetails){
        try {
            slvSyncDetailsDao.createOrUpdate(slvSyncDetails);
        }catch (Exception e){
            e.printStackTrace();
        }
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

    public ConnectionSource getConnection() {
        return connectionSource;
    }
}
