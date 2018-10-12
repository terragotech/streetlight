package com.terragoedge.streetlight;

import com.terragoedge.streetlight.service.InstallDateFix;
import com.terragoedge.streetlight.service.SlvService;
import com.terragoedge.streetlight.service.StreetlightChicagoService;
import com.terragoedge.streetlight.service.TalkAddressService;


public class StreetlightApp {


	public static void main(String[] args) {
        while (true){
            try{
                StreetlightChicagoService streetlightChicagoService = new StreetlightChicagoService();
                streetlightChicagoService.run();
                Thread.sleep(50000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
	}

}
