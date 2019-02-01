package com.terragoedge.slvinterface.service;

import com.terragoedge.slvinterface.dao.tables.SlvSyncDetails;
import com.terragoedge.slvinterface.enumeration.EdgeComponentType;
import com.terragoedge.slvinterface.exception.DeviceUpdationFailedException;
import com.terragoedge.slvinterface.exception.InValidBarCodeException;
import com.terragoedge.slvinterface.exception.NoValueException;
import com.terragoedge.slvinterface.json.slvInterface.ConfigurationJson;
import com.terragoedge.slvinterface.json.slvInterface.Id;
import com.terragoedge.slvinterface.model.EdgeFormData;
import com.terragoedge.slvinterface.model.EdgeNote;

import java.util.List;

public class SlvInterfaceServiceImpl extends SlvInterfaceService {


    @Override
    public void buildFixtureStreetLightData(String data, List<Object> paramsList, EdgeNote edgeNote)
            throws InValidBarCodeException {
        System.out.println("buildFixtureStreetLightData = " + data);
        String[] fixtureInfo = data.split(",");
        logger.info("Fixture QR Scan Val lenght" + fixtureInfo.length);
        if (fixtureInfo.length >= 12) {
            addStreetLightData("device.node.serialnumber", fixtureInfo[0], paramsList);
            /**
             * As per Mail conversion, In the older data, the luminaire model was the
             * shorter version of the fixture, so for the General Electric fixtures it was
             * ERLH. The Luminaire Part Number would be the longer more detailed number.
             */
            String partNumber = fixtureInfo[1].trim();
            String model = fixtureInfo[2].trim();
            /*if (fixtureInfo[1].trim().length() <= fixtureInfo[2].trim().length()) {
                model = fixtureInfo[1].trim();
                partNumber = fixtureInfo[2].trim();
            }*/
            addStreetLightData("categoryStrId", partNumber, paramsList);
            addStreetLightData("device.luminaire.drivermanufacturer", model, paramsList);
            addStreetLightData("ElexonChargeCode", fixtureInfo[3], paramsList);
            String powerVal = fixtureInfo[4];
            if (powerVal != null && !powerVal.isEmpty()) {
                powerVal = powerVal.replaceAll("W", "");
                powerVal = powerVal.replaceAll("w", "");
            }

            addStreetLightData("lampType", powerVal, paramsList);
            addStreetLightData("luminaire.brand", fixtureInfo[5], paramsList);
            // dailyReportCSV.setFixtureType(fixtureInfo[5]);
            addStreetLightData("device.luminaire.colortemp", fixtureInfo[6], paramsList);
            addStreetLightData("luminaire.model", fixtureInfo[7], paramsList);
            addStreetLightData("device.luminaire.manufacturedate", fixtureInfo[8], paramsList);
            addStreetLightData("device.luminaire.partnumber", fixtureInfo[9], paramsList);
            addStreetLightData("luminaire.type", fixtureInfo[10], paramsList);
            addStreetLightData("power", fixtureInfo[11], paramsList);
//            addStreetLightData("ballast.dimmingtype", fixtureInfo[12], paramsList);
            System.out.println("steetlight processed data" + paramsList);

        } else {
            throw new InValidBarCodeException(
                    "Fixture MAC address is not valid (" + edgeNote.getTitle() + "). Value is:" + data);
        }
    }


    public void processSetDevice(List<EdgeFormData> edgeFormDataList, ConfigurationJson configurationJson, EdgeNote edgeNote, List<Object> paramsList, SlvSyncDetails slvSyncDetails, String controllerStrIdValue) throws NoValueException, DeviceUpdationFailedException {
        //setValues and Empty ReplaceOLC
        List<Id> idList = configurationJson.getIds();
        // Process Fixture value
        Id fixureID = getIDByType(idList, EdgeComponentType.FIXTURE.toString());
        if (fixureID != null) {
            processFixtureScan(edgeFormDataList, fixureID, edgeNote, paramsList, slvSyncDetails);
        }
        paramsList.add("idOnController=" + edgeNote.getTitle());
        paramsList.add("controllerStrId=" + controllerStrIdValue);
        String nodeTypeStrId = properties.getProperty("streetlight.slv.equipment.type");
        paramsList.add("nodeTypeStrId=" + nodeTypeStrId);
        addStreetLightData("nodeTypeStrId", nodeTypeStrId, paramsList);
        addOtherParams(edgeNote, paramsList);
        setDeviceValues(paramsList, slvSyncDetails);

    }


}
