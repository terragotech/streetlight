package com.terragoedge.automation.Dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.terragoedge.automation.model.UserEntity;
import com.terragoedge.slvinterface.dao.tables.SlvDevice;
import com.terragoedge.slvinterface.dao.tables.SlvSyncDetails;
import com.terragoedge.slvinterface.entity.EdgeFormEntity;
import com.terragoedge.slvinterface.entity.EdgeNoteEntity;
import com.terragoedge.slvinterface.entity.EdgeNoteView;
import com.terragoedge.slvinterface.entity.EdgeNotebookEntity;
import com.terragoedge.slvinterface.utils.PropertiesReader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class InstallDAO {

    ConnectionSource connectionSource = null;
    Dao<EdgeFormEntity, String> edgeformDao;
    private Dao<EdgeNoteView, String> edgeNoteViewDao;
    private Dao<EdgeNoteEntity, String> edgeNoteDao;
    private Dao<EdgeNotebookEntity, String> notebookDao;
    private Dao<UserEntity, String> userDao;
    private Properties properties;

    public InstallDAO() {
        try {
            String installUrl = PropertiesReader.getProperties().getProperty("edge.reports.installUrl");
            connectionSource = new JdbcConnectionSource(installUrl);
            System.out.println("ConnectionSucess");
            edgeformDao = DaoManager.createDao(connectionSource, EdgeFormEntity.class);
            notebookDao = DaoManager.createDao(connectionSource, EdgeNotebookEntity.class);
            edgeNoteViewDao = DaoManager.createDao(connectionSource, EdgeNoteView.class);
            edgeNoteDao = DaoManager.createDao(connectionSource, EdgeNoteEntity.class);
            userDao = DaoManager.createDao(connectionSource, UserEntity.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getInventoryHandlingEntity(String formTemplateGuid, String macAddress) {
        String query = "select noteguid from edgenote, edgeform where edgenote.isdeleted = false and edgenote.iscurrent = true and  edgenote.noteid =  edgeform.edgenoteentity_noteid and edgeform.formtemplateguid = '" + formTemplateGuid + "' and formdef like '%" + macAddress + "%' order by edgenote.createddatetime desc limit 1;";
        try {
            List<String> noteGuids = edgeNoteViewDao.queryRaw(query, new RawRowMapper<String>() {
                @Override
                public String mapRow(String[] columnNames, String[] resultColumns) throws SQLException {
                    return resultColumns[0];
                }
            }).getResults();
            return (noteGuids.size() > 0) ? noteGuids.get(0) : null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public EdgeNoteView getEdgeNoteView(String noteGuid) {
        try {
            return edgeNoteViewDao.queryBuilder().where().eq(EdgeNoteView.NOTE_GUID, noteGuid).queryForFirst();
        } catch (Exception e) {
            System.out.println("Error" + e.toString());
            e.printStackTrace();
        }
        return null;
    }

    public List<UserEntity> getUsersList() {
        try {
            return userDao.queryBuilder().query();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    public EdgeFormEntity getFormDef(int noteId) {
        try {
            QueryBuilder<EdgeFormEntity, String> formBuilder = edgeformDao.queryBuilder();
            return formBuilder.where().eq("edgenoteentity_noteid", noteId).queryForFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
