package com.terrago.streetlights;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.terrago.streetlights.dao.TerragoDAO;
import com.terrago.streetlights.json.TerragoData;
import com.terrago.streetlights.json.UbiData;
import com.terrago.streetlights.service.MonitorChanges;
import com.terrago.streetlights.service.MonitorChanges2;
import com.terrago.streetlights.service.RESTService;
import com.terrago.streetlights.service.UbicquiaLightsInterface;
import com.terrago.streetlights.utils.*;
import com.terragoedge.edgeserver.EdgeFormData;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Hello world!
 *
 */

public class App 
{
    private static Logger logger = Logger.getLogger(App.class);
    private static String checkDataNull(JsonObject jsonObject,String key)
    {
        if(!jsonObject.get(key).isJsonNull() && jsonObject.get(key) != null)
        {
            return jsonObject.get(key).getAsString();
        }
        return "";
    }
    public static void main( String[] args ) {

        MonitorChanges monitorChanges = new MonitorChanges();
        monitorChanges.startMonitoring();
        /*MonitorChanges2 monitorChanges2 = new MonitorChanges2();
        monitorChanges2.startMonitoring();*/
    }

}
