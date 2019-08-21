package com.terragoedge.streetlight;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PingIP {

    private static final Logger logger = Logger.getLogger(PingIP.class);

    public static boolean runSystemCommand(String command) {

        try {
            Process p = Runtime.getRuntime().exec("ping " +command);
            BufferedReader inputStream = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));

            String s = "";
            // reading output stream of the command
            int i = 0;
            while ((s = inputStream.readLine()) != null) {
                logger.info(s);
                i = i + 1;
                if (i > 5) {
                    if (s.startsWith("Request timeout")) {
                        return false;
                    } else {
                        return true;
                    }
                }

            }

        } catch (Exception e) {
            logger.error("Error in runSystemCommand",e);
        }
        return false;
    }

    public static void main(String[] args) {

        String ip = "199.233.240.10";
        System.out.println(runSystemCommand("ping " + ip));


    }
}
