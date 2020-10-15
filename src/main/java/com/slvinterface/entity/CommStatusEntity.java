package com.slvinterface.entity;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "inbcomstatus")
public class CommStatusEntity {
    public static String FIXTURE_ID = "fixtureid";
    public static String COMSTATUSDATE = "comstatusdate";
    public static String COMSTATUS = "comstatus";


    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = "fixtureid")
    private String fixtureid;

    @DatabaseField(columnName = "comstatusdate")
    private String comstatusdate;

    @DatabaseField(columnName = "comstatus")
    private String comstatus;


    @DatabaseField(columnName = "existinterrago")
    private String existinterrago;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFixtureid() {
        return fixtureid;
    }

    public void setFixtureid(String fixtureid) {
        this.fixtureid = fixtureid;
    }

    public String getComstatusdate() {
        return comstatusdate;
    }

    public void setComstatusdate(String comstatusdate) {
        this.comstatusdate = comstatusdate;
    }

    public String getComstatus() {
        return comstatus;
    }

    public void setComstatus(String comstatus) {
        this.comstatus = comstatus;
    }

    public String getExistinterrago() {
        return existinterrago;
    }

    public void setExistinterrago(String existinterrago) {
        this.existinterrago = existinterrago;
    }
}
