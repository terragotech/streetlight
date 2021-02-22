package com.terragoedge.slvinterface;

import com.terragoedge.slvinterface.service.TitleChangeListener;
import org.apache.log4j.Logger;

public class MainApp {
    private static Logger logger = Logger.getLogger(MainApp.class);
    public static void main(String[] args) {
        TitleChangeListener titleChangeListener = new TitleChangeListener();
        try {
            while (true) {
                titleChangeListener.start();
                Thread.sleep(30 * 1000);// 30 secs
            }
        }catch (Exception e){
           logger.error("Error while starting: "+e.getMessage());
        }
    }

}
