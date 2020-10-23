package com.terrago.streetlights.service;

import com.terrago.streetlights.utils.LastUpdated;
import com.terrago.streetlights.utils.PropertiesReader;
import org.apache.log4j.Logger;

public class CycleSingleLightDeviceControl implements Runnable{
    private Logger logger = Logger.getLogger(CycleSingleLightDeviceControl.class);
    protected String fixtureID;
    protected Thread t;
    protected LastUpdated lastUpdated;
    public CycleSingleLightDeviceControl(LastUpdated lastUpdated, String fixtureID)
    {
        this.fixtureID = fixtureID;
        this.lastUpdated = lastUpdated;
        t = new Thread(this);
        t.start();
    }
    public void run(){
        String strLightCycle = PropertiesReader.getProperties().getProperty("lightcycle");
        String strLightCycleRep = PropertiesReader.getProperties().getProperty("lightcyclerep");
        int n = Integer.parseInt(strLightCycleRep);
        for(int idx=0;idx<n;idx++) {
            try {
                logger.info("cycle interation : " + idx);
                UbicquiaLightsInterface.requestDynamicToken();
                logger.info("Requesting light ON");
                UbicquiaLightsInterface.SetDevice(lastUpdated, fixtureID, true);
                logger.info("Going to wait state");
                Thread.sleep(Long.parseLong(strLightCycle));
                logger.info("Requesting light OFF");
                UbicquiaLightsInterface.requestDynamicToken();
                UbicquiaLightsInterface.SetDevice(lastUpdated, fixtureID, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
