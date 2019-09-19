package com.terragoedge.slvinterface.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.terragoedge.slvinterface.dao.tables.DuplicateMacAddress;
import com.terragoedge.slvinterface.dao.tables.GeozoneEntity;
import com.terragoedge.slvinterface.dao.tables.SlvDevice;
import com.terragoedge.slvinterface.dao.tables.SlvSyncDetail;
import com.terragoedge.slvinterface.model.JPSWorkflowModel;
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
    public Dao<GeozoneEntity, String> geozoneEntitiesDao = null;
    private static final Logger logger = Logger.getLogger(ConnectionDAO.class);

    ConnectionDAO() {

        try {
            connectionSource = new JdbcConnectionSource(DATABASE_URL);
            try {
                TableUtils.createTable(connectionSource, SlvSyncDetail.class);
            } catch (Exception e) {
                  e.printStackTrace();
            }
            try {
                TableUtils.createTable(connectionSource, GeozoneEntity.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                TableUtils.createTable(connectionSource, DuplicateMacAddress.class);
            } catch (Exception e) {
            }
            try {
                TableUtils.createTable(connectionSource, SlvDevice.class);
            } catch (Exception e) {
            }
            slvSyncDetailsDao = DaoManager.createDao(connectionSource, SlvSyncDetail.class);
            slvDeviceDao = DaoManager.createDao(connectionSource, SlvDevice.class);
            duplicateMacaddressDao = DaoManager.createDao(connectionSource, DuplicateMacAddress.class);
            geozoneEntitiesDao = DaoManager.createDao(connectionSource, GeozoneEntity.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getEdgeNoteGuid(String installFormTemplateGuid,String newFixtureFormTemplateGuid,long maxSyncTime) {
        try {

            List<String> noteGuids = slvSyncDetailsDao.queryRaw("select noteguid from edgenote, edgeform where edgenote.isdeleted = false and edgenote.iscurrent = true and edgenote.noteid = edgeform.edgenoteentity_noteid and (edgeform.formtemplateguid = '" + installFormTemplateGuid + "' or edgeform.formtemplateguid = '" + newFixtureFormTemplateGuid + "') and edgenote.synctime > "+maxSyncTime+" order by edgenote.synctime asc;", new RawRowMapper<String>() {
                @Override
                public String mapRow(String[] columnNames, String[] resultColumns) throws SQLException {
                    return resultColumns[0];
                }
            }).getResults();
            return noteGuids;
        } catch (Exception e) {
            logger.error("Error in getNoteGuids", e);
        }
        return new ArrayList<>();

    }

    public SlvSyncDetail getSlvSyncDetails(String noteguid) {
        try {
            SlvSyncDetail slvSyncDetail = slvSyncDetailsDao.queryBuilder().where().eq("noteguid", noteguid).queryForFirst();
            return slvSyncDetail;
        } catch (Exception e) {
            logger.error("Error in getNoteGuids", e);
        }
        return null;

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

    public void saveDuplicateMacAddress(DuplicateMacAddress duplicateMacAddress) {
        try {
            duplicateMacaddressDao.create(duplicateMacAddress);
        } catch (Exception e) {
            logger.error("Erro ", e);
        }
    }

    public SlvSyncDetail getSlvSyncDetail(String poleNumber) {
        try {
            return slvSyncDetailsDao.queryBuilder().where().eq("pole_number", poleNumber).queryForFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<DuplicateMacAddress> getDuplicateEntities() {
        try {
            return duplicateMacaddressDao.queryForAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void saveSlvSyncDetail(SlvSyncDetail slvSyncDetail) {
        try {
            slvSyncDetailsDao.create(slvSyncDetail);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateSlvSyncDetail(SlvSyncDetail slvSyncDetail) {
        try {
            slvSyncDetailsDao.update(slvSyncDetail);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getProcessedItems() {
        try {
            try {
                List<String> noteGuids = slvSyncDetailsDao.queryRaw("select noteguid from slvsyncdetails;", new RawRowMapper<String>() {
                    @Override
                    public String mapRow(String[] columnNames, String[] resultColumns) throws SQLException {
                        return resultColumns[0];
                    }
                }).getResults();
                return noteGuids;
            } catch (Exception e) {
                logger.error("Error in getNoteGuids", e);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void saveSlvDevice(SlvDevice slvDevice) {
        try {
            slvDeviceDao.create(slvDevice);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SlvDevice getSlvDevice(String poleNumber) {
        try {
            return slvDeviceDao.queryBuilder().where().eq("pole_number", poleNumber).queryForFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateSlvDevice(SlvDevice slvDevice) {
        try {
            slvDeviceDao.update(slvDevice);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<SlvDevice> getProcessedSlvDevices() {
        try {
            return slvDeviceDao.queryForAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void createGeozone(GeozoneEntity geozoneEntity) {
        try {
            logger.info("Geozone value has been inserted local geozone table");
            geozoneEntitiesDao.create(geozoneEntity);
        } catch (Exception e) {
            logger.error("geozone inserted Error", e);
        }
    }

    public GeozoneEntity getGeozoneEntity(JPSWorkflowModel jpsWorkflowModel) {
        try {
            return geozoneEntitiesDao.queryBuilder().where().eq("parishzonename", jpsWorkflowModel.getCity()).and().eq("divisionzonename", jpsWorkflowModel.getNotebookName()).and().eq("streetzonename",jpsWorkflowModel.getAddress1()).queryForFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public long getMaxSyncTime() {
        try {
            long maxSyncTime = slvSyncDetailsDao.queryRawValue("select max(created_date_time) from slvsyncdetails");
            return maxSyncTime;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    public ConnectionSource getConnection() {
        return connectionSource;
    }
}
