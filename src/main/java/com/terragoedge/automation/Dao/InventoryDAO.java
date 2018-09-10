package com.terragoedge.automation.Dao;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.terragoedge.slvinterface.dao.tables.SlvDevice;
import com.terragoedge.slvinterface.dao.tables.SlvSyncDetails;
import com.terragoedge.slvinterface.entity.EdgeFormEntity;
import com.terragoedge.slvinterface.entity.EdgeNoteEntity;
import com.terragoedge.slvinterface.entity.EdgeNoteView;
import com.terragoedge.slvinterface.entity.EdgeNotebookEntity;
import com.terragoedge.slvinterface.enumeration.Status;
import com.terragoedge.slvinterface.utils.PropertiesReader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public enum InventoryDAO {

    INSTANCE;
    private final static String DATABASE_URL = "jdbc:postgresql://127.0.0.1:5432/terragoedge?user=postgres&password=password";

    ConnectionSource connectionSource = null;
    private Dao<SlvSyncDetails, String> slvSyncDetailsDao;
    Dao<EdgeFormEntity, String> edgeformDao;
    private Dao<EdgeNoteView, String> edgeNoteViewDao;
    private Dao<EdgeNoteEntity, String> edgeNoteDao;
    private Dao<EdgeNotebookEntity, String> notebookDao;
    public Dao<SlvDevice, String> slvDeviceDao = null;
    private Properties properties;

    InventoryDAO() {

        try {
            String inventoryConnection = PropertiesReader.getProperties().getProperty("edge.reports.inventoryUrl");
            connectionSource = new JdbcConnectionSource(inventoryConnection);
            System.out.println("ConnectionSucess");
            //TableUtils.createTable(connectionSource, SlvSyncDetails.class);
            // TableUtils.createTable(connectionSource, SlvDevice.class);
            edgeformDao = DaoManager.createDao(connectionSource, EdgeFormEntity.class);
            notebookDao = DaoManager.createDao(connectionSource, EdgeNotebookEntity.class);
            edgeNoteViewDao = DaoManager.createDao(connectionSource, EdgeNoteView.class);
            slvSyncDetailsDao = DaoManager.createDao(connectionSource, SlvSyncDetails.class);
            slvDeviceDao = DaoManager.createDao(connectionSource, SlvDevice.class);
            edgeNoteDao = DaoManager.createDao(connectionSource, EdgeNoteEntity.class);

        } catch (Exception e) {
            System.out.println("Failed inventory");
            e.printStackTrace();
        }

    }

    public void setupDatabase() {

    }

    public EdgeNoteView getEdgeNoteView(String noteGuid) {
        try {
            return edgeNoteViewDao.queryBuilder().where().eq(EdgeNoteView.NOTE_GUID, noteGuid).queryForFirst();
        } catch (Exception e) {
            System.out.println("Error"+e.toString());
            e.printStackTrace();
        }
        return null;
    }

    public EdgeNoteView getEdgeNoteViewById(List<Integer> noteId) {
        try {
            QueryBuilder<EdgeNoteView, String> queryBuilder = edgeNoteViewDao.queryBuilder();
            queryBuilder.where().in(EdgeNoteView.NOTE_ID, noteId);
            queryBuilder.orderBy(EdgeNoteView.SYNC_TIME, false);
            queryBuilder.limit(1L);
            return queryBuilder.queryForFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
       /* try {
            return edgeNoteViewDao.queryBuilder().where().in(EdgeNoteView.NOTE_ID, noteId).queryForFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return null;
    }

    public void saveSlvDevices(SlvDevice slvDevice) {
        try {
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
            List<String> noteGuids = edgeNoteViewDao.queryRaw("select noteguid from edgenote,edgeform a where a.formtemplateguid='c8acc150-6228-4a27-bc7e-0fabea0e2b93' and a.formdef like '%Unable to Repair(CDOT Issue)%' and a.edgenoteentity_noteid = edgenote.noteid and edgenote.isdeleted = 'f' and edgenote.iscurrent = 't';", new RawRowMapper<String>() {
                @Override
                public String mapRow(String[] columnNames, String[] resultColumns) throws SQLException {
                    return resultColumns[0];
                }
            }).getResults();
            return noteGuids;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();

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

    public List<EdgeFormEntity> getEdgeFormEntities(int noteId, String formTemplateGuid) {
        try {
            return edgeformDao.queryBuilder().where().eq(EdgeFormEntity.NOTEID, noteId).and().eq(EdgeFormEntity.FORM_TEMPLATE_GUID, formTemplateGuid).query();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();

    }

    public List<Integer> getEdgeNoteId(String macAddress) {
        List<Integer> noteId = new ArrayList<>();
        List<EdgeFormEntity> edgeFormEntities = getEdgeFormEntities(macAddress);
        for (EdgeFormEntity edgeFormEntity : edgeFormEntities) {
            noteId.add(edgeFormEntity.getEdgenoteentity_noteid());
        }
        return noteId;
    }

    public List<EdgeFormEntity> getEdgeFormEntities(String macAddress) {
        try {
            return edgeformDao.queryBuilder().where().like(EdgeFormEntity.FROM_DEF, "%" + macAddress + "%").query();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();

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

    public EdgeNotebookEntity getInstalledNotebook(){
        try{
          return   notebookDao.queryBuilder().where().eq(EdgeNotebookEntity.NOTEBOOK_NAME,"Installed").and().eq(EdgeNotebookEntity.IS_DELETED,false).queryForFirst();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public EdgeNoteView getEdgeNoteViewByTitle(String title){
        try{
            EdgeNotebookEntity installedNb = getInstalledNotebook();
          String sss =  edgeNoteViewDao.queryBuilder().orderBy(EdgeNoteView.CREATED_DATE_TIME,false).where().eq(EdgeNoteView.TITLE,title).and().ne(EdgeNoteView.EDGE_NOTEBOOK_ENTITY,installedNb).getStatement();
          return   edgeNoteViewDao.queryBuilder().orderBy(EdgeNoteView.CREATED_DATE_TIME,false).where().eq(EdgeNoteView.TITLE,title).and().ne(EdgeNoteView.EDGE_NOTEBOOK_ENTITY,installedNb.getNotebookId()).queryForFirst();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public EdgeNotebookEntity getEdgeNotebookEntity(String notebookId) {
        try {
            return notebookDao.queryBuilder().where().eq(EdgeNotebookEntity.NOTEBOOK_ID, notebookId).queryForFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ConnectionSource getConnection() {
        return connectionSource;
    }

    public EdgeNoteView getEdgeNoteViewFromTitle(String title) {
        try {
            return edgeNoteViewDao.queryBuilder().where().eq(EdgeNoteView.TITLE, title).queryForFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<EdgeFormEntity> getFormDef(String noteGuid){
        try {
            QueryBuilder<EdgeFormEntity,String> formBuilder = edgeformDao.queryBuilder();
            QueryBuilder<EdgeNoteEntity,String> noteBuilder = edgeNoteDao.queryBuilder();
            EdgeNoteEntity edgeNoteEntity = noteBuilder.where().eq("noteguid", noteGuid).queryForFirst();
            return formBuilder.where().eq("edgenoteentity_noteid", edgeNoteEntity.getNoteid()).query();
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public JsonArray getNotebookFromNoteTitle(List<String> titles){
        JsonArray jsonArray = new JsonArray();
        try{
           List<EdgeNoteView> edgeNoteEntities = edgeNoteViewDao.queryBuilder().where().in("title", titles).and().eq("iscurrent",true).and().eq("isdeleted",false).query();
           for(EdgeNoteView edgeNoteView : edgeNoteEntities){
               EdgeNotebookEntity edgeNotebookEntity = notebookDao.queryBuilder().where().eq("notebookid", edgeNoteView.getNotebookid()).queryForFirst();
               if(((edgeNotebookEntity == null) ||  !(edgeNotebookEntity.getNotebookName().equals("Installed") || edgeNotebookEntity.getNotebookName().startsWith("User-")))){
                   JsonObject jsonObject  =new JsonObject();
                   jsonObject.addProperty("macaddress",edgeNoteView.getTitle());
                   jsonObject.addProperty("id",edgeNoteView.getNoteGuid());
                   jsonObject.addProperty("notebook",edgeNotebookEntity.getNotebookName());
                   jsonArray.add(jsonObject);
               }
           }

        }catch (Exception e){
            e.printStackTrace();
        }
        return jsonArray;
    }
}
