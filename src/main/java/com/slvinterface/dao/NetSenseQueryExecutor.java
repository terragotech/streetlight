package com.slvinterface.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.slvinterface.entity.EdgeAllMac;
import com.slvinterface.entity.NetSenseEntity;
import com.slvinterface.entity.SLVSyncTable;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

public class NetSenseQueryExecutor {

    ConnectionSource connectionSource = null;
    private static final Logger logger = Logger.getLogger(NetSenseQueryExecutor.class);
    private Dao<NetSenseEntity, String> netSenseDAO = null;

    public NetSenseQueryExecutor() throws Exception
    {
        connectionSource = ConnectionDao.INSTANCE.connectionSource;
        initTable();
        initDao();
    }
    private void initDao() throws Exception {
        netSenseDAO = DaoManager.createDao(connectionSource, NetSenseEntity.class);
    }
    private void initTable() throws Exception
    {
        TableUtils.createTableIfNotExists(connectionSource, NetSenseEntity.class);
        /*List<String[]> result1 = getLastModifiedTime();
        if(result1 != null)
        {
            if(result1.size() < 1)
            {
                //setOnceLastModifiedTime(System.currentTimeMillis());
            }
        }*/
    }
    public List<String[]> getLastModifiedTime(){
        List<String[]> results = null;
        String queryString = "select lastupdatedtime from netsensedata";
        try {
            GenericRawResults<String[]> rawResults = netSenseDAO.queryRaw(queryString);
            results = rawResults.getResults();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return results;
    }
    public List<String[]> getLatestNoteGUIDS(long mtime){
        List<String[]> results = null;
        String queryString = "select noteguid,createddatetime from edgenote where iscurrent=true and isdeleted=false and createddatetime > " + Long.toString(mtime);
        try {
            GenericRawResults<String[]> rawResults = netSenseDAO.queryRaw(queryString);
            results = rawResults.getResults();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return results;
    }
    public void setLastModifiedTime(long mtime)
    {
        String queryString = "update netsensedata set lastupdatedtime=" + Long.toString(mtime);
        try {
            netSenseDAO.executeRaw(queryString);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    public void setOnceLastModifiedTime(long mtime)
    {
        String queryString = "insert into netsensedata values(" + Long.toString(mtime) + ")";
        System.out.println(queryString);
        try {
            netSenseDAO.executeRaw(queryString);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

    }

}
