package com.terragoedge.slvinterface.service;

import com.terragoedge.slvinterface.model.JPSWorkflowModel;

public class SlvService {
    public SlvService() {
    }

    public void processSlv(JPSWorkflowModel jpsWorkflowModel){
        // search idoncontroller on slv
        if(true){// device already present in slv
            processDeviceValues(jpsWorkflowModel,true);
            processMacAddress(jpsWorkflowModel);
        }else{// device not present in slv
            // create device in slv
            // save or update slvdevice in local db
            processDeviceValues(jpsWorkflowModel,false);
            processMacAddress(jpsWorkflowModel);
        }
    }

    private void processDeviceValues(JPSWorkflowModel jpsWorkflowModel,boolean devicePresentInSlv){
        if(devicePresentInSlv) {
            // get device values from local dp and check
            if(false){
                // call set device values
                // save or update slvSyncDetail in local db
            }
        }else{
            // call set device values
            // save or update slvSyncDetail in local db
        }

    }

    private void processMacAddress(JPSWorkflowModel jpsWorkflowModel){
        // check mac address already present
        if(true){
            // save duplicate mac address to local db
        }else{
            // check device has another mac
            if(true){
                // replace ylc with empty macaddress
                // replace ylc with macaddress
            }else{
                // replace ylc with macaddress
            }
        }
    }
}
