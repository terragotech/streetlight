package com.terragoedge.streetlight;

import java.io.FileInputStream;
import java.util.Properties;

public class PropertiesReader {
	static Properties mainProperties = null;
	String path = null;

	private PropertiesReader(String path) {
		this.path = path;
	}

	private void init() {
		try {
			// to load application's properties, we use this class
			mainProperties = new Properties();
			FileInputStream file;
			
			// load the file handle for main.properties
			file = new FileInputStream(path+"/main.properties");

			// load all the properties from this file
			mainProperties.load(file);

			// we have loaded the properties, so close the file handle
			file.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static Properties getProperties(String path) {
		if (mainProperties == null) {
			PropertiesReader propertiesReader = new PropertiesReader(path);
			propertiesReader.init();
		}
		return mainProperties;
	}

}
