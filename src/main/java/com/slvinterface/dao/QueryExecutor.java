package com.slvinterface.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.slvinterface.entity.EdgeAllCalendar;
import com.slvinterface.entity.SLVSyncTable;
import com.slvinterface.entity.SLVTransactionLogs;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class QueryExecutor {

    ConnectionSource connectionSource = null;

    private static final Logger logger = Logger.getLogger(QueryExecutor.class);

    private Dao<EdgeAllCalendar, String> edgeAllMacsDao;
    public Dao<SLVSyncTable, String> slvSyncTablesDao = null;
    public Dao<SLVTransactionLogs, String> slvTransactionLogsDao = null;


    public QueryExecutor() throws Exception {
        connectionSource = ConnectionDao.INSTANCE.connectionSource;
        initTable();
        initDao();
    }


    private void initDao() throws Exception {
        edgeAllMacsDao = DaoManager.createDao(connectionSource, EdgeAllCalendar.class);
        slvSyncTablesDao = DaoManager.createDao(connectionSource, SLVSyncTable.class);
        slvTransactionLogsDao = DaoManager.createDao(connectionSource, SLVTransactionLogs.class);
    }


    private void initTable() {
        try {
            TableUtils.createTableIfNotExists(connectionSource, EdgeAllCalendar.class);
        } catch (Exception e) {
              e.printStackTrace();
              logger.error("Error in EdgeAllCalendar",e);
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

    }


    public void saveEdgeAllMac(EdgeAllCalendar edgeAllCalendar) {
        try {
            edgeAllMacsDao.create(edgeAllCalendar);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void removeEdgeAllMac(String title){
        try{
            DeleteBuilder deleteBuilder =  edgeAllMacsDao.deleteBuilder();
            deleteBuilder.where().eq(EdgeAllCalendar.TITLE,title);
            deleteBuilder.delete();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public List<EdgeAllCalendar> getEdgeAllCalendar(String title, String calendar){
        try {
           return edgeAllMacsDao.queryBuilder().where().eq(EdgeAllCalendar.TITLE,title).and().eq(EdgeAllCalendar.EDGER_CALENDAR,calendar).query();
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ArrayList<>();
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
            e.printStackTrace();
        }
        return -1L;
    }

}
