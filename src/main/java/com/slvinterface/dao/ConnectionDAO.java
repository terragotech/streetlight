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
import com.slvinterface.dao.tables.SLVTransactionLogs;
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
    public Dao<SLVTransactionLogs, String> slvTransactionLogDao = null;

    private static final Logger logger = Logger.getLogger(ConnectionDAO.class);

    ConnectionDAO() {

        try {
            connectionSource = new JdbcConnectionSource(DATABASE_URL);
            try {
                TableUtils.createTableIfNotExists(connectionSource, SlvSyncDetails.class);
            }catch (Exception e){
                //  logger.error("Error",e)
            }
            try {
                TableUtils.createTableIfNotExists(connectionSource, SlvDevice.class);
            }catch (Exception e){
                //  logger.error("Error",e)
                e.printStackTrace();
            }
            try {
                TableUtils.createTableIfNotExists(connectionSource, SLVTransactionLogs.class);
            }catch (Exception e){
                //  logger.error("Error",e)
            }





            slvSyncDetailsDao = DaoManager.createDao(connectionSource, SlvSyncDetails.class);
            slvDeviceDao = DaoManager.createDao(connectionSource, SlvDevice.class);
            slvTransactionLogDao = DaoManager.createDao(connectionSource, SLVTransactionLogs.class);

        } catch (Exception e) {
            //e.printStackTrace();
        }

    }


    public void saveSLVTransactionLog(SLVTransactionLogs slvTransactionLogs)
    {
        try {
            slvTransactionLogDao.create(slvTransactionLogs);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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



    public void saveSlvSyncDetails(SlvSyncDetails slvSyncDetails) {
        try {
            slvSyncDetailsDao.create(slvSyncDetails);
        } catch (Exception e) {
            logger.error("Error",e);
        }
    }








    public void updateSlvDevice(String idOnController, String macAddress,String deviceValues) {
        try {
            UpdateBuilder<SlvDevice, String> updateBuilder = slvDeviceDao.updateBuilder();
            updateBuilder.where().eq(SlvDevice.SLV_DEVICE_ID, idOnController);
            updateBuilder.updateColumnValue(SlvDevice.MACADDRESS, macAddress);
            updateBuilder.updateColumnValue(SlvDevice.PROCESSED_DATE_TIME,System.currentTimeMillis());
            updateBuilder.updateColumnValue(SlvDevice.DEVICE_VALUES,deviceValues);
            updateBuilder.update();
        } catch (Exception e) {
            logger.error("Error",e);
        }
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

    public boolean checkNoteProcessed(String noteguid){
        boolean result = false;
        try {
            String queryString = "select noteguid from slvsyncinfo where noteguid='" + noteguid + "'";
            GenericRawResults<String[]> rawResults = slvSyncDetailsDao.queryRaw(queryString);
            List<String[]> results = rawResults.getResults();
            if(results.size() > 0 )
            {
                result = true;
            }
            else
            {
                result = false;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return result;
    }


}

