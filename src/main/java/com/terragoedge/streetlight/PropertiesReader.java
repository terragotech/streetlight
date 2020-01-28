package com.terragoedge.streetlight;

import java.io.FileInputStream;
import java.util.Properties;

public class PropertiesReader {
    static Properties mainProperties = null;

    private PropertiesReader() {

    }

    private void init() {
        try {
            // to load application's properties, we use this class
            mainProperties = new Properties();
            FileInputStream file;


            //String path = "./src/main/resources/main.properties";
            String path = "./resources/main.properties";

            // load the file handle for main.properties
            file = new FileInputStream(path);

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
            PropertiesReader propertiesReader = new PropertiesReader();
            propertiesReader.init();
        }
        return mainProperties;
    }

}
