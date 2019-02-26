package com.terragoedge.slvinterface.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.terragoedge.slvinterface.dao.tables.DuplicateMacAddress;
import com.terragoedge.slvinterface.dao.tables.SlvDevice;
import com.terragoedge.slvinterface.dao.tables.SlvSyncDetail;
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
    private Dao<SlvSyncDetail, String> slvSyncDetailsDao;
    public Dao<SlvDevice, String> slvDeviceDao = null;
    public Dao<DuplicateMacAddress, String> duplicateMacaddressDao = null;
    private static final Logger logger = Logger.getLogger(ConnectionDAO.class);

    ConnectionDAO() {

        try {
            connectionSource = new JdbcConnectionSource(DATABASE_URL);
            try {
                TableUtils.createTable(connectionSource, SlvSyncDetail.class);
                TableUtils.createTable(connectionSource, SlvDevice.class);
                TableUtils.createTable(connectionSource, DuplicateMacAddress.class);
            }catch (Exception e){
              //  e.printStackTrace();
            }
            slvSyncDetailsDao = DaoManager.createDao(connectionSource, SlvSyncDetail.class);
            slvDeviceDao = DaoManager.createDao(connectionSource, SlvDevice.class);
            duplicateMacaddressDao = DaoManager.createDao(connectionSource,DuplicateMacAddress.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public List<String> getEdgeNoteGuid(String formTemplateGuid) {
        try {
            List<String> noteGuids = slvSyncDetailsDao.queryRaw("select noteguid from edgenote, edgeform where edgenote.isdeleted = false  and edgenote.iscurrent = true  and  edgenote.noteid =  edgeform.edgenoteentity_noteid and edgenote.createddatetime > 1549002982362 and edgenote.title = 'LC2B' and edgeform.formtemplateguid = '" + formTemplateGuid + "';", new RawRowMapper<String>() {
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

    public void saveDuplicateMacAddress(DuplicateMacAddress duplicateMacAddress){
        try {
            duplicateMacaddressDao.create(duplicateMacAddress);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<DuplicateMacAddress> getDuplicateEntities(){
        try {
            return duplicateMacaddressDao.queryForAll();
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void saveSlvSyncDetail(SlvSyncDetail slvSyncDetail){
        try {
            slvSyncDetailsDao.create(slvSyncDetail);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void updateSlvSyncDetail(SlvSyncDetail slvSyncDetail){
        try {
            slvSyncDetailsDao.update(slvSyncDetail);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<SlvSyncDetail> getProcessedItems(){
        try{
            return slvSyncDetailsDao.queryBuilder().selectColumns("noteguid").query();
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void saveSlvDevice(SlvDevice slvDevice){
        try {
            slvDeviceDao.create(slvDevice);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void updateSlvDevice(SlvDevice slvDevice){
        try {
            slvDeviceDao.update(slvDevice);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<SlvDevice> getProcessedSlvDevices(){
        try {
            return slvDeviceDao.queryForAll();
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    public ConnectionSource getConnection() {
        return connectionSource;
    }
}
