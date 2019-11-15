package com.slvinterface.service;

public class DeviceControl implements Runnable {
    String nodeID;
    Thread t;
    String lightLevel;
    String turnOnDuration;

    public DeviceControl(String nodeID,String lightLevel,String turnOnDuration)
    {
        this.nodeID = nodeID;
        this.lightLevel = lightLevel;
        this.turnOnDuration = turnOnDuration;
        t = new Thread(this);
        t.start();
    }
    public void run(){
        NetSenseInterface.setLight(nodeID,lightLevel,turnOnDuration,true);
        try{
            Thread.sleep(300000);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        NetSenseInterface.setLight(nodeID,"1","1",false);
    }
}
