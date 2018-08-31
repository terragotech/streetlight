package com.terragoedge.slv2edge.metering.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.terragoedge.slv2edge.metering.model.MeteringEntity;
import com.terragoedge.slv2edge.metering.util.MeteringPropertiesReader;
import com.terragoedge.slvinterface.dao.tables.SlvSyncDetails;

import java.util.List;

public enum  MeteringServiceDAO {

    INSTANCE;


    ConnectionSource connectionSource = null;

    private Dao<MeteringEntity, String> meteringEntityDao;
    MeteringServiceDAO() {
        String DATABASE_URL = MeteringPropertiesReader.getProperties().getProperty("metering.db.connection.url");
        try {
            connectionSource = new JdbcConnectionSource(DATABASE_URL);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public List<MeteringEntity> getMeteringEntityList()throws Exception{
       return meteringEntityDao.queryBuilder().query();
    }

}
