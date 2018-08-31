package com.terragoedge.slv2edge.metering.util;

import java.io.FileInputStream;
import java.util.Properties;

public class MeteringPropertiesReader {
    static Properties mainProperties = null;

    private MeteringPropertiesReader() {

    }

    private void init() {
        try {
            // to load application's properties, we use this class
            mainProperties = new Properties();

            String path = "./src/main/resources/kingcity/main.properties";
            //String path = "./resources/main.properties";

            // load the file handle for main.properties
            FileInputStream file = new FileInputStream(path);

            // load all the properties from this file
            mainProperties.load(file);

            // we have loaded the properties, so close the file handle
            file.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static Properties getProperties() {
        if (mainProperties == null) {
            MeteringPropertiesReader propertiesReader = new MeteringPropertiesReader();
            propertiesReader.init();
        }
        return mainProperties;
    }


}
