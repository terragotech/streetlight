package com.terrago.streetlights.service;

import org.apache.log4j.Logger;

import java.util.List;

public class GroupDeviceControl implements Runnable {
    private Logger logger = Logger.getLogger(GroupDeviceControl.class);
    private Thread t;
    private List<String> lstID;
    public void run(){
        try {
            UbicquiaLightsInterface.requestDynamicToken();
            UbicquiaLightsInterface.SetMultipleDevice(lstID,true);
            Thread.sleep(300000);
            UbicquiaLightsInterface.requestDynamicToken();
            UbicquiaLightsInterface.SetMultipleDevice(lstID,false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public GroupDeviceControl(List<String> lstID){
        this.lstID = lstID;
        t = new Thread(this);
        t.start();

    }
}
