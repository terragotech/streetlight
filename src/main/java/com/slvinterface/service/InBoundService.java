package com.slvinterface.service;

import com.slvinterface.utils.InBoundFileUtils;
import com.slvinterface.utils.ResourceDetails;

import java.io.File;
import java.util.Calendar;

public class InBoundService implements Runnable{
    Thread thread;
    public InBoundService()
    {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        String mutexFile = ResourceDetails.INBOUND_FILE_STORE+ File.separator+"startedprocess.txt";
        while(true)
        {
            Calendar rightNow = Calendar.getInstance();
            int hour = rightNow.get(Calendar.HOUR_OF_DAY);
            if(hour == 2 &&
                    !InBoundFileUtils.doesFileExists(mutexFile))
            {
                try {
                    InBoundFileUtils.createFile(mutexFile);
                    InBoundInterface inBoundInterface = new GlasGlowInBoundInterface();
                    inBoundInterface.addNewDevices();
                    inBoundInterface.updateDevices();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            if(hour != 2)
            {
                if(InBoundFileUtils.doesFileExists(mutexFile))
                {
                    InBoundFileUtils.deleteFile(mutexFile);
                }
            }
            try {
                Thread.sleep(300000);

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
