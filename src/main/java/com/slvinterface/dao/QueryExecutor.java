package com.slvinterface.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.slvinterface.entity.EdgeAllMac;
import com.slvinterface.entity.SLVSyncTable;
import com.slvinterface.entity.SLVTransactionLogs;
import org.apache.log4j.Logger;

public class QueryExecutor {

    ConnectionSource connectionSource = null;

    private static final Logger logger = Logger.getLogger(QueryExecutor.class);

    private Dao<EdgeAllMac, String> edgeAllMacsDao;
    public Dao<SLVSyncTable, String> slvSyncTablesDao = null;
    public Dao<SLVTransactionLogs, String> slvTransactionLogsDao = null;


    public QueryExecutor() throws Exception {
        initTable();
        initDao();
    }


    private void initDao() throws Exception {
        edgeAllMacsDao = DaoManager.createDao(connectionSource, EdgeAllMac.class);
        slvSyncTablesDao = DaoManager.createDao(connectionSource, SLVSyncTable.class);
        slvTransactionLogsDao = DaoManager.createDao(connectionSource, SLVTransactionLogs.class);
    }


    private void initTable() {
        try {
            TableUtils.createTableIfNotExists(connectionSource, EdgeAllMac.class);
        } catch (Exception e) {
            //  e.printStackTrace();
        }

        try {
            TableUtils.createTableIfNotExists(connectionSource, SLVSyncTable.class);
        } catch (Exception e) {
            //  e.printStackTrace();
        }

        try {
            TableUtils.createTableIfNotExists(connectionSource, SLVTransactionLogs.class);
        } catch (Exception e) {
            //  e.printStackTrace();
        }

    }


    public void saveEdgeAllMac(EdgeAllMac edgeAllMac) {
        try {
            edgeAllMacsDao.create(edgeAllMac);
        } catch (Exception e) {
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

}
