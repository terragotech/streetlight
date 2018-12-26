package com.terragoedge.streetlight;

import com.terragoedge.streetlight.service.InstallDateFix;
import com.terragoedge.streetlight.service.SlvService;
import com.terragoedge.streetlight.service.StreetlightChicagoService;
import com.terragoedge.streetlight.service.TalkAddressService;


public class StreetlightApp {


	public static void main(String[] args) {
	    while (true){
            StreetlightChicagoService streetlightChicagoService = null;
            try{
                streetlightChicagoService = new StreetlightChicagoService();
                streetlightChicagoService.loadDevices();
                streetlightChicagoService.run();
                Thread.sleep(60000);
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                // streetlightChicagoService.closeConnection();
            }
        }
	}

}
