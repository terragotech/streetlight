package com.terragoedge.slvinterface.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.terragoedge.slvinterface.dao.tables.SlvDevice;
import com.terragoedge.slvinterface.dao.tables.SlvSyncDetails;
import com.terragoedge.slvinterface.enumeration.Status;
import com.terragoedge.slvinterface.model.EdgeNote;
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
    private static final Logger logger = Logger.getLogger(ConnectionDAO.class);

    ConnectionDAO() {

        try {
            connectionSource = new JdbcConnectionSource(DATABASE_URL);
            //  TableUtils.createTableIfNotExists(connectionSource, SlvSyncDetails.class);
            // TableUtils.createTableIfNotExists(connectionSource, SlvDevice.class);
            slvSyncDetailsDao = DaoManager.createDao(connectionSource, SlvSyncDetails.class);
            slvDeviceDao = DaoManager.createDao(connectionSource, SlvDevice.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setupDatabase() {

    }

    public void saveSlvDevices(SlvDevice slvDevice) {
        try {
            slvDevice.setProcessedDateTime(System.currentTimeMillis());
            slvDeviceDao.create(slvDevice);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SlvDevice getSlvDevices(String deviceId) {
        try {
            return slvDeviceDao.queryBuilder().where().eq(SlvDevice.SLV_DEVICE_ID, deviceId).queryForFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public SlvSyncDetails getSlvSyncDetailWithoutTalq(String deviceId) {
        try {
            return slvSyncDetailsDao.queryBuilder().where().eq(SlvSyncDetails.NOTE_GUID, deviceId).and().isNull(SlvSyncDetails.TALQ_ADDRESS).queryForFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveSlvSyncDetails(SlvSyncDetails slvSyncDetails) {
        try {
            slvSyncDetailsDao.createOrUpdate(slvSyncDetails);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getEdgeNoteGuid(String formTemplateGuid) {
        try {
            List<String> noteGuids = slvSyncDetailsDao.queryRaw("select noteguid from edgenote, edgeform where edgenote.isdeleted = false  and edgenote.iscurrent = true and  edgenote.noteid =  edgeform.edgenoteentity_noteid and formdef like '%SELC QR Code#00%' and  edgeform.formtemplateguid = '" + formTemplateGuid + "';", new RawRowMapper<String>() {
                @Override
                public String mapRow(String[] columnNames, String[] resultColumns) throws SQLException {
                    return resultColumns[0];
                }
            }).getResults();
            return noteGuids;
        } catch (Exception e) {
              logger.error("Error in getNoteGuids",e);
        }
        //24c4a87e-92ee-433f-ad06-353a73640975
       /* List<String> stringList = new ArrayList<>();
        stringList.add("f3a60382-28d2-4038-bff1-48d435fa8a16");
        stringList.add("f4f79f31-7ba9-423f-8dea-bb568738701f");
        stringList.add("a55b1731-7063-4769-a5d0-c6c0b7f61634");
        stringList.add("351213f5-6330-45ba-b2ef-45b5edc0dd3f");
        stringList.add("c6a2168d-c0f4-4771-a418-b5f48b62dcf7");
        stringList.add("46f00b24-2eff-46f0-8f16-b179468861b2");
        stringList.add("7627b7ef-793e-4f14-aa2f-8c40c2a2a64d");
        stringList.add("3a599060-1a0f-4147-bb81-37708fe18371");
        stringList.add("855e40ec-209a-469e-b329-2680746ecf81");
        stringList.add("a33f8181-6cef-4613-a3f5-cce961c2fd8c");
        stringList.add("940e4784-785a-4bf0-9c46-94b7034705ae");
        stringList.add("912d569f-f692-425e-a2a4-c3ffcd91877b");
        stringList.add("8bd5f4f1-2aa1-4142-88b5-f43fc177ce15");
        stringList.add("c30ece6c-b379-4345-abb6-413fd8576a01");
        stringList.add("72d09ff5-b625-4747-9ecc-e6393ebff1b1");
        stringList.add("2e2d34fb-d585-422f-9890-641b741749d8");


        stringList.add("79788155-8ec4-4b27-b4bb-326b6128337f");
        stringList.add("afa6243c-117f-483e-9bd3-de76b236f1b5");
        stringList.add("7be5c71c-455f-4a4f-9dcc-c4342808f842");
        stringList.add("f753c654-2bd3-457e-8446-df948271d5ae");
        stringList.add("6e2c0ccf-f0bd-454a-9ebc-45273ffd4dc6");

        stringList.add("dfad16ff-f7f9-440a-ae25-70c5ffbf6321");
        stringList.add("459407e2-3901-435a-9076-d80ba0c65c20");

        return stringList;*/
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

    public List<SlvSyncDetails> getUnSyncedTalqaddress() {
        try {
            return slvSyncDetailsDao.queryBuilder().where().isNull(SlvSyncDetails.TALQ_ADDRESS).and().eq(SlvSyncDetails.STATUS, Status
                    .Success.toString()).query();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();

    }

    public List<SlvSyncDetails> getSyncEntityList() {
        try {
            return slvSyncDetailsDao.queryBuilder().query();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();

    }

    public void deleteProcessedNotes(String noteGuid) {
        try {
            DeleteBuilder<SlvSyncDetails, String> deleteBuilder = slvSyncDetailsDao.deleteBuilder();
            deleteBuilder.where().eq(SlvSyncDetails.NOTE_GUID, noteGuid);
            slvSyncDetailsDao.delete(deleteBuilder.prepare());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateSlvSyncdetails(SlvSyncDetails slvSyncDetails) {
        try {
            slvSyncDetailsDao.update(slvSyncDetails);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateSlvDevice(String idOnController, String macAddress) {
        try {
            UpdateBuilder<SlvDevice, String> updateBuilder = slvDeviceDao.updateBuilder();
            updateBuilder.where().eq(SlvDevice.SLV_DEVICE_ID, idOnController);
            updateBuilder.updateColumnValue(SlvDevice.MACADDRESS, macAddress);
            updateBuilder.updateColumnValue(SlvDevice.PROCESSED_DATE_TIME,System.currentTimeMillis());
            updateBuilder.update();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public SlvSyncDetails getFixtureSyncDetails(String fixtureId) {
        try {
            return slvSyncDetailsDao.queryBuilder().where().eq(SlvSyncDetails.NOTENAME, fixtureId).and().isNull(SlvSyncDetails.TALQ_ADDRESS).queryForFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ConnectionSource getConnection() {
        return connectionSource;
    }
}
