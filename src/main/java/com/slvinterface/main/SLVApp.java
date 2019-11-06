package com.slvinterface.main;

import com.slvinterface.service.*;
import org.apache.log4j.Logger;

public class SLVApp {

    private static final Logger logger = Logger.getLogger(SLVApp.class);
    public static void main(String args[])
    {
        //NetSenseInterface.setLight("015322000006412",false);
        NetSenseInterface netSenseInterface = new NetSenseInterface();
        netSenseInterface.start();
        /*JsonObject jsonObject = NetSenseInterface.getNodeDetails("015322000006412");
        String fixtureID = jsonObject.get("fixtureid").getAsString();
        JsonObject jsonObject1 = NetSenseInterface.getFixtureDetails(fixtureID);*/

    }
}
