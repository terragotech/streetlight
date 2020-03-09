package com.terrago.streetlights.service;

import com.terrago.streetlights.utils.LastUpdated;

public class SingleLightDeviceControl implements Runnable {
    private String fixtureID;
    private Thread t;
    private LastUpdated lastUpdated;
    public SingleLightDeviceControl(LastUpdated lastUpdated, String fixtureID)
    {
        this.fixtureID = fixtureID;
        this.lastUpdated = lastUpdated;
        t = new Thread(this);
        t.start();
    }
    public void run(){
        try{
            UbicquiaLightsInterface.requestDynamicToken();
            UbicquiaLightsInterface.SetDevice(lastUpdated,fixtureID,true);
            Thread.sleep(300000);
            UbicquiaLightsInterface.requestDynamicToken();
            UbicquiaLightsInterface.SetDevice(lastUpdated,fixtureID,false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
