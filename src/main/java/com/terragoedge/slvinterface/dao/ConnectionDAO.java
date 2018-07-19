package com.terragoedge.slvinterface.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.terragoedge.slvinterface.dao.tables.SlvDevice;
import com.terragoedge.slvinterface.dao.tables.SlvSyncDetails;

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
    public ConnectionSource getConnection() {
        return connectionSource;
    }
}
