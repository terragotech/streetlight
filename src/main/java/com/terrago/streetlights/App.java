package com.terrago.streetlights;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.terrago.streetlights.dao.TerragoDAO;
import com.terrago.streetlights.json.TerragoData;
import com.terrago.streetlights.json.UbiData;
import com.terrago.streetlights.service.*;
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

        //String strDev = "UBC-20-0-01-NA-01-2-GY;2005DFA000090;7526E80F495CCAFB";

        //System.out.println(TerragoUtils.parseDevUI(strDev));
        //UbicquiaLightsInterface.requestDynamicToken();

        MonitorChanges monitorChanges = new LUSPilot();
        monitorChanges.startMonitoring2();
        /*MonitorChanges2 monitorChanges2 = new MonitorChanges2();
        monitorChanges2.startMonitoring();*/
    }

}
