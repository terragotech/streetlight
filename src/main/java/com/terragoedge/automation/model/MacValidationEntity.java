package com.terragoedge.automation.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@DatabaseTable(tableName = "mac_validation_report_install_inventory")
public class MacValidationEntity {
    public static String FIXTIRE_ID = "fixture_id";
    public static String MODIFIED_DATE = "modified_date";
    @DatabaseField(columnName = "id", generatedId = true)
    private int id;
    @DatabaseField(columnName = "mac_id")
    private String mac_id;
    @DatabaseField(columnName = "fixture_id")
    private String fixture_id;
    @DatabaseField(columnName = "municipality")
    private String municipality;
    @DatabaseField(columnName = "modified_date")
    private String modified_date;
    @DatabaseField(columnName = "user_id")
    private String user_id;
    @DatabaseField(columnName = "assigned_user")
    private String assigned_user;
    /*@DatabaseField(columnName = "createddatetime")
    private String createddatetime;*/

    public MacValidationEntity() {

    }

    public MacValidationEntity(MacValidationModel macValidationModel) {
        this.mac_id = macValidationModel.getMacaddress();
        this.fixture_id = macValidationModel.getFixtureid();
        this.municipality = macValidationModel.getMunicipality();
        //this.modified_date = getDateAsMilliSecond(macValidationModel.getModifieddate());
        this.modified_date = macValidationModel.getModifieddate();
        this.user_id = macValidationModel.getUser();
        this.assigned_user = macValidationModel.getAssigneduser();
    }

    public String getDateAsMilliSecond(String modified_date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy HH:MM");
        try {
            Date dateAsMilli = dateFormat.parse(modified_date);
            return String.valueOf(dateAsMilli.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return String.valueOf(-1);
    }

    public static String getFixtireId() {
        return FIXTIRE_ID;
    }

    public static void setFixtireId(String fixtireId) {
        FIXTIRE_ID = fixtireId;
    }

    public static String getModifiedDate() {
        return MODIFIED_DATE;
    }

    public static void setModifiedDate(String modifiedDate) {
        MODIFIED_DATE = modifiedDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMac_id() {
        return mac_id;
    }

    public void setMac_id(String mac_id) {
        this.mac_id = mac_id;
    }

    public String getFixture_id() {
        return fixture_id;
    }

    public void setFixture_id(String fixture_id) {
        this.fixture_id = fixture_id;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getModified_date() {
        return modified_date;
    }

    public void setModified_date(String modified_date) {
        this.modified_date = modified_date;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getAssigned_user() {
        return assigned_user;
    }

    public void setAssigned_user(String assigned_user) {
        this.assigned_user = assigned_user;
    }

    @Override
    public String toString() {
        return "MacValidationEntity{" +
                "id=" + id +
                ", mac_id='" + mac_id + '\'' +
                ", fixture_id='" + fixture_id + '\'' +
                ", municipality='" + municipality + '\'' +
                ", modified_date='" + modified_date + '\'' +
                ", user_id='" + user_id + '\'' +
                ", assigned_user='" + assigned_user + '\'' +
                '}';
    }
}
