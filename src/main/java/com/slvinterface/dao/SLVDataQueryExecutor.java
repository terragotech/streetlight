package com.slvinterface.dao;

import com.google.gson.Gson;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.slvinterface.entity.InBoundNewDevices;
import com.slvinterface.entity.InBoundSLVData;

import com.slvinterface.json.InBoundConfig;
import org.apache.log4j.Logger;

import java.sql.SQLException;
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

            String queryString1 = "drop table if exists tmp_newdevices";
            inBoundSLVDataDao.executeRaw(queryString1);
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
    public List<String[]> getDelDeviceList(InBoundConfig inBoundConfig){
        List<String[]> results = null;
        try {

            String queryString1 = "drop table if exists tmp_deldevices";
            inBoundSLVDataDao.executeRaw(queryString1);
            inBoundSLVDataDao.executeRaw("create table tmp_deldevices as select * from slvdata_y");
            inBoundSLVDataDao.executeRaw("delete from tmp_deldevices where tmp_deldevices.idoncontroller  in (select idoncontroller from slvdata)");
            System.out.println("select idoncontroller,name from tmp_deldevices");
            GenericRawResults<String[]> rawResults = inBoundSLVDataDao.queryRaw("select idoncontroller,name from tmp_newdevices");
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

            String queryString3 = "select slvdata.idoncontroller,slvdata.name as name,slvdata_y.name as name_y,upper(slvdata.macaddress) as macaddress,upper(slvdata_y.macaddress) as macaddress_y from slvdata,slvdata_y where ( (slvdata.name != slvdata_y.name) or (upper(slvdata.macaddress) != upper(slvdata_y.macaddress))) and slvdata.idoncontroller=slvdata_y.idoncontroller";
            String queryString4 = "select slvdata.idoncontroller,slvdata.name as name,slvdata_y.name as name_y,upper(slvdata.macaddress) as macaddress,upper(slvdata_y.macaddress) as macaddress_y from slvdata,slvdata_y where (  (upper(slvdata.macaddress) is  not null and  upper(slvdata_y.macaddress) is  null)) and slvdata.idoncontroller=slvdata_y.idoncontroller";
            String queryString5 = "select slvdata.idoncontroller,slvdata.name as name,slvdata_y.name as name_y,upper(slvdata.macaddress) as macaddress,upper(slvdata_y.macaddress) as macaddress_y from slvdata,slvdata_y where (  (upper(slvdata.macaddress) is  null and  upper(slvdata_y.macaddress) is  not null)) and slvdata.idoncontroller=slvdata_y.idoncontroller";



            GenericRawResults<String[]> rawResults = inBoundSLVDataDao.queryRaw(queryString3);
            results = rawResults.getResults();

            GenericRawResults<String[]> rawResults1 = inBoundSLVDataDao.queryRaw(queryString4);
            List<String[]> results1 = rawResults1.getResults();

            GenericRawResults<String[]> rawResults2 = inBoundSLVDataDao.queryRaw(queryString5);
            List<String[]> results2 = rawResults2.getResults();

            results.addAll(results1);
            results.addAll(results2);

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
    public String getCurrentNoteGUIDFromIDOnController(String idoncontroller,
                                                       String formFieldName,
                                                       String formTemplateGUID
                                                       )
    {
        String queryString = "select edgenote.noteguid from edgenote,edgeform where edgeform.formtemplateguid='"+ formTemplateGUID +"' and edgeform.edgenoteentity_noteid=edgenote.noteid and edgenote.iscurrent=true and edgenote.isdeleted=false and replace(replace(substring(edgeform.formdef from '" + formFieldName + "#(.+?)count'),'\"',''),',','')='" + idoncontroller + "'";
        String result = "";
        try {
            List<String[]> results = null;
            GenericRawResults<String[]> rawResults = inBoundSLVDataDao.queryRaw(queryString);
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
    public void importSLVData(String filePath)
    {
        String queryString1 = "drop table if exists slvdata_y";
        String queryString2 = "create table slvdata_y as (select * from slvdata);";
        String queryString3 = "delete from slvdata";

        String queryString4 = "copy slvdata from '" + filePath + "' delimiter ';' csv header";
        System.out.println(queryString3);
        try {
            inBoundSLVDataDao.executeRaw(queryString1);
            inBoundSLVDataDao.executeRaw(queryString2);
            inBoundSLVDataDao.executeRaw(queryString3);
            inBoundSLVDataDao.executeRaw(queryString4);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        String result = "";
        /*try {
            List<String[]> results = null;
            GenericRawResults<String[]> rawResults = inBoundSLVDataDao.executeRaw(queryString3);
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
        }*/

    }
}
