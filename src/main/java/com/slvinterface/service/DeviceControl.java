package com.slvinterface.service;

public class DeviceControl implements Runnable {
    String nodeID;
    Thread t;
    public DeviceControl(String nodeID)
    {
        this.nodeID = nodeID;
        t = new Thread(this);
        t.start();
    }
    public void run(){
        NetSenseInterface.setLight(nodeID,true);
        try{
            Thread.sleep(300000);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        NetSenseInterface.setLight(nodeID,false);
    }
}
