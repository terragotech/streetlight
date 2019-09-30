package com.slvinterface.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.query.In;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.slvinterface.entity.InBoundNewDevices;
import com.slvinterface.entity.InBoundSLVData;

import com.slvinterface.json.InBoundConfig;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SLVDataQueryExecutor {
    ConnectionSource connectionSource = null;

    private static final Logger logger = Logger.getLogger(SLVDataQueryExecutor.class);

    private Dao<InBoundSLVData, String> inBoundSLVDataDao;
    private Dao<InBoundNewDevices, String> inBoundNewDeviceDao;

    public SLVDataQueryExecutor() throws Exception {
        connectionSource = ConnectionDao.INSTANCE.connectionSource;
        initDao();
    }
    private void initDao() throws Exception {
        inBoundSLVDataDao = DaoManager.createDao(connectionSource, InBoundSLVData.class);
        inBoundNewDeviceDao = DaoManager.createDao(connectionSource,InBoundNewDevices.class);

    }
    public List<String[]> getNewDeviceList(InBoundConfig inBoundConfig){
        List<String[]> results = null;
        try {
            TableUtils.dropTable(connectionSource, InBoundNewDevices.class,true);

            inBoundSLVDataDao.executeRaw("create table tmp_newdevices as select * from slvdata");
            inBoundSLVDataDao.executeRaw("delete from tmp_newdevices where tmp_newdevices.idoncontroller  in (select idoncontroller from slvdata_y)");
            System.out.println("select " + inBoundConfig.getSlvquery() + " from tmp_newdevices");
            GenericRawResults<String[]> rawResults = inBoundSLVDataDao.queryRaw("select " + inBoundConfig.getSlvquery() + " from tmp_newdevices");
            results = rawResults.getResults();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return results;
    }
    public List<String[]> getUpdatedDeviceList(InBoundConfig inBoundConfig){
        List<String[]> results = null;
        try {
            //TableUtils.dropTable(connectionSource, InBoundNewDevices.class,true);

            GenericRawResults<String[]> rawResults = inBoundSLVDataDao.queryRaw("select slvdata.name,slvdata.idoncontroller,slvdata.macaddress from slvdata,slvdata_y where upper(slvdata.macaddress) != upper(slvdata_y.macaddress) and slvdata.idoncontroller=slvdata_y.idoncontroller");
            results = rawResults.getResults();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return results;
    }
    public String getNoteBookGuid(String notebookName)
    {
        String result = "";
        try {
            List<String[]> results = null;
            GenericRawResults<String[]> rawResults = inBoundSLVDataDao.queryRaw("select notebookguid from edgenotebook where notebookname='" + notebookName + "' and isdeleted=false" );
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
    public String getCurrentNoteGUID(String noteTitle)
    {
        String result = "";
        try {
            List<String[]> results = null;
            GenericRawResults<String[]> rawResults = inBoundSLVDataDao.queryRaw("select noteguid from edgenote where iscurrent=true and isdeleted=false and title='" + noteTitle + "'" );
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
}
