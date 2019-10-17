package com.terrago.jsoncsvconvertor;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBean;

public class SLVData extends CsvToBean {

    @CsvBindByName
    private String idOnController;
    @CsvBindByName
    private String address;
    @CsvBindByName
    private String location_atlasphysicalpage;
    @CsvBindByName
    private String luminaire_proposedfixture;
    @CsvBindByName
    private String location_proposedcontext;
    @CsvBindByName
    private String comed_projectname;
    @CsvBindByName
    private String luminaire_fixturecode;
    @CsvBindByName
    private String fixing_mastarmangle;
    @CsvBindByName
    private String fixing_mastarmlength;
    @CsvBindByName
    private String fixing_numberofmastarms;
    @CsvBindByName
    private String TalqAddress;
    @CsvBindByName
    private String luminaire_type;
    @CsvBindByName
    private String pole_type;
    @CsvBindByName
    private String pole_height;
    @CsvBindByName
    private String pole_material;
    @CsvBindByName
    private String luminaire_warranty_start_date;
    @CsvBindByName
    private String luminaire_warranty_status;
    @CsvBindByName
    private String macAddress;
    @CsvBindByName
    private String cslp_node_install_date;
    @CsvBindByName
    private String cslp_lum_install_date;
    @CsvBindByName
    private String install_date;
    @CsvBindByName
    private String luminaire_installdate;
    @CsvBindByName
    private String fixtureqrscan;

    public String getIdOnController() {
        return idOnController;
    }

    public void setIdOnController(String idOnController) {
        this.idOnController = idOnController;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocation_atlasphysicalpage() {
        return location_atlasphysicalpage;
    }

    public void setLocation_atlasphysicalpage(String location_atlasphysicalpage) {
        this.location_atlasphysicalpage = location_atlasphysicalpage;
    }

    public String getLuminaire_proposedfixture() {
        return luminaire_proposedfixture;
    }

    public void setLuminaire_proposedfixture(String luminaire_proposedfixture) {
        this.luminaire_proposedfixture = luminaire_proposedfixture;
    }

    public String getLocation_proposedcontext() {
        return location_proposedcontext;
    }

    public void setLocation_proposedcontext(String location_proposedcontext) {
        this.location_proposedcontext = location_proposedcontext;
    }

    public String getComed_projectname() {
        return comed_projectname;
    }

    public void setComed_projectname(String comed_projectname) {
        this.comed_projectname = comed_projectname;
    }

    public String getLuminaire_fixturecode() {
        return luminaire_fixturecode;
    }

    public void setLuminaire_fixturecode(String luminaire_fixturecode) {
        this.luminaire_fixturecode = luminaire_fixturecode;
    }

    public String getFixing_mastarmangle() {
        return fixing_mastarmangle;
    }

    public void setFixing_mastarmangle(String fixing_mastarmangle) {
        this.fixing_mastarmangle = fixing_mastarmangle;
    }

    public String getFixing_mastarmlength() {
        return fixing_mastarmlength;
    }

    public void setFixing_mastarmlength(String fixing_mastarmlength) {
        this.fixing_mastarmlength = fixing_mastarmlength;
    }

    public String getFixing_numberofmastarms() {
        return fixing_numberofmastarms;
    }

    public void setFixing_numberofmastarms(String fixing_numberofmastarms) {
        this.fixing_numberofmastarms = fixing_numberofmastarms;
    }

    public String getTalqAddress() {
        return TalqAddress;
    }

    public void setTalqAddress(String talqAddress) {
        TalqAddress = talqAddress;
    }

    public String getLuminaire_type() {
        return luminaire_type;
    }

    public void setLuminaire_type(String luminaire_type) {
        this.luminaire_type = luminaire_type;
    }

    public String getPole_type() {
        return pole_type;
    }

    public void setPole_type(String pole_type) {
        this.pole_type = pole_type;
    }

    public String getPole_height() {
        return pole_height;
    }

    public void setPole_height(String pole_height) {
        this.pole_height = pole_height;
    }

    public String getPole_material() {
        return pole_material;
    }

    public void setPole_material(String pole_material) {
        this.pole_material = pole_material;
    }

    public String getLuminaire_warranty_start_date() {
        return luminaire_warranty_start_date;
    }

    public void setLuminaire_warranty_start_date(String luminaire_warranty_start_date) {
        this.luminaire_warranty_start_date = luminaire_warranty_start_date;
    }

    public String getLuminaire_warranty_status() {
        return luminaire_warranty_status;
    }

    public void setLuminaire_warranty_status(String luminaire_warranty_status) {
        this.luminaire_warranty_status = luminaire_warranty_status;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getCslp_node_install_date() {
        return cslp_node_install_date;
    }

    public void setCslp_node_install_date(String cslp_node_install_date) {
        this.cslp_node_install_date = cslp_node_install_date;
    }

    public String getCslp_lum_install_date() {
        return cslp_lum_install_date;
    }

    public void setCslp_lum_install_date(String cslp_lum_install_date) {
        this.cslp_lum_install_date = cslp_lum_install_date;
    }

    public String getInstall_date() {
        return install_date;
    }

    public void setInstall_date(String install_date) {
        this.install_date = install_date;
    }

    public String getLuminaire_installdate() {
        return luminaire_installdate;
    }

    public void setLuminaire_installdate(String luminaire_installdate) {
        this.luminaire_installdate = luminaire_installdate;
    }

    public String getFixtureqrscan() {
        return fixtureqrscan;
    }

    public void setFixtureqrscan(String fixtureqrscan) {
        this.fixtureqrscan = fixtureqrscan;
    }

    @Override
    public String toString() {
        return "SLVData{" +
                "idOnController='" + idOnController + '\'' +
                ", address='" + address + '\'' +
                ", location_atlasphysicalpage='" + location_atlasphysicalpage + '\'' +
                ", luminaire_proposedfixture='" + luminaire_proposedfixture + '\'' +
                ", location_proposedcontext='" + location_proposedcontext + '\'' +
                ", comed_projectname='" + comed_projectname + '\'' +
                ", luminaire_fixturecode='" + luminaire_fixturecode + '\'' +
                ", fixing_mastarmangle='" + fixing_mastarmangle + '\'' +
                ", fixing_mastarmlength='" + fixing_mastarmlength + '\'' +
                ", fixing_numberofmastarms='" + fixing_numberofmastarms + '\'' +
                ", TalqAddress='" + TalqAddress + '\'' +
                ", luminaire_type='" + luminaire_type + '\'' +
                ", pole_type='" + pole_type + '\'' +
                ", pole_height='" + pole_height + '\'' +
                ", pole_material='" + pole_material + '\'' +
                ", luminaire_warranty_start_date='" + luminaire_warranty_start_date + '\'' +
                ", luminaire_warranty_status='" + luminaire_warranty_status + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", cslp_node_install_date='" + cslp_node_install_date + '\'' +
                ", cslp_lum_install_date='" + cslp_lum_install_date + '\'' +
                ", install_date='" + install_date + '\'' +
                ", luminaire_installdate='" + luminaire_installdate + '\'' +
                ", fixtureqrscan='" + fixtureqrscan + '\'' +
                '}';
    }
}
