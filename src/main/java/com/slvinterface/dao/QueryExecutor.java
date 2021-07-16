package com.slvinterface.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.slvinterface.entity.*;
import org.apache.log4j.Logger;

public class QueryExecutor {

    ConnectionSource connectionSource = null;

    private static final Logger logger = Logger.getLogger(QueryExecutor.class);

    private Dao<EdgeAllMac, String> edgeAllMacsDao;
    public Dao<SLVSyncTable, String> slvSyncTablesDao = null;
    public Dao<SLVTransactionLogs, String> slvTransactionLogsDao = null;
    public Dao<PromotedFormDataEntity, String> promotedFormDataEntities = null;
    public Dao<LookupEntity, String> lookupDao = null;


    public QueryExecutor() throws Exception {
        connectionSource = ConnectionDao.INSTANCE.connectionSource;
        initTable();
        initDao();
    }


    private void initDao() throws Exception {
        edgeAllMacsDao = DaoManager.createDao(connectionSource, EdgeAllMac.class);
        slvSyncTablesDao = DaoManager.createDao(connectionSource, SLVSyncTable.class);
        slvTransactionLogsDao = DaoManager.createDao(connectionSource, SLVTransactionLogs.class);
        promotedFormDataEntities = DaoManager.createDao(connectionSource,PromotedFormDataEntity.class);
        lookupDao = DaoManager.createDao(connectionSource,LookupEntity.class);
    }


    private void initTable() {
        try {
            TableUtils.createTableIfNotExists(connectionSource, EdgeAllMac.class);
        } catch (Exception e) {
              e.printStackTrace();
              logger.error("Error in EdgeAllMac",e);
        }

        try {
            TableUtils.createTableIfNotExists(connectionSource, SLVSyncTable.class);
        } catch (Exception e) {
              e.printStackTrace();
            logger.error("Error in SLVSyncTable",e);
        }

        try {
            TableUtils.createTableIfNotExists(connectionSource, SLVTransactionLogs.class);
        } catch (Exception e) {
              e.printStackTrace();
            logger.error("Error in SLVTransactionLogs",e);
        }

        try {
            TableUtils.createTableIfNotExists(connectionSource, LookupEntity.class);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error in SLVTransactionLogs",e);
        }

    }


    public void saveEdgeAllMac(EdgeAllMac edgeAllMac) {
        try {
            edgeAllMacsDao.create(edgeAllMac);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void removeEdgeAllMac(String title){
        try{
            DeleteBuilder deleteBuilder =  edgeAllMacsDao.deleteBuilder();
            deleteBuilder.where().eq(EdgeAllMac.TITLE,title);
            deleteBuilder.delete();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public EdgeAllMac getEdgeAllMac(String title, String macAddress) throws Exception {
        return edgeAllMacsDao.queryBuilder().where().eq(EdgeAllMac.TITLE, title).and().eq(EdgeAllMac.MAC_ADDRESS, macAddress).queryForFirst();
    }


    public boolean isExistMacAddress(String idOncontroller, String macaddress) throws Exception {
        EdgeAllMac edgeAllMac = getEdgeAllMac(idOncontroller, macaddress.toUpperCase());
        return edgeAllMac != null;
    }


    public void saveSLVTransactionLogs(SLVTransactionLogs slvTransactionLogs) {
        try {
            slvTransactionLogsDao.create(slvTransactionLogs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void saveSLVTransactionLogs(SLVSyncTable slvSyncTable) throws Exception {
        slvSyncTablesDao.create(slvSyncTable);
    }


    public SLVSyncTable getSLSyncTable(String noteGuid) throws Exception {
        return slvSyncTablesDao.queryBuilder().where().eq(SLVSyncTable.NOTE_GUID, noteGuid).queryForFirst();
    }

    public Long getMaxSyncTime(){
        try{
            SLVSyncTable slvSyncTable = slvSyncTablesDao.queryBuilder().orderBy("id",false).queryForFirst();
            if(slvSyncTable != null){
                return slvSyncTable.getSyncTime();
            }

        }catch (Exception e){
            logger.error("Error in getMaxSyncTime",e);
        }
        return -1L;
    }


    public long getMaxId(){
        try {
            PromotedFormDataEntity promotedFormDataEntity =  promotedFormDataEntities.queryBuilder().orderBy(PromotedFormDataEntity.PROMOTED_ID,false).queryForFirst();
            if(promotedFormDataEntity != null){
                return promotedFormDataEntity.getPromotedId();
            }
        }catch (Exception e){
            logger.error("Error in getMaxId",e);
        }
        return -1L;
    }

    public PromotedFormDataEntity getPromotedFormDataEntity(String parentNoteGuid){
        try {
           return promotedFormDataEntities.queryBuilder().where().eq(PromotedFormDataEntity.PARENT_NOTE_GUID,parentNoteGuid).queryForFirst();
        }catch (Exception e){
            logger.error("Error in getPromotedFormDataEntity",e);
        }
        return null;
    }


    public void updatePromotedFormDataEntity(PromotedFormDataEntity promotedFormDataEntity){
        try {
          UpdateBuilder updateBuilder =  promotedFormDataEntities.updateBuilder();
            updateBuilder =   updateBuilder.updateColumnValue(PromotedFormDataEntity.PROMOTED_VALUE,promotedFormDataEntity.getPromotedvalue());
            updateBuilder =   updateBuilder.updateColumnValue(PromotedFormDataEntity.LAST_UPDATED_DATE_TIME,System.currentTimeMillis());
            if(promotedFormDataEntity.getNotebookguid() != null){
                updateBuilder =   updateBuilder.updateColumnValue(PromotedFormDataEntity.NOTEBOOK_GUID,promotedFormDataEntity.getNotebookguid());
            }
            updateBuilder.where().eq(PromotedFormDataEntity.PARENT_NOTE_GUID,promotedFormDataEntity.getParentnoteguid());
            updateBuilder.update();
        }catch (Exception e){
            logger.error("Error in updatePromotedFormDataEntity",e);
        }
    }


    public void savePromotedFormDataEntity(PromotedFormDataEntity promotedFormDataEntity){
        try {
            promotedFormDataEntities.create(promotedFormDataEntity);
        }catch (Exception e){
            logger.error("Error in savePromotedFormDataEntity",e);
        }
    }


    public LookupEntity getLookupEntity(String lanternType){
        try {
            return lookupDao.queryBuilder().where().eq(LookupEntity.LANTERN_TYPE,lanternType).queryForFirst();
        }catch (Exception e){
            logger.error("Error in getPromotedFormDataEntity",e);
        }



        return null;
    }

    public void loadLookUp(LookupEntity lookupEntity){
        try {
            lookupDao.create(lookupEntity);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
