package com.terragoedge.slvinterface.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.terragoedge.slvinterface.entity.InventoryReport;
import com.terragoedge.slvinterface.utils.PropertiesReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public enum EdgeReportDAO {

    INSTANCE;
    private final static String DATABASE_URL = "jdbc:postgresql://127.0.0.1:5432/edgereport?user=postgres&password=password";

    ConnectionSource connectionSource = null;
    private Dao<InventoryReport, String> inventoryReportDao;
    private Properties properties;
    EdgeReportDAO() {

        try {
            properties = PropertiesReader.getProperties();
            connectionSource = new JdbcConnectionSource(properties.getProperty("streetlight.report.db"));
            System.out.println("EdgeReportDAO ConnectionSucess");
            //TableUtils.createTable(connectionSource, SlvSyncDetails.class);
            // TableUtils.createTable(connectionSource, SlvDevice.class);
            inventoryReportDao = DaoManager.createDao(connectionSource, InventoryReport.class);

        } catch (Exception e) {
            System.out.println("Failed inventory");
            e.printStackTrace();
        }

    }

    public ConnectionSource getConnection() {
        return connectionSource;
    }

    public List<InventoryReport> getReportDetails(String macAddress,int id){
        List<InventoryReport> inventoryReports = new ArrayList<>();
        try{
            inventoryReports = inventoryReportDao.queryBuilder().where().eq("macaddress", macAddress).and().eq("id", id).query();
        }catch (Exception e){
            e.printStackTrace();
        }
        return inventoryReports;
    }
}
