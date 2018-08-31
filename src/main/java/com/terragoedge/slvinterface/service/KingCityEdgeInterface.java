package com.terragoedge.slvinterface.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

import com.google.gson.reflect.TypeToken;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.terragoedge.slvinterface.dao.tables.SlvSyncDetails;
import com.terragoedge.slvinterface.exception.InValidBarCodeException;
import com.terragoedge.slvinterface.exception.NoValueException;
import com.terragoedge.slvinterface.json.slvInterface.ConfigurationJson;
import com.terragoedge.slvinterface.kingcity.DefaultData;
import com.terragoedge.slvinterface.kingcity.GeoZoneDetails;
import com.terragoedge.slvinterface.model.EdgeNote;
import com.terragoedge.slvinterface.utils.Utils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.terragoedge.slvinterface.exception.DeviceUpdationFailedException;
import com.terragoedge.slvinterface.model.EdgeFormData;
import com.terragoedge.slvinterface.model.NoteDetails;
import com.terragoedge.slvinterface.model.SLVDataEntity;
import com.terragoedge.slvinterface.utils.PropertiesReader;

public class KingCityEdgeInterface extends SlvInterfaceService{

	JsonObject mappingJson = null;
	List<DefaultData> defaultDataHashMap = new ArrayList<>();
    List<GeoZoneDetails> geoZoneDetailsList = new ArrayList<>();


	public KingCityEdgeInterface() {
	    super();
        loadDefaultValue();
        loadGeoZoneVal();
        loadMappingVal();
	}

	@Override
	public void buildFixtureStreetLightData(String data, List<Object> paramsList, EdgeNote edgeNote) throws InValidBarCodeException {

	}

	public void updateDeviceValues(List<Object> paramsList, EdgeNote edgeNote,
								   List<EdgeFormData> edgeFormValuesList) throws NoValueException{
		String comment = "";
		for (EdgeFormData formValues : edgeFormValuesList) {
			JsonElement streetLightKey = mappingJson.get(formValues.getLabel());
			if (streetLightKey != null && !streetLightKey.isJsonNull()) {
				String key = streetLightKey.getAsString();
				String value = formValues.getValue();
				if(!key.equals("comment") && !key.equals("MacAddress") && !key.toLowerCase().equals("power")){
					addStreetLightData(key, value, paramsList);
				}

				switch (key) {


				case "location.mapnumber":
					value = formValues.getValue();
					String tt = "Block " + value;
					//addStreetLightData(key, value, slvDataEntity.getParamsList());
					break;

				case "comment":
					comment = comment + " " + formValues.getLabel() + ":" + formValues.getValue();
					break;



				default:
					break;
				}
			}

		}

		DefaultData defaultData = new DefaultData();
        defaultData.setIdOnController(edgeNote.getTitle());
        int pos = defaultDataHashMap.indexOf(defaultData);
        if(pos != -1){
            defaultData = defaultDataHashMap.get(pos);
            addStreetLightData("power", defaultData.getWattage(), paramsList);
            addStreetLightData("powerCorrection", defaultData.getWattage(), paramsList);
            addStreetLightData("DimmingGroupName", defaultData.getDimmingGroupValue(), paramsList);
            addStreetLightData("address", edgeNote.getDescription(), paramsList);
            //address

            String modelFunctionId = properties.getProperty("streetlight.slv.modelfuncion.id");
            addStreetLightData("modelFunctionId", modelFunctionId, paramsList);

            addStreetLightData("comment", comment, paramsList);
            addStreetLightData("location.locationtype", "LOCATION_TYPE_PREMISE", paramsList);

            String streetLightDate = dateFormat(edgeNote.getCreatedDateTime());
            addStreetLightData("lamp.installdate", streetLightDate, paramsList);
        }else{
            throw  new NoValueException("Dimming Group Value not found.");
        }







	}

    @Override
    public void processSetDevice(List<EdgeFormData> edgeFormDataList, ConfigurationJson configurationJson, EdgeNote edgeNote, List<Object> paramsList, SlvSyncDetails slvSyncDetails, String controllerStrIdValue) throws NoValueException, DeviceUpdationFailedException {
        paramsList.add("idOnController=" + edgeNote.getTitle());
        paramsList.add("controllerStrId=" + controllerStrIdValue);
        addStreetLightData("idOnController", edgeNote.getTitle(), paramsList);
        addOtherParams(edgeNote, paramsList);
        updateDeviceValues(paramsList, edgeNote,edgeFormDataList);
        setDeviceValues(paramsList, slvSyncDetails);
    }




    public void addOtherParams(EdgeNote edgeNote, List<Object> paramsList) {
        addStreetLightData("install.date", Utils.dateFormat(edgeNote.getCreatedDateTime()), paramsList);
        addStreetLightData("installStatus", "Installed", paramsList);
        addStreetLightData("location.utillocationid", edgeNote.getTitle()+".Lamp", paramsList);
    }

    private String dateFormat(Long dateTime) {
		Date date = new Date(Long.valueOf(dateTime));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dff = dateFormat.format(date);
		return dff;
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
        DefaultData defaultData = new DefaultData();
        defaultData.setIdOnController(title);
        int pos = defaultDataHashMap.indexOf(defaultData);
        if(pos != -1) {
            defaultData = defaultDataHashMap.get(pos);
            String geoZoneVal =  defaultData.getGeoZoneValue();
            GeoZoneDetails geoZoneDetails = new GeoZoneDetails();
            geoZoneDetails.setName(geoZoneVal);
            pos =  geoZoneDetailsList.indexOf(geoZoneDetails);
            if(pos != -1){
                geoZoneDetails = geoZoneDetailsList.get(pos);
               return geoZoneDetails.getId();
            }
        }
        return null;
    }


    private void loadMappingVal(){
        FileInputStream fis = null;
	    try{
             fis = new FileInputStream("./src/main/resources/kingcity/mapping.json");
            String data =  IOUtils.toString(fis);
            JsonParser jsonParser = new JsonParser();
            mappingJson = (JsonObject) jsonParser.parse(data);
        }catch (Exception e){
e.printStackTrace();
        }

    }

}
