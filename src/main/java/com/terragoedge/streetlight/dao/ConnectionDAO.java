package com.terragoedge.streetlight.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.terragoedge.streetlight.json.model.SlvServerData;
import org.apache.log4j.Logger;

public enum ConnectionDAO {
    INSTANCE;

    private final static String DATABASE_URL = "jdbc:postgresql://127.0.0.1:5432/terragoedge?user=postgres&password=password";

    ConnectionSource connectionSource = null;
    final Logger logger = org.apache.log4j.Logger.getLogger(ConnectionDAO .class);


    public Dao<SlvServerData, String> slvDeviceDao = null;


    ConnectionDAO() {
        openConnection();
    }

    private void openConnection(){
        try {
            connectionSource = new JdbcConnectionSource(DATABASE_URL);
            try {
                TableUtils.createTable(connectionSource, SlvServerData.class);
            }catch (Exception e){
                e.printStackTrace();
            }
            slvDeviceDao = DaoManager.createDao(connectionSource, SlvServerData.class);
            System.out.println("Connected.....");
        } catch (Exception e) {
            logger.error("Error in openConnection",e);
        }
    }


    public Dao<SlvServerData, String> getSlvDeviceDao() {
        return slvDeviceDao;
    }

    public void reConnect(){
        if(connectionSource == null){
            openConnection();
        }
    }


    public void closeConnection(){
        if(connectionSource != null){
            try{
                connectionSource.close();
                connectionSource = null;
            }catch (Exception e){
                logger.error("Error in closeConnection",e);
            }

        }
    }
}
