package com.terragoedge.streetlight.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.terragoedge.edgeserver.*;
import com.terragoedge.streetlight.enumeration.DateType;
import com.terragoedge.streetlight.json.model.*;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public enum ConnectionDAO {
    INSTANCE;

   // private final static String DATABASE_URL = "jdbc:postgresql://127.0.0.1:5432/terragoedge?user=postgres&password=password";
   private final static String DATABASE_URL = "jdbc:postgresql://127.0.0.1:5432/terragoedge_staging?user=postgres&password=password";

    ConnectionSource connectionSource = null;
    final Logger logger = org.apache.log4j.Logger.getLogger(ConnectionDAO.class);


    public Dao<SlvServerData, String> slvDeviceDao = null;
    public Dao<DuplicateMacAddress, String> duplicateMacAddressDao = null;
    public Dao<DeviceAttributes, String> deviceAttributeDao = null;
    public Dao<DuplicateMACAddressEventLog, String> macAddressEventLogsDao = null;
    public Dao<EdgeAllMacData, String> edgeAllMacDataDao = null;
    public Dao<EdgeAllFixtureData, String> edgeAllFixtureDataDao = null;
    public Dao<SlvInterfaceLogEntity, String> slvInterfaceLogDao = null;
    public Dao<ExistingMacValidationFailure, String> existingMacValidationFailureDao = null;


    public Dao<EdgeSLVDate, String> edgeNodeDates = null;

    ConnectionDAO() {
        openConnection();
    }

    private void openConnection() {
        try {
            connectionSource = new JdbcConnectionSource(DATABASE_URL);
            try {
                TableUtils.createTableIfNotExists(connectionSource, DuplicateMacAddress.class);

            } catch (Exception e) {
                //logger.error("Error in openConnection", e);
            }

            try {
                TableUtils.createTableIfNotExists(connectionSource, SlvServerData.class);

            } catch (Exception e) {
                //logger.error("Error in openConnection", e);
            }

            try {
                TableUtils.createTableIfNotExists(connectionSource, DeviceAttributes.class);

            } catch (Exception e) {
                //logger.error("Error in openConnection", e);
            }

            try {
                TableUtils.createTableIfNotExists(connectionSource, DuplicateMACAddressEventLog.class);
            } catch (Exception e) {
                //logger.error("Error in openConnection", e);
            }
            try {
                TableUtils.createTableIfNotExists(connectionSource, EdgeAllFixtureData.class);
            } catch (Exception e) {
                //logger.error("Error in openConnection", e);
            }
            try {
                TableUtils.createTableIfNotExists(connectionSource, EdgeAllMacData.class);
            } catch (Exception e) {
               // logger.error("Error in openConnection", e);
            }
            try {
                TableUtils.createTableIfNotExists(connectionSource, SlvInterfaceLogEntity.class);
            } catch (Exception e) {
               // logger.error("Error in openConnection", e);
            }
            try{
                TableUtils.createTableIfNotExists(connectionSource,ExistingMacValidationFailure.class);
            }catch (Exception e){
                //logger.error("Error in openConnection", e);
            }


            try{
                TableUtils.createTableIfNotExists(connectionSource,EdgeSLVDate.class);
            }catch (Exception e){
                //logger.error("Error in openConnection", e);
            }

            slvDeviceDao = DaoManager.createDao(connectionSource, SlvServerData.class);
            duplicateMacAddressDao = DaoManager.createDao(connectionSource, DuplicateMacAddress.class);
            deviceAttributeDao = DaoManager.createDao(connectionSource, DeviceAttributes.class);
            macAddressEventLogsDao = DaoManager.createDao(connectionSource, DuplicateMACAddressEventLog.class);
            edgeAllMacDataDao = DaoManager.createDao(connectionSource, EdgeAllMacData.class);
            edgeAllFixtureDataDao = DaoManager.createDao(connectionSource, EdgeAllFixtureData.class);
            slvInterfaceLogDao = DaoManager.createDao(connectionSource, SlvInterfaceLogEntity.class);
            existingMacValidationFailureDao = DaoManager.createDao(connectionSource,ExistingMacValidationFailure.class);

            edgeNodeDates = DaoManager.createDao(connectionSource, EdgeSLVDate.class);

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
            logger.error("Error in saveDuplicateMacAddress",e);
        }
    }

    public void saveDeviceAttributes(DeviceAttributes deviceAttributes) {
        try {
            deviceAttributeDao.create(deviceAttributes);
        } catch (Exception e) {
            logger.error("Error in saveDeviceAttributes",e);
        }
    }

    public void saveMacAddressEventLog(DuplicateMACAddressEventLog duplicateMACAddressEventLog) {
        try {
            macAddressEventLogsDao.create(duplicateMACAddressEventLog);
        } catch (Exception e) {
            logger.error("Error in saveMacAddressEventLog",e);
        }
    }

    public DuplicateMacAddress getDuplicateMacAddress(String macaddress) {
        try {
            return duplicateMacAddressDao.queryBuilder().where().eq("macaddress", macaddress).queryForFirst();
        } catch (Exception e) {
            logger.error("Error in getDuplicateMacAddress",e);
        }
        return null;
    }

    public void deleteDuplicateMacAddress(String noteguid) {
        try {
            DeleteBuilder<DuplicateMacAddress, String> deleteBuilder = duplicateMacAddressDao.deleteBuilder();
            deleteBuilder.where().eq("noteguid", noteguid);
            deleteBuilder.delete();
        } catch (Exception e) {
            logger.error("Error in deleteDuplicateMacAddress",e);
        }
    }

    public boolean isExistMacAddress(String idOncontroller, String macaddress) {
        EdgeAllMacData edgeAllMacData = null;
        try {
            edgeAllMacData = edgeAllMacDataDao.queryBuilder().where().eq("title", idOncontroller).and().eq("macaddress", macaddress).queryForFirst();
        } catch (Exception e) {
            logger.error("Error in isExistMacAddress",e);
        }
        return (edgeAllMacData != null) ? true : false;
    }

    public void saveEdgeAllMac(EdgeAllMacData edgeAllMacData) {
        try {
            edgeAllMacDataDao.create(edgeAllMacData);
        } catch (Exception e) {
            logger.error("Error in saveEdgeAllMac",e);
        }
    }


    public void removeEdgeAllMAC(String idOnController,String macAddress){
        try {
            DeleteBuilder<EdgeAllMacData, String> deleteBuilder = edgeAllMacDataDao.deleteBuilder();
            deleteBuilder.where().eq("title",idOnController).and().eq("macaddress",macAddress);
            deleteBuilder.delete();
        }catch (Exception e){
            logger.error("Error in removeEdgeAllMAC",e);
        }
    }

    public void removeEdgeAllFixture(String idOnController){
        try {
            DeleteBuilder<EdgeAllFixtureData, String> deleteBuilderEdgeAllFix = edgeAllFixtureDataDao.deleteBuilder();
            deleteBuilderEdgeAllFix.where().eq("title",idOnController);
            deleteBuilderEdgeAllFix.delete();
        }catch (Exception e){
            logger.error("Error in removeEdgeAllFixture",e);
        }
    }

    public void saveEdgeAllFixture(EdgeAllFixtureData edgeAllFixtureData) {
        try {
            edgeAllFixtureDataDao.create(edgeAllFixtureData);
        } catch (Exception e) {
            logger.error("Error in saveEdgeAllFixture",e);
        }
    }

    public boolean isExistFixture(String idOncontroller, String fixtureQrScan) {
        EdgeAllFixtureData edgeAllFixtureData = null;
        try {
            edgeAllFixtureData = edgeAllFixtureDataDao.queryBuilder().where().eq("title", idOncontroller).and().eq("fixtureqrscan", fixtureQrScan).queryForFirst();
        } catch (Exception e) {
            logger.error("Error in isExistFixture",e);
        }
        return (edgeAllFixtureData != null) ? true : false;
    }

    public void saveSlvInterfaceLog(SlvInterfaceLogEntity slvInterfaceLogEntity) {
        try {
            slvInterfaceLogDao.create(slvInterfaceLogEntity);
        } catch (Exception e) {
            logger.error("Error in saveSlvInterfaceLog",e);
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

    public void saveExistingMacFailure(ExistingMacValidationFailure existingMacValidationFailure){
        try{
            existingMacValidationFailureDao.create(existingMacValidationFailure);
        }catch (Exception e){
            logger.error("Error in saveExistingMacFailure",e);
        }
    }

    public List<ExistingMacValidationFailure> getAllExistingMacVaildationFailures(long time){
        try {
            return existingMacValidationFailureDao.queryBuilder().where().ge("processed_date_time",time).query();
        }catch (Exception e){
            logger.error("Error in getAllExistingMacVaildationFailures",e);
        }
        return new ArrayList<>();
    }

    public void deleteExistingMacVaildationFailure(ExistingMacValidationFailure existingMacValidationFailure){
        try{
            existingMacValidationFailureDao.delete(existingMacValidationFailure);
        }catch (Exception e){
            logger.error("Error in deleteExistingMacVaildationFailure",e);
        }
    }

    public List<ExistingMacValidationFailure> getExistingMacValidationFailure(String idoncontroller,String existingMac){
        try{
            return existingMacValidationFailureDao.queryBuilder().where().eq("idoncontroller",idoncontroller).and().eq("slvmacaddress",existingMac).query();
        }catch (Exception e){
            logger.error("Error in getExistingMacValidationFailure",e);
        }
        return new ArrayList<>();
    }



    public void saveEdgeNodeDate(EdgeSLVDate edgeSLVDate){
        try{
            edgeNodeDates.create(edgeSLVDate);
        }catch (Exception e){
            logger.error("Error in saveEdgeNodeDate",e);
        }
    }

    public void removeAllEdgeFormDates(String idOnController){
        try {
            DeleteBuilder<EdgeSLVDate, String> deleteBuilder = edgeNodeDates.deleteBuilder();
            deleteBuilder.where().eq("title",idOnController);
            deleteBuilder.delete();
        }catch (Exception e){
            logger.error("Error in removeAllEdgeFormDates",e);
        }
    }

    public void removeCurrentEdgeFormDates(String idOnController){
        deleteEdgeNoteFormDate(idOnController, DateType.LUM.toString());
        deleteEdgeNoteFormDate(idOnController, DateType.NODE.toString());
    }


    public void deleteEdgeNoteFormDate(String idOnController,String type){
        try {
           DeleteBuilder<EdgeSLVDate, String> deleteBuilder = edgeNodeDates.deleteBuilder();
           deleteBuilder.where().eq("title",idOnController).and().eq("dates_type",type);
           deleteBuilder.delete();
        }catch (Exception e){
            logger.error("Error in deleteEdgeNoteFormDate",e);
        }
    }


    public EdgeSLVDate getEdgeNodeDate(String title, String edgeDate,String type){
        try {
            return edgeNodeDates.queryBuilder().where().eq("title",title).and().eq("edge_date",edgeDate).and().eq("dates_type",type).queryForFirst();
        }catch (Exception e){
            logger.error("Error in getEdgeCSLPNodeDate",e);
        }
        return null;
    }


}
