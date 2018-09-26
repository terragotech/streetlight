package com.terragoedge.automation.Dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.terragoedge.automation.model.MacValidationEntity;
import com.terragoedge.slvinterface.entity.EdgeFormEntity;
import com.terragoedge.slvinterface.entity.EdgeNoteEntity;
import com.terragoedge.slvinterface.utils.PropertiesReader;

public enum EdgeReportDAO {
    INSTANCE;
    private final static String DATABASE_URL = "jdbc:postgresql://127.0.0.1:5432/edgereport?user=postgres&password=password";
    //private final static String DATABASE_URL = "jdbc:postgresql://127.0.0.1:5432/terragoedge?user=postgres&password=password";
    ConnectionSource connectionSource = null;
    private Dao<MacValidationEntity, String> macValidationDao;

    EdgeReportDAO() {
        try {
            // String inventoryConnection = PropertiesReader.getProperties().getProperty("edge.reports.inventoryUrl");
            connectionSource = new JdbcConnectionSource(DATABASE_URL);
            TableUtils.createTable(connectionSource, MacValidationEntity.class);
            macValidationDao = DaoManager.createDao(connectionSource, MacValidationEntity.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createMacValidationEntity(MacValidationEntity macValidationEntity) {
        try {
            macValidationDao.create(macValidationEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MacValidationEntity getMacValidationEntity(String fixtureId, String modifiedDate) {
        try {
            return macValidationDao.queryBuilder().where().eq(MacValidationEntity.FIXTIRE_ID, fixtureId).and().eq(MacValidationEntity.MODIFIED_DATE, modifiedDate).queryForFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
