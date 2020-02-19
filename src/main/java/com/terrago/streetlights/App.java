package com.terrago.streetlights;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.terrago.streetlights.dao.TerragoDAO;
import com.terrago.streetlights.service.MonitorChanges;
import com.terrago.streetlights.service.UbicquiaLightsInterface;
import com.terrago.streetlights.utils.*;
import org.apache.log4j.Logger;

import java.util.List;


/**
 * Hello world!
 *
 */
public class App 
{
    private static Logger logger = Logger.getLogger(App.class);
    public static void main( String[] args ) {
        //LatLong latLong = LatLongUtils.getLatLngFromGeoJson("{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Point\",\"coordinates\":[ 104.8214, 38.8339 ]}}");
        //UbicquiaLightsInterface.requestDynamicToken();
        //JsonObject jsonObject = UbicquiaLightsInterface.getNodes("5c1919");
       // System.out.println(jsonObject.toString());52
        //UbicquiaLightsInterface.CreateNewNode("5c1920","27.6648","-81.5158");

       /* UbicquiaLightsInterface.requestDynamicToken();
        System.out.println(UbicquiaLightsInterface.getNodes("5c0690").toString());
        System.out.println(UbicquiaLightsInterface.getQueryData("5c0690"));
        System.out.println(UbicquiaLightsInterface.getQueryData("5c1914"));*/
        MonitorChanges monitorChanges = new MonitorChanges();
        monitorChanges.startMonitoring();
    }
        //UbicquiaLightsInterface.requestDynamicToken();
        //System.out.println("SI");
        //CreateRevision2 createRevision2 = new CreateRevision2();
        //System.out.println(args[0]);
       // createRevision2.createRevision(args[0]);
       /* MonitorChanges monitorChanges = new MonitorChanges();
        monitorChanges.startMonitoring();*/
        //UbicquiaLightsInterface.requestDynamicToken();

        /*
        String result1 = UbicquiaLightsInterface.getQueryData("5c0928c7920b29dd");
        System.out.println(result1);*/


        //UbicquiaLightsInterface.requestDynamicToken();
        //String setValue = "{\"id\":212,\"poleId\":\"14350\",\"fixtureId\":\"13221\",\"poleType\":\"Wood\",\"fixtureType\":\"Acorn\"}";
        //UbicquiaLightsInterface.setNodeData("212",setValue);
        /*
        String result1 = UbicquiaLightsInterface.getQueryData("5c407c784e49898a");
        System.out.println(result1);*/
        //UbicquiaLightsInterface.SetDevice("218",false);
        //String setValue = "{\"id\":218,\"poleId\":\"14321\",\"fixtureId\":\"12121\"}";
        //UbicquiaLightsInterface.setNodeData("218",setValue);

        /*
        String result1 = UbicquiaLightsInterface.getQueryData("5c0928c7920b29dd");
        String result2 = UbicquiaLightsInterface.getQueryData("5c407c784e49898a");
        System.out.println(result1);
        System.out.println(result2);
        String setValue = "{\"id\":218,\"poleType\":\"Aluminium\",\"fixtureType\":\"LED2\"}";
        UbicquiaLightsInterface.setNodeData("218",setValue);
        try {
            Thread.sleep(2000);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        UbicquiaLightsInterface.requestDynamicToken();
        String result21 = UbicquiaLightsInterface.getQueryData("5c0928c7920b29dd");
        System.out.println(result21);*/



    //}
    //CreateRevision createRevision = new CreateRevision();
    //createRevision.createRevision(args[0]);
    //System.out.println(args[0]);
        /*
        Gson gson = new Gson();
        UbicquiaLightsInterface.requestDynamicToken();
        String result1 = UbicquiaLightsInterface.getQueryData("947bbe81e7dba2da");
        if(result1 != null)
        {
            System.out.println(result1);
            JsonObject jsonObject = null;
            try {
                jsonObject = JsonDataParser.getJsonObject(result1);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            //jsonObject.addProperty("poleId","1535");
            //jsonObject.addProperty("fixtureId","1424");
            jsonObject.addProperty("poleType","");
            jsonObject.addProperty("fixtureType","");
            String strID = JsonDataParser.checkDataNull(jsonObject,"id");
            String result2 = jsonObject.toString();
                  //  jsonObject.getAsString();
            UbicquiaLightsInterface.setNodeData(strID,result2);
        }*/


        /*logger.info("Application Starts ....");
        MonitorChanges monitorChanges = new MonitorChanges();
        monitorChanges.startMonitoring();*/
}
