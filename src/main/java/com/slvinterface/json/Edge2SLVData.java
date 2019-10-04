package com.slvinterface.json;


public class Edge2SLVData {

    private String title;
    private String idOnController;
    private String controllerStrId;
    private String installDate;
    private String macAddress;
    private Priority priority;
    private String existingMACAddress;
    private String fixtureQRScan;
    private String installStatus;

    private String premiseNodeLocation;
    private String poleNo;
    private String StreetAddress;
    private String LLCGrid;

    private String fixtureOwnerShipCode;
    private String fixturecompatibleUnit;
    private String armCompatibleUnit;
    private String supplyType;
    private String fixtureStyle;
    private String lightInstallationDate;

    private String armSize;
    private String armType;
    private String lightLocationType;
    private String latitude;
    private String longitude;
    private String associatedTransformer;
    private String llcVoltage;
    private String shade;

    private String height;
    private String poleInstallationDate;

    private String poleColor;
    private String material;

    private String slopShroud;
    private String poleOwnershipCode;

    private String fixtureWattage;
    private String lampType;
    private String fixtureType;
    private String fixtureColor;
    private String installComments;

    public static final String  CALENDAR = "";
    public static final String  CABINET_CONTROLLER  = "";
    public static final String  TIMEZONE = "Etc/UTC";
    public static final String HIGH_VOLTAGE_THRESHOLD = "135";
    public static final String LOW_VOLTAGE_THRESHOLD = "95";
    public static final String POWER_FACTOR_THRESHOLD = "0.6";
    public static final String NIC_FALLBACK_MODE = "";
    public static final String DEFAULT_LIGHT_LEVEL = "100";
    public static final String VIRTUAL_POWER_OUTPUT = "100";
    public static final String CLO_INITIAL_VALUE = "100";
    public static final String ON_LUX_LEVEL = "10";
    public static final String OFF_LUX_LEVEL = "30";
    public static final String POLE_INTERVAL = "5";
    public static final String SAMPLES_INTERVAL = "2";
    public static final String DIMMING_INTERFACE= "DALI LOG";





    public String getInstallStatus() {
        return installStatus;
    }

    public void setInstallStatus(String installStatus) {
        this.installStatus = installStatus;
    }

    public String getFixtureQRScan() {
        return fixtureQRScan;
    }

    public void setFixtureQRScan(String fixtureQRScan) {
        this.fixtureQRScan = fixtureQRScan;
    }

    public String getExistingMACAddress() {
        return existingMACAddress;
    }

    public void setExistingMACAddress(String existingMACAddress) {
        this.existingMACAddress = existingMACAddress;
    }

    public String getIdOnController() {
        return idOnController;
    }

    public void setIdOnController(String idOnController) {
        this.idOnController = idOnController;
    }

    public String getControllerStrId() {
        return controllerStrId;
    }

    public void setControllerStrId(String controllerStrId) {
        this.controllerStrId = controllerStrId;
    }

    public String getInstallDate() {
        return installDate;
    }

    public void setInstallDate(String installDate) {
        this.installDate = installDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getPremiseNodeLocation() {
        return premiseNodeLocation;
    }

    public void setPremiseNodeLocation(String premiseNodeLocation) {
        this.premiseNodeLocation = premiseNodeLocation;
    }

    public String getPoleNo() {
        return poleNo;
    }

    public void setPoleNo(String poleNo) {
        this.poleNo = poleNo;
    }

    public String getStreetAddress() {
        return StreetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        StreetAddress = streetAddress;
    }

    public String getLLCGrid() {
        return LLCGrid;
    }

    public void setLLCGrid(String LLCGrid) {
        this.LLCGrid = LLCGrid;
    }

    public String getFixtureOwnerShipCode() {
        return fixtureOwnerShipCode;
    }

    public void setFixtureOwnerShipCode(String fixtureOwnerShipCode) {
        this.fixtureOwnerShipCode = fixtureOwnerShipCode;
    }

    public String getFixturecompatibleUnit() {
        return fixturecompatibleUnit;
    }

    public void setFixturecompatibleUnit(String fixturecompatibleUnit) {
        this.fixturecompatibleUnit = fixturecompatibleUnit;
    }

    public String getArmCompatibleUnit() {
        return armCompatibleUnit;
    }

    public void setArmCompatibleUnit(String armCompatibleUnit) {
        this.armCompatibleUnit = armCompatibleUnit;
    }

    public String getSupplyType() {
        return supplyType;
    }

    public void setSupplyType(String supplyType) {
        this.supplyType = supplyType;
    }

    public String getFixtureStyle() {
        return fixtureStyle;
    }

    public void setFixtureStyle(String fixtureStyle) {
        this.fixtureStyle = fixtureStyle;
    }

    public String getLightInstallationDate() {
        return lightInstallationDate;
    }

    public void setLightInstallationDate(String lightInstallationDate) {
        this.lightInstallationDate = lightInstallationDate;
    }

    public String getArmSize() {
        return armSize;
    }

    public void setArmSize(String armSize) {
        this.armSize = armSize;
    }

    public String getArmType() {
        return armType;
    }

    public void setArmType(String armType) {
        this.armType = armType;
    }

    public String getLightLocationType() {
        return lightLocationType;
    }

    public void setLightLocationType(String lightLocationType) {
        this.lightLocationType = lightLocationType;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAssociatedTransformer() {
        return associatedTransformer;
    }

    public void setAssociatedTransformer(String associatedTransformer) {
        this.associatedTransformer = associatedTransformer;
    }

    public String getLlcVoltage() {
        return llcVoltage;
    }

    public void setLlcVoltage(String llcVoltage) {
        this.llcVoltage = llcVoltage;
    }

    public String getShade() {
        return shade;
    }

    public void setShade(String shade) {
        this.shade = shade;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getPoleInstallationDate() {
        return poleInstallationDate;
    }

    public void setPoleInstallationDate(String poleInstallationDate) {
        this.poleInstallationDate = poleInstallationDate;
    }

    public String getPoleColor() {
        return poleColor;
    }

    public void setPoleColor(String poleColor) {
        this.poleColor = poleColor;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getSlopShroud() {
        return slopShroud;
    }

    public void setSlopShroud(String slopShroud) {
        this.slopShroud = slopShroud;
    }

    public String getPoleOwnershipCode() {
        return poleOwnershipCode;
    }

    public void setPoleOwnershipCode(String poleOwnershipCode) {
        this.poleOwnershipCode = poleOwnershipCode;
    }

    public String getFixtureWattage() {
        return fixtureWattage;
    }

    public void setFixtureWattage(String fixtureWattage) {
        this.fixtureWattage = fixtureWattage;
    }

    public String getLampType() {
        return lampType;
    }

    public void setLampType(String lampType) {
        this.lampType = lampType;
    }

    public String getFixtureType() {
        return fixtureType;
    }

    public void setFixtureType(String fixtureType) {
        this.fixtureType = fixtureType;
    }

    public String getFixtureColor() {
        return fixtureColor;
    }

    public void setFixtureColor(String fixtureColor) {
        this.fixtureColor = fixtureColor;
    }

    public String getInstallComments() {
        return installComments;
    }

    public void setInstallComments(String installComments) {
        this.installComments = installComments;
    }

    @Override
    public String toString() {
        return "Edge2SLVData{" +
                "title='" + title + '\'' +
                ", idOnController='" + idOnController + '\'' +
                ", controllerStrId='" + controllerStrId + '\'' +
                ", installDate='" + installDate + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", priority=" + priority +
                ", existingMACAddress='" + existingMACAddress + '\'' +
                ", fixtureQRScan='" + fixtureQRScan + '\'' +
                ", installStatus='" + installStatus + '\'' +
                '}';
    }
}
