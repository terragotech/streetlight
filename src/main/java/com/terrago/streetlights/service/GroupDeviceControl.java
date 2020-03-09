package com.terrago.streetlights.service;

import com.terrago.streetlights.utils.LastUpdated;
import org.apache.log4j.Logger;

import java.util.List;

public class GroupDeviceControl implements Runnable {
    private Logger logger = Logger.getLogger(GroupDeviceControl.class);
    private Thread t;
    private List<String> lstID;
    private LastUpdated lastUpdated;
    public void run(){
        try {
            UbicquiaLightsInterface.requestDynamicToken();
            UbicquiaLightsInterface.SetMultipleDevice(lastUpdated,lstID,true);
            Thread.sleep(300000);
            UbicquiaLightsInterface.requestDynamicToken();
            UbicquiaLightsInterface.SetMultipleDevice(lastUpdated,lstID,false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public GroupDeviceControl(LastUpdated lastUpdated,List<String> lstID){
        this.lstID = lstID;
        this.lastUpdated = lastUpdated;
        t = new Thread(this);
        t.start();

    }
}
