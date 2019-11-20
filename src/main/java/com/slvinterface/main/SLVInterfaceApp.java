package com.slvinterface.main;

import com.slvinterface.service.EdgeRestService;
import com.slvinterface.service.StreetLightCanadaService;

public class SLVInterfaceApp {
    public static void main(String []args)
    {
        try {
            StreetLightCanadaService streetLightCanadaService = new StreetLightCanadaService();
            streetLightCanadaService.run();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
