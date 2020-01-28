package com.terrago.streetlights.service;

public class SingleLightDeviceControl implements Runnable {
    private String fixtureID;
    private Thread t;
    public SingleLightDeviceControl(String fixtureID)
    {
        this.fixtureID = fixtureID;
        t = new Thread(this);
        t.start();
    }
    public void run(){
        try{
            UbicquiaLightsInterface.requestDynamicToken();
            UbicquiaLightsInterface.SetDevice(fixtureID,true);
            Thread.sleep(300000);
            UbicquiaLightsInterface.requestDynamicToken();
            UbicquiaLightsInterface.SetDevice(fixtureID,false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
