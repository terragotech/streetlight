package com.terragoedge.slvinterface.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.terragoedge.slvinterface.entity.InventoryReport;

import java.util.ArrayList;
import java.util.List;

public enum EdgeReportDAO {

    INSTANCE;
    private final static String DATABASE_URL = "jdbc:postgresql://127.0.0.1:5432/edgereport?user=postgres&password=password";

    ConnectionSource connectionSource = null;
    private Dao<InventoryReport, String> inventoryReportDao;

    EdgeReportDAO() {

        try {
            connectionSource = new JdbcConnectionSource(DATABASE_URL);
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

    public List<InventoryReport> getReportDetails(String macAddress){
        List<InventoryReport> inventoryReports = new ArrayList<>();
        try{
            inventoryReports = inventoryReportDao.queryBuilder().where().eq("macaddress", macAddress).query();
        }catch (Exception e){
            e.printStackTrace();
        }
        return inventoryReports;
    }
}
