package com.terragoedge.streetlight;

import com.terragoedge.streetlight.edgeinterface.ReadCSvservice;


public class StreetlightApp {
    public static void main(String[] args) {
        ReadCSvservice readCSvservice = new ReadCSvservice();
        readCSvservice.start();
    }

}
