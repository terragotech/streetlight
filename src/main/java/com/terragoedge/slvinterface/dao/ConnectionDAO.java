package com.terragoedge.slvinterface.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.terragoedge.slvinterface.utils.PropertiesReader;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public enum ConnectionDAO {

    INSTANCE;

    ConnectionSource connectionSource = null;
    private Dao<TitleChangeDetail, String> titleChangeDetailDao;
    private static final Logger logger = Logger.getLogger(ConnectionDAO.class);

    ConnectionDAO() {

        try {
            String DATABASE_URL = PropertiesReader.getProperties().getProperty("com.slv.database.url");
            connectionSource = new JdbcConnectionSource(DATABASE_URL);
            try {
                TableUtils.createTable(connectionSource, TitleChangeDetail.class);
            } catch (Exception e) {
                  e.printStackTrace();
            }
            titleChangeDetailDao = DaoManager.createDao(connectionSource, TitleChangeDetail.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getMaxSyncTime() {
        try {
            long maxSyncTime = titleChangeDetailDao.queryRawValue("select max(created_date_time) from slvsyncdetails");
            return maxSyncTime;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }



    public void saveSlvSyncDetail(TitleChangeDetail slvSyncDetail) {
        try {
            titleChangeDetailDao.create(slvSyncDetail);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateSlvSyncDetail(TitleChangeDetail slvSyncDetail) {
        try {
            titleChangeDetailDao.update(slvSyncDetail);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public ConnectionSource getConnection() {
        return connectionSource;
    }
}
