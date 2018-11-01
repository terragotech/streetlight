package com.terragoedge.streetlight;

import com.terragoedge.streetlight.edgeinterface.ReadCSvservice;

import java.text.SimpleDateFormat;
import java.util.Date;


public class StreetlightApp {


    public static void main(String[] args) {
        ReadCSvservice readCSvservice = new ReadCSvservice();
        readCSvservice.start();
    }

}
