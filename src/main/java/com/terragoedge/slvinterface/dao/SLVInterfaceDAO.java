package com.terragoedge.slvinterface.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.terragoedge.slvinterface.dao.tables.SlvSyncDetails;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SLVInterfaceDAO {

    private Dao<SlvSyncDetails, String> slvSyncDetailsDao;

    private static final Logger logger = Logger.getLogger(SLVInterfaceDAO.class);

    public SLVInterfaceDAO(){
        ConnectionSource  connectionSource = ConnectionDAO.INSTANCE.getConnection();
        try{
            slvSyncDetailsDao = DaoManager.createDao(connectionSource, SlvSyncDetails.class);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }



    public List<String> getNoteGuids(){
        try {
            List<String> noteGuids = slvSyncDetailsDao.queryRaw("select noteguid from slvsyncdetails;", new RawRowMapper<String>() {
                @Override
                public String mapRow(String[] columnNames, String[] resultColumns) throws SQLException {
                    return resultColumns[0];
                }
            }).getResults();
            return noteGuids;
        }catch (Exception e){
            logger.error("Error in getNoteGuids",e);
        }
       return new ArrayList<>();
    }
}
