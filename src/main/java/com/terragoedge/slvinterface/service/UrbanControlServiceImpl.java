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

public class UrbanControlServiceImpl extends SlvInterfaceService  {

    //DW Windsor,UC000000017519,Libra retrofit tray,Philips,SR,DALI LOG,LED,20W,3000k,

    @Override
    public void buildFixtureStreetLightData(String data, List<Object> paramsList, EdgeNote edgeNote)
            throws InValidBarCodeException {
        String[] fixtureInfo = data.split(",");
        if (fixtureInfo.length >= 9) {
            addStreetLightData("luminaire.brand", fixtureInfo[0], paramsList);
            addStreetLightData("device.luminaire.partnumber", fixtureInfo[1], paramsList);
            addStreetLightData("luminaire.model", fixtureInfo[2], paramsList);
            addStreetLightData("ballast.brand", fixtureInfo[3], paramsList);
            addStreetLightData("ballast.type", fixtureInfo[4], paramsList);
            addStreetLightData("ballast.dimmingtype", fixtureInfo[5], paramsList);
            addStreetLightData("luminaire.distributiontype", fixtureInfo[6], paramsList);
            addStreetLightData("luminaire.DistributionType", fixtureInfo[6], paramsList);

            String powerVal = fixtureInfo[7];
            if (powerVal != null && !powerVal.isEmpty()) {
                powerVal = powerVal.replaceAll("W", "");
                powerVal = powerVal.replaceAll("w", "");
            }


            addStreetLightData("power", powerVal, paramsList);
            addStreetLightData("device.luminaire.colortemp", fixtureInfo[8], paramsList);

        }

    }


    // replaceOLC(controllerStrIdValue, edgeNote.getTitle(), newNodeMacAddress);
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
        //String nodeTypeStrId = properties.getProperty("streetlight.slv.equipment.type");
       // paramsList.add("nodeTypeStrId=" + nodeTypeStrId);
       // addStreetLightData("nodeTypeStrId", nodeTypeStrId, paramsList);
        addOtherParams(edgeNote, paramsList);
        setDeviceValues(paramsList, slvSyncDetails);

    }
}
