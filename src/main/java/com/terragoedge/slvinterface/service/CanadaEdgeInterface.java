package com.terragoedge.slvinterface.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.terragoedge.slvinterface.dao.tables.SlvSyncDetails;
import com.terragoedge.slvinterface.exception.DeviceCreationFailedException;
import com.terragoedge.slvinterface.exception.DeviceUpdationFailedException;
import com.terragoedge.slvinterface.exception.InValidBarCodeException;
import com.terragoedge.slvinterface.exception.NoValueException;
import com.terragoedge.slvinterface.json.slvInterface.ConfigurationJson;
import com.terragoedge.slvinterface.kingcity.DefaultData;
import com.terragoedge.slvinterface.kingcity.GeoZoneDetails;
import com.terragoedge.slvinterface.model.CanadaFormModel;
import com.terragoedge.slvinterface.model.EdgeFormData;
import com.terragoedge.slvinterface.model.EdgeNote;
import com.terragoedge.slvinterface.model.FormData;
import com.terragoedge.slvinterface.utils.ResourceDetails;
import com.terragoedge.slvinterface.utils.Utils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.ResponseEntity;

import java.io.FileInputStream;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

public class CanadaEdgeInterface extends SlvInterfaceService {

    static Map<String, Integer> luminareCore = new HashMap<>();

    List<DefaultData> defaultDataHashMap = new ArrayList<>();
    List<GeoZoneDetails> geoZoneDetailsList = new ArrayList<>();


    public CanadaEdgeInterface() {
        super();
        loadDefaultValue();
        loadGeoZoneVal();
        loadMappingVal();
    }

    static {
        luminareCore.put("L1", 39);
        luminareCore.put("L2", 58);
        luminareCore.put("L3", 27);
        luminareCore.put("L4", 80);
        luminareCore.put("L5", 108);
        luminareCore.put("L6", 108);
        luminareCore.put("L7", 133);
        luminareCore.put("L8", 158);
        luminareCore.put("L9", 80);
        luminareCore.put("L10", 133);
        luminareCore.put("L11", 108);
        luminareCore.put("L12", 158);
        luminareCore.put("L13", 80);
        luminareCore.put("L14", 52);
        luminareCore.put("P1", 25);
        luminareCore.put("PL5", 44);
        luminareCore.put("PL4", 44);
        luminareCore.put("PL3", 89);
        luminareCore.put("PL1", 89);
        luminareCore.put("PL6", 44);
        luminareCore.put("PL2", 89);

    }

    JsonObject mappingJson = null;

    @Override
    public void buildFixtureStreetLightData(String data, List<Object> paramsList, EdgeNote edgeNote) throws InValidBarCodeException {

    }


    private String dateFormat(Long dateTime) {
        Date date = new Date(Long.valueOf(dateTime));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dff = dateFormat.format(date);
        return dff;
    }

    public void addOtherParams(EdgeNote edgeNote, List<Object> paramsList) {
        addStreetLightData("install.date", Utils.dateFormat(edgeNote.getCreatedDateTime()), paramsList);
        addStreetLightData("installStatus", "Installed", paramsList);
        addStreetLightData("location.utillocationid", edgeNote.getTitle() + ".Lamp", paramsList);
    }

    @Override
    public void processSetDevice(List<EdgeFormData> edgeFormDataList, ConfigurationJson configurationJson, EdgeNote edgeNote, List<Object> paramsList, SlvSyncDetails slvSyncDetails, String controllerStrIdValue) throws NoValueException, DeviceUpdationFailedException {
        paramsList.add("idOnController=" + edgeNote.getTitle());
        addStreetLightData("idOnController", edgeNote.getTitle(), paramsList);
        paramsList.add("controllerStrId=" + controllerStrIdValue);
        addOtherParams(edgeNote, paramsList);
        updateDeviceValues(paramsList, edgeNote, edgeFormDataList);
        setDeviceValues(paramsList, slvSyncDetails);
    }


    public void updateDeviceValues(List<Object> paramsList, EdgeNote edgeNote,
                                   List<EdgeFormData> edgeFormValuesList) throws NoValueException {
        String comment = "";
        int power2Watt = 0;
        int lWatt = 0;


        for (EdgeFormData formValues : edgeFormValuesList) {
            JsonElement streetLightKey = mappingJson.get(formValues.getLabel());
            if (streetLightKey != null && !streetLightKey.isJsonNull()) {
                String key = streetLightKey.getAsString();
                String value = formValues.getValue();


                if (formValues.getLabel().equals("New Luminare Code")) {
                    if (value != null && !(value.trim().isEmpty())
                            && !(value.trim().equalsIgnoreCase("(null)"))) {
                        String[] luminareCoreValues = value.split("-");
                        if (luminareCoreValues.length > 0) {
                            String luminareCoreValue = luminareCoreValues[0].trim();
                            if (luminareCore.containsKey(luminareCoreValue)) {
                                lWatt = luminareCore.get(luminareCoreValue);
                            }
                        }
                    }
                }


                if (!key.equals("comment") && !key.equals("MacAddress") && !key.toLowerCase().equals("power")) {
                    addStreetLightData(key, value, paramsList);
                }


                if (key.equalsIgnoreCase("power2")) {
                    if (value != null && !(value.trim().isEmpty())
                            && !(value.trim().equalsIgnoreCase("(null)"))) {
                        String temp = value.replaceAll("[^\\d.]", "");
                        temp = temp.trim();
                        power2Watt = Integer.parseInt(temp);
                    }

                }

                if (key.equalsIgnoreCase("comment")) {
                    comment = comment + " " + formValues.getLabel() + ":" + value;
                } else {
                    if (key.equalsIgnoreCase("power")) {
                        if (value != null && !(value.trim().isEmpty())
                                && !(value.trim().equalsIgnoreCase("(null)"))) {
                            String temp = value.replaceAll("[^\\d.]", "");
                            temp = temp.trim();
                            lWatt = Integer.parseInt(temp);
                        }

                    }
                }


            }

        }


        if (lWatt == 0) {
            addStreetLightData("power", "39 W", paramsList);
            lWatt = 39;
        } else {
            addStreetLightData("power", lWatt + " W", paramsList);
        }
        int watt = power2Watt - lWatt;
        addStreetLightData("powerCorrection", watt + "", paramsList);


        addStreetLightData("location.utillocationid", edgeNote.getTitle() + ".Lamp", paramsList);
        String nodeTypeStrId = properties.getProperty("streetlight.slv.equipment.type");
        addStreetLightData("modelFunctionId", nodeTypeStrId, paramsList);

        addStreetLightData("comment", comment, paramsList);

        String streetLightDate = dateFormat(edgeNote.getCreatedDateTime());
        addStreetLightData("lamp.installdate", streetLightDate, paramsList);
        addStreetLightData("location.locationtype", "LOCATION_TYPE_PREMISE", paramsList);

        DefaultData defaultData = new DefaultData();
        defaultData.setIdOnController(edgeNote.getTitle());
        int pos = defaultDataHashMap.indexOf(defaultData);
        if (pos != -1) {
            defaultData = defaultDataHashMap.get(pos);
            addStreetLightData("DimmingGroupName", defaultData.getDimmingGroupValue(), paramsList);
        }

    }

    @Override
    public void updateFormTemplateValues(List<EdgeFormData> edgeFormDataList, CanadaFormModel canadaFormModel) {
        // new streetlights 26
        Utils.updateFormValue(edgeFormDataList, 26, canadaFormModel.getAttribute());
        // action 1
        Utils.updateFormValue(edgeFormDataList, 1, canadaFormModel.getAction());
        //SL 31
        Utils.updateFormValue(edgeFormDataList, 31, canadaFormModel.getSl());
        // streetname 35
        Utils.updateFormValue(edgeFormDataList, 35, canadaFormModel.getStreet_name());
        //geozone 32
        Utils.updateFormValue(edgeFormDataList, 32, canadaFormModel.getGeozoneId());
        //block 34
        Utils.updateFormValue(edgeFormDataList, 34, canadaFormModel.getBlock());
        //Pole_Type 38
        Utils.updateFormValue(edgeFormDataList, 38, canadaFormModel.getPole_Type());
        //Arm_Length 39
        Utils.updateFormValue(edgeFormDataList, 39, canadaFormModel.getArm_Length());
        //Lum_Ht 40
        Utils.updateFormValue(edgeFormDataList, 40, canadaFormModel.getLum_Ht());
        //Pole_Colour 42
        Utils.updateFormValue(edgeFormDataList, 42, canadaFormModel.getPole_Colour());
        // Luminaire_Per_Pole 43
        Utils.updateFormValue(edgeFormDataList, 43, canadaFormModel.getLuminaire_Per_Pole());
        //SELC QR Code 22
        Utils.updateFormValue(edgeFormDataList, 22, canadaFormModel.getMacAddress());
        // Pole Tag Present 45
        Utils.updateFormValue(edgeFormDataList, 45, canadaFormModel.getPole_Tag_Present());
        // Utility Pole 46
        Utils.updateFormValue(edgeFormDataList, 46, canadaFormModel.getUtility_Pole());
        // Arm Type 47
        Utils.updateFormValue(edgeFormDataList, 47, canadaFormModel.getArmType());
        //Vegetation Obstruction 48
        Utils.updateFormValue(edgeFormDataList, 48, canadaFormModel.getVegetation_Obstruction());
        // New Luminare Code 49
        Utils.updateFormValue(edgeFormDataList, 49, canadaFormModel.getNew_Luminare_Code());
        // L1 Watt 50
        Utils.updateFormValue(edgeFormDataList, 50, canadaFormModel.getL1_Watt());
        // Watt 37
        Utils.updateFormValue(edgeFormDataList, 37, canadaFormModel.getWatt());
        // Pole Length (m) 64
        Utils.updateFormValue(edgeFormDataList, 64, canadaFormModel.getPole_Length());
        // Pole Condition 65
        Utils.updateFormValue(edgeFormDataList, 65, canadaFormModel.getPole_Condition());
        // Pole Manufacturer 66
        Utils.updateFormValue(edgeFormDataList, 66, canadaFormModel.getPole_manufacturer());
        //Fuse 67
        Utils.updateFormValue(edgeFormDataList, 67, canadaFormModel.getFuse());
    }

    @Override
    public CanadaFormModel processDuplicateForm(List<FormData> formDataList) {
        CanadaFormModel canadaFormModel = new CanadaFormModel();
        for (FormData formData : formDataList) {
            List<EdgeFormData> edgeFormDataList = formData.getFormDef();
            for (EdgeFormData edgeFormData : edgeFormDataList) {
                if (edgeFormData != null) {
                    switch (edgeFormData.getLabel()) {
                        case "SL":
                            if (nullCheck(edgeFormData.getValue()))
                                canadaFormModel.setSl(edgeFormData.getValue());
                            break;
                        case "Street_name":
                            if (nullCheck(edgeFormData.getValue()))
                                canadaFormModel.setStreet_name(edgeFormData.getValue());
                            break;
                        case "Pole_Type":
                            if (nullCheck(edgeFormData.getValue()))
                                canadaFormModel.setPole_Type(edgeFormData.getValue());
                            break;
                        case "Arm_Length":
                            if (nullCheck(edgeFormData.getValue()))
                                canadaFormModel.setArm_Length(edgeFormData.getValue());
                            break;
                        case "Lum_Ht":
                            if (nullCheck(edgeFormData.getValue()))
                                canadaFormModel.setLum_Ht(edgeFormData.getValue());
                            break;
                        case "Pole_Colour":
                            if (nullCheck(edgeFormData.getValue()))
                                canadaFormModel.setPole_Colour(edgeFormData.getValue());
                            break;
                        case "Luminaire_Per_Pole":
                            if (nullCheck(edgeFormData.getValue()))
                                canadaFormModel.setLuminaire_Per_Pole(edgeFormData.getValue());
                            break;
                        case "SELC QR Code":
                            if (nullCheck(edgeFormData.getValue()))
                                canadaFormModel.setMacAddress(edgeFormData.getValue());
                            break;
                        case "Luminaire Scan":
                            if (nullCheck(edgeFormData.getValue()))
                                if (edgeFormData.getValue() != null && edgeFormData.getValue().startsWith("00135"))
                                    canadaFormModel.setMacAddress(edgeFormData.getValue());
                            break;
                        case "Pole Tag Present":
                            if (nullCheck(edgeFormData.getValue()))
                                canadaFormModel.setPole_Tag_Present(edgeFormData.getValue());
                            break;
                        case "Utility Pole":
                            if (nullCheck(edgeFormData.getValue()))
                                canadaFormModel.setUtility_Pole(edgeFormData.getValue());
                            break;
                        case "Arm Type":
                            if (nullCheck(edgeFormData.getValue()))
                                canadaFormModel.setArmType(edgeFormData.getValue());
                            break;
                        case "Vegetation Obstruction":
                            if (nullCheck(edgeFormData.getValue()))
                                canadaFormModel.setVegetation_Obstruction(edgeFormData.getValue());
                            break;
                        case "New Luminare Code":
                            if (nullCheck(edgeFormData.getValue()))
                                canadaFormModel.setNew_Luminare_Code(edgeFormData.getValue());
                            break;
                        case "Watt":
                            if (nullCheck(edgeFormData.getValue()))
                                canadaFormModel.setWatt(edgeFormData.getValue());
                            break;
                        case "Pole Length (m)":
                            if (nullCheck(edgeFormData.getValue()))
                                canadaFormModel.setPole_Length(edgeFormData.getValue());
                            break;
                        case "Pole Condition":
                            if (nullCheck(edgeFormData.getValue()))
                                canadaFormModel.setPole_Condition(edgeFormData.getValue());
                            break;
                        case "Pole Manufacturer":
                            if (nullCheck(edgeFormData.getValue()))
                                canadaFormModel.setPole_manufacturer(edgeFormData.getValue());
                            break;
                        case "Fuse":
                            if (nullCheck(edgeFormData.getValue()))
                                canadaFormModel.setFuse(edgeFormData.getValue());
                            break;
                        case "Block":
                            if (nullCheck(edgeFormData.getValue()))
                                canadaFormModel.setBlock(edgeFormData.getValue());
                            break;
                        case "Facility name":
                            if (nullCheck(edgeFormData.getValue()))
                                canadaFormModel.setStreet_name(edgeFormData.getValue());
                            break;
                        case "Geozone ID":
                            if (nullCheck(edgeFormData.getValue()))
                                canadaFormModel.setGeozoneId(edgeFormData.getValue());
                            break;
                        case "Attributes":
                            if (nullCheck(edgeFormData.getValue()))
                                canadaFormModel.setAttribute(edgeFormData.getValue());
                            break;
                        case "Action":
                            if (nullCheck(edgeFormData.getValue()))
                                canadaFormModel.setAction(edgeFormData.getValue());
                            break;
                    }
                }
            }
        }
        return canadaFormModel;
    }


    private void loadMappingVal() {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(ResourceDetails.MAPPING_PATH);
            String data = IOUtils.toString(fis);
            JsonParser jsonParser = new JsonParser();
            mappingJson = (JsonObject) jsonParser.parse(data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

    }


    private void loadDefaultValue() {
        try {

            CsvToBean<DefaultData> csvToBean = new CsvToBeanBuilder(new FileReader(properties.getProperty("slvdata.csv.location")))
                    .withType(DefaultData.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            defaultDataHashMap = csvToBean.parse();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String getGeoZoneValue(String title) {
        logger.info("given idoncontroller BlockName is :" + title);
        logger.info("given list:" + gson.toJson(geoZoneDetailsList));
        GeoZoneDetails geoZoneDetails = new GeoZoneDetails();
        if (title.contains("Block")) {
            geoZoneDetails.setName(title);
        } else {
            geoZoneDetails.setName("Block " + title);
        }
        int pos = geoZoneDetailsList.indexOf(geoZoneDetails);
        System.out.println("idOnController position :" + pos);
        if (pos != -1) {
            geoZoneDetails = geoZoneDetailsList.get(pos);
            logger.info("given idoncontroller geozoneid is :" + geoZoneDetails.getId());
            System.out.println("geozoneid is :" + geoZoneDetails.getId());
            return geoZoneDetails.getId();
        }
        logger.info("given idoncontroller geozoneid is null");
        return null;
    }


    private void loadGeoZoneVal() {

        String mainUrl = properties.getProperty("streetlight.slv.url.main");
        String updateDeviceValues = properties.getProperty("streetlight.slv.geozone");
        String url = mainUrl + updateDeviceValues;
        ResponseEntity<String> response = slvRestService.getRequest(url, false);
        String responseString = response.getBody();

        Type listType = new TypeToken<ArrayList<GeoZoneDetails>>() {
        }.getType();

        geoZoneDetailsList = gson.fromJson(responseString, listType);
    }


    @Override
    public void createDevice(EdgeNote edgeNote, SlvSyncDetails slvSyncDetails, String geoZoneId, List<EdgeFormData> edgeFormDataList) throws DeviceCreationFailedException {
        try {
            String blockId = properties.getProperty("streetlight.edge.blockid");
            String blockName = valueById(edgeFormDataList, Integer.parseInt(blockId));
            System.out.println("BlockName" + blockName);
            // String blockName = valueById(edgeFormDataList, 32);
            logger.info("Block Name:" + blockName);
            logger.info("34 idoncontroller BlockName is :" + valueById(edgeFormDataList, 34));
            logger.info("32 idoncontroller BlockName is :" + valueById(edgeFormDataList, 32));
            geoZoneId = getGeoZoneValue(blockName);
            if (geoZoneId == null) {
                logger.error("No GeoZone");
                slvSyncDetails.setErrorDetails("No GeoZone");
                throw new DeviceCreationFailedException("Unable to find GeoZone");
            }

            super.createDevice(edgeNote, slvSyncDetails, geoZoneId, edgeFormDataList);
        } catch (NoValueException e) {
            throw new DeviceCreationFailedException("Unable to get block Name");
        }

    }

    private boolean nullCheck(String data) {
        return (data != null && data.length() > 0 && !data.contains("null")) ? true : false;
    }
}
