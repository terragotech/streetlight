package com.terragoedge.streetlight.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.terragoedge.streetlight.json.model.DeviceAttributes;
import com.terragoedge.streetlight.json.model.DuplicateMacAddress;
import com.terragoedge.streetlight.json.model.SlvServerData;
import org.apache.log4j.Logger;

public enum ConnectionDAO {
    INSTANCE;

    private final static String DATABASE_URL = "jdbc:postgresql://127.0.0.1:5432/terragoedge?user=postgres&password=password";

    ConnectionSource connectionSource = null;
    final Logger logger = org.apache.log4j.Logger.getLogger(ConnectionDAO.class);


    public Dao<SlvServerData, String> slvDeviceDao = null;
    public Dao<DuplicateMacAddress, String> duplicateMacAddressDao = null;
    public Dao<DeviceAttributes, String> deviceAttributeDao = null;


    ConnectionDAO() {
        openConnection();
    }

    private void openConnection() {
        try {
            connectionSource = new JdbcConnectionSource(DATABASE_URL);
            try {
                TableUtils.createTable(connectionSource, DuplicateMacAddress.class);
                TableUtils.createTable(connectionSource, SlvServerData.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            slvDeviceDao = DaoManager.createDao(connectionSource, SlvServerData.class);
            duplicateMacAddressDao = DaoManager.createDao(connectionSource, DuplicateMacAddress.class);
            deviceAttributeDao = DaoManager.createDao(connectionSource, DeviceAttributes.class);
            System.out.println("Connected.....");
        } catch (Exception e) {
            logger.error("Error in openConnection", e);
        }
    }


    public Dao<SlvServerData, String> getSlvDeviceDao() {
        return slvDeviceDao;
    }

    public void reConnect() {
        if (connectionSource == null) {
            openConnection();
        }
    }

    public void saveDuplicateMacAddress(DuplicateMacAddress duplicateMacAddress) {
        try {
            duplicateMacAddressDao.create(duplicateMacAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveDeviceAttributes(DeviceAttributes deviceAttributes) {
        try {
            deviceAttributeDao.create(deviceAttributes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DuplicateMacAddress getDuplicateMacAddress(String macaddress) {
        try {
            return duplicateMacAddressDao.queryBuilder().where().eq("macaddress", macaddress).queryForFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteDuplicateMacAddress(String noteguid) {
        try {
            DeleteBuilder<DuplicateMacAddress, String> deleteBuilder = duplicateMacAddressDao.deleteBuilder();
            deleteBuilder.where().eq("noteguid", noteguid);
            deleteBuilder.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        if (connectionSource != null) {
            try {
                connectionSource.close();
                connectionSource = null;
            } catch (Exception e) {
                logger.error("Error in closeConnection", e);
            }

        }
    }
}
