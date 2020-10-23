package com.terrago.streetlights.service;

import com.terrago.streetlights.utils.LastUpdated;

public class SingleLightDeviceControl implements Runnable {
    protected String fixtureID;
    protected Thread t;
    protected LastUpdated lastUpdated;
    private boolean isCompleted;

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public SingleLightDeviceControl(LastUpdated lastUpdated, String fixtureID)
    {
        isCompleted = false;
        this.fixtureID = fixtureID;
        this.lastUpdated = lastUpdated;
        t = new Thread(this);
        t.start();
    }
    public void run(){
        try{

                UbicquiaLightsInterface.requestDynamicToken();
                UbicquiaLightsInterface.SetDevice(lastUpdated, fixtureID, true);
                //Thread.sleep(300000);
                Thread.sleep(10000);
                UbicquiaLightsInterface.requestDynamicToken();
                UbicquiaLightsInterface.SetDevice(lastUpdated, fixtureID, false);
                Thread.sleep(10000);
                isCompleted = true;

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
