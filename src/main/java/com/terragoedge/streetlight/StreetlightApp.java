package com.terragoedge.streetlight;

import com.terragoedge.streetlight.service.InstallDateFix;
import com.terragoedge.streetlight.service.SlvService;
import com.terragoedge.streetlight.service.StreetlightChicagoService;
import com.terragoedge.streetlight.service.TalkAddressService;


public class StreetlightApp {



    public static void  main(String[] rr){
        StreetlightChicagoService streetlightChicagoService = new StreetlightChicagoService();
        streetlightChicagoService.run();
    }


	public static void main_1(String[] args) {
	    while (true){
            StreetlightChicagoService streetlightChicagoService = null;
            try{
                streetlightChicagoService = new StreetlightChicagoService();
                //streetlightChicagoService.loadDevices();
                streetlightChicagoService.run();
                Thread.sleep(30000);
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                streetlightChicagoService.closeConnection();
            }
        }
	}

}
