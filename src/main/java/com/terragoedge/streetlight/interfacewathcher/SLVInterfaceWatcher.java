package com.terragoedge.streetlight.interfacewathcher;

import com.terragoedge.streetlight.PropertiesReader;
import com.terragoedge.streetlight.StreetlightApp;
import org.apache.log4j.Logger;

public class SLVInterfaceWatcher {
    private static final Logger logger = Logger.getLogger(SLVInterfaceWatcher.class);

    public void doProcess(){
        // Set Initially mailSent to false, so that it will send first time
        boolean mailSent = false;
        // Current time in Last Mail Time
        long lastMailTime = System.currentTimeMillis();
       while (true){
           try{
                // Get Config Values
               Long noDataTimeInterval = Long.valueOf(PropertiesReader.getProperties().getProperty("slvinterface.watcher.nodata.timeinterval"));
               Long mailIntervalTime = Long.valueOf(PropertiesReader.getProperties().getProperty("slvinterface.watcher.nodata.mail.timeinterval")) * 1000;
               // Get SLVInterface last Update time from DB
              Long slvInterfaceTime =  ConnectionDAO.INSTANCE.getSlvInterfaceLastRunTime();

              //Process only > 0 and SLVInterface is not null
              if(slvInterfaceTime != null && slvInterfaceTime > 0){
                  // Get the Diff
                 long diff =  System.currentTimeMillis() - slvInterfaceTime;
                 // If Diff is greater than threshold value, then trigger the email
                 if(diff >= noDataTimeInterval){
                     // If its already send mail, then need to wait next turn(based on config, so that we can avoid continuous mail)
                     if(mailSent){
                         // Get Mail Diff
                      long mailDiff =   System.currentTimeMillis() - lastMailTime;
                      // If the Mail Diff is greater than threshold, then trigger the mail
                      if(mailDiff >= mailIntervalTime){
                            // Sent Mail
                          sendMail();
                          // Reset the flag
                          mailSent = true;
                          lastMailTime = System.currentTimeMillis();
                      }
                     }else{
                         // Sent Mail
                         sendMail();
                         // Reset the Flag
                         mailSent = true;
                         lastMailTime = System.currentTimeMillis();
                     }
                 }else{
                     mailSent = false;
                 }
              }else{
                  mailSent = false;
              }
           }catch (Exception e){
               logger.error("Error in doProcess",e);
               mailSent = true;
               lastMailTime = System.currentTimeMillis();
           }
           try {
               Thread.sleep(30000);
           }catch (Exception e){
               logger.error("Error in doProcess",e);
           }

       }

    }

    private void sendMail(){
        String serverUrl = PropertiesReader.getProperties().getProperty("com.edge.url.slvinterface.down.url");
        String subject = PropertiesReader.getProperties().getProperty("com.edge.url.slvinterface.subject");
        String body = PropertiesReader.getProperties().getProperty("com.edge.url.slvinterface.body");
        String to = PropertiesReader.getProperties().getProperty("com.edge.url.slvinterface.to");
        try{
            StreetlightApp.serverCall(serverUrl, subject,body,to);
        }catch (Exception e){
            logger.error("Error in doProcess",e);
        }
    }
}
