package com.terragoedge.streetlight;

import org.apache.log4j.Logger;

import java.nio.file.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LogWatcher {

    private static final Logger logger = Logger.getLogger(LogWatcher.class);

    private static long lastModifiedTime = 0;
    private static long lastMailTime = 0;

    public static void main(String[] r){
        LogWatcher logWatcher = new LogWatcher();
        logWatcher.watchLogFile();
    }
    public void watchLogFile(){
        try{
            // Get the Config details from the Properties file

            String logFilePath = PropertiesReader.getProperties().getProperty("logwatcher.log.filepath");
            Long timeInterval = Long.valueOf(PropertiesReader.getProperties().getProperty("logwatcher.log.nodata.timeinterval")) * 1000;
            String logFileName =  PropertiesReader.getProperties().getProperty("logwatcher.log.filename");

            Long mailTimeInterval = Long.valueOf(PropertiesReader.getProperties().getProperty("logwatcher.log.nodata.mail.timeinterval")) * 1000 * 60;

            Path path = Paths.get(logFilePath);
            // Initialize the Watch Service
            WatchService watchService =  path.getFileSystem().newWatchService();
            // Register the file Change Event
            path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            //Set Current Time as Initial File Modified Time.
            lastModifiedTime = System.currentTimeMillis();

            WatchKey watchKey = null;
            while (true) {
                // Poll Every 30 seconds and check file is modified or not
                watchKey = watchService.poll(30, TimeUnit.SECONDS);
                if(watchKey != null) {
                    // Get all the File which are updated
                    List<WatchEvent<?>> watchEventList = watchKey.pollEvents();
                    for(WatchEvent watchEvent : watchEventList){
                        final Path changed = (Path) watchEvent.context();
                        // If the Given file is modified, then update LastModified Time
                        if (changed.endsWith(logFileName)) {
                            lastModifiedTime = System.currentTimeMillis();
                        }
                    }
                    // Reset the Watch Key
                    watchKey.reset();
                }

                // Check the Difference, if it goes beyond threshold then triggers the email.
                long timeDiff = System.currentTimeMillis() - lastModifiedTime;
                logger.info("Data Time Diff:"+timeDiff);
                if(timeDiff >= timeInterval){
                    long mailTimeDiff = System.currentTimeMillis() - lastMailTime;
                    logger.info("Mail Time Diff:"+mailTimeDiff);
                    if(mailTimeDiff > mailTimeInterval){
                        lastMailTime = System.currentTimeMillis();

                        String serverUrl = PropertiesReader.getProperties().getProperty("com.edge.url.slvinterface.down.url");
                        String subject = PropertiesReader.getProperties().getProperty("com.edge.url.slvinterface.subject");
                        String body = PropertiesReader.getProperties().getProperty("com.edge.url.slvinterface.body");
                        String to = PropertiesReader.getProperties().getProperty("com.edge.url.slvinterface.to");
                        try{
                            StreetlightApp.serverCall(serverUrl, subject,body,to);
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        // Need to trigger the mail
                    }

                }

            }

        }catch (Exception e){
            logger.error("Error in watchLogFile",e);
        }
    }
}
