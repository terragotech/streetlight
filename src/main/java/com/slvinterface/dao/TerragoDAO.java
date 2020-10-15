package com.slvinterface.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.slvinterface.entity.CommStatusEntity;
import com.slvinterface.entity.CommStatusNBHistoryEntity;
import com.slvinterface.entity.InBoundSLVData;

import java.sql.SQLException;
import java.util.List;

public class TerragoDAO {
    ConnectionSource connectionSource = null;
    private Dao<CommStatusEntity, String> commStatusEntitiesDao;
    private Dao<CommStatusNBHistoryEntity, String> commStatusNBHistoryEntityStringDao;
    public ConnectionSource getConnectionSource(){
        return connectionSource;
    }
    public TerragoDAO() throws SQLException
    {
        connectionSource = ConnectionDao.INSTANCE.connectionSource;
        initDao();
    }
    public void initDao() throws SQLException {
        commStatusEntitiesDao = DaoManager.createDao(connectionSource, CommStatusEntity.class);
        commStatusNBHistoryEntityStringDao = DaoManager.createDao(connectionSource,CommStatusNBHistoryEntity.class);
    }
    public CommStatusNBHistoryEntity getPreviousNoteState(String idOnController){
        CommStatusNBHistoryEntity result = null;
        try {
            List<CommStatusNBHistoryEntity> lstResult = commStatusNBHistoryEntityStringDao.queryBuilder().where().eq(CommStatusNBHistoryEntity.FIXTURE_ID, idOnController).query();
            if(lstResult.size() > 0 )
            {
                result = lstResult.get(0);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return result;
    }
    public boolean getComStatusResultOnDay(String fixtureID,String curDate)
    {
        boolean result = false;
        try {
            List<CommStatusEntity> lstResult =
                    commStatusEntitiesDao.queryBuilder()
                            .where().eq(CommStatusEntity.FIXTURE_ID, fixtureID)
                            .and().eq(CommStatusEntity.COMSTATUSDATE,curDate).query();
            if(lstResult.size() > 0){
                result = true;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return result;
    }
    public void addToHistory(CommStatusNBHistoryEntity commStatusNBHistoryEntity)
    {
        try {
            commStatusNBHistoryEntityStringDao.create(commStatusNBHistoryEntity);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    public void removeFromHistory(String idOnController)
    {
        try {
            DeleteBuilder <CommStatusNBHistoryEntity,String> deleteBuilder =
                    commStatusNBHistoryEntityStringDao.deleteBuilder();
            deleteBuilder.where().eq(CommStatusNBHistoryEntity.FIXTURE_ID, idOnController);
            deleteBuilder.delete();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    public void addIfNotExist(CommStatusEntity commStatusEntity)
    {
        try{
            List<CommStatusEntity> lstResult = commStatusEntitiesDao.queryBuilder()
                    .where().eq(CommStatusEntity.FIXTURE_ID, commStatusEntity.getFixtureid())
                    .and().eq(CommStatusEntity.COMSTATUSDATE, commStatusEntity.getComstatusdate()).query();
            if(lstResult.size() > 0)
            {
                //Allready Exists
            }
            else
            {
                commStatusEntitiesDao.create(commStatusEntity);
            }
        }
        catch (SQLException e)
        {

        }
    }
    public String getCurrentNoteGUIDFromIDOnController(String idoncontroller,
                                                       String formFieldName,
                                                       String formTemplateGUID
    )
    {
        String queryString = "select edgenote.noteguid from edgenote,edgeform where edgeform.formtemplateguid='"+ formTemplateGUID +"' and edgeform.edgenoteentity_noteid=edgenote.noteid and edgenote.iscurrent=true and edgenote.isdeleted=false and replace(replace(substring(edgeform.formdef from '" + formFieldName + "#(.+?)count'),'\"',''),',','')='" + idoncontroller + "'";
        String result = "";
        try {
            List<String[]> results = null;
            GenericRawResults<String[]> rawResults = commStatusEntitiesDao.queryRaw(queryString);
            results = rawResults.getResults();
            if(results.size() > 0)
            {
                String []values = results.get(0);
                result = values[0];
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return result;
    }
    public void updateUserName(String noteguid,String userName)
    {
        String queryString = "update edgenote set createdby='" + userName + "' where noteguid='"+ noteguid + "'";
        try{

            commStatusEntitiesDao.executeRaw(queryString);

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}
