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
import com.terragoedge.slvinterface.model.EdgeFormData;
import com.terragoedge.slvinterface.model.EdgeNote;
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
        addStreetLightData("location.utillocationid", edgeNote.getTitle()+".Lamp", paramsList);
    }

    @Override
    public void processSetDevice(List<EdgeFormData> edgeFormDataList, ConfigurationJson configurationJson, EdgeNote edgeNote, List<Object> paramsList, SlvSyncDetails slvSyncDetails, String controllerStrIdValue) throws NoValueException, DeviceUpdationFailedException {
        paramsList.add("idOnController=" + edgeNote.getTitle());
        addStreetLightData("idOnController", edgeNote.getTitle(), paramsList);
        paramsList.add("controllerStrId=" + controllerStrIdValue);
        addOtherParams(edgeNote, paramsList);
        updateDeviceValues(paramsList, edgeNote,edgeFormDataList);
        setDeviceValues(paramsList, slvSyncDetails);
    }



    public void updateDeviceValues(List<Object> paramsList, EdgeNote edgeNote,
                                   List<EdgeFormData> edgeFormValuesList) throws NoValueException{
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


                if(!key.equals("comment") && !key.equals("MacAddress") && !key.toLowerCase().equals("power")){
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
                }else {
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


        addStreetLightData("location.utillocationid",  edgeNote.getTitle()+ ".Lamp", paramsList);
        String nodeTypeStrId = properties.getProperty("streetlight.slv.equipment.type");
        addStreetLightData("modelFunctionId", nodeTypeStrId, paramsList);

        addStreetLightData("comment", comment, paramsList);

        String streetLightDate = dateFormat(edgeNote.getCreatedDateTime());
        addStreetLightData("lamp.installdate", streetLightDate, paramsList);
        addStreetLightData("location.locationtype", "LOCATION_TYPE_PREMISE", paramsList);

        DefaultData defaultData = new DefaultData();
        defaultData.setIdOnController(edgeNote.getTitle());
        int pos = defaultDataHashMap.indexOf(defaultData);
        if(pos != -1){
            defaultData = defaultDataHashMap.get(pos);
            addStreetLightData("DimmingGroupName", defaultData.getDimmingGroupValue(), paramsList);
        }

        }





    private void loadMappingVal(){
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(ResourceDetails.MAPPING_PATH);
            String data =  IOUtils.toString(fis);
            JsonParser jsonParser = new JsonParser();
            mappingJson = (JsonObject) jsonParser.parse(data);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(fis != null){
                try{
                    fis.close();
                }catch (Exception e){
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


    public String getGeoZoneValue(String title){
        GeoZoneDetails geoZoneDetails = new GeoZoneDetails();
        geoZoneDetails.setName(title);
        int pos =  geoZoneDetailsList.indexOf(geoZoneDetails);
        if(pos != -1){
            geoZoneDetails = geoZoneDetailsList.get(pos);
            return geoZoneDetails.getId();
        }
        return null;
    }


    private void loadGeoZoneVal(){

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
    public void createDevice(EdgeNote edgeNote, SlvSyncDetails slvSyncDetails, String geoZoneId,List<EdgeFormData> edgeFormDataList) throws DeviceCreationFailedException {
        try{
            String blockName = valueById(edgeFormDataList,34);

            blockName = "Block "+blockName;
            logger.info("Block Name:"+blockName);
            geoZoneId = getGeoZoneValue(blockName);
            if(geoZoneId == null){
                logger.error("No GeoZone");
                throw new DeviceCreationFailedException("Unable to find GeoZone");
            }

            super.createDevice(edgeNote,slvSyncDetails,geoZoneId,edgeFormDataList);
        }catch (NoValueException e){
            throw new DeviceCreationFailedException("Unable to get block Name");
        }

    }

}
