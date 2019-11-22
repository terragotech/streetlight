package com.slvinterface.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.slvinterface.dao.tables.*;

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
            logger.error("Error",e);
        }
    }




}
