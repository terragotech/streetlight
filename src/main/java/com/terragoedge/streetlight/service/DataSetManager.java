package com.terragoedge.streetlight.service;

import com.terragoedge.edgeserver.AddressSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DataSetManager {

   private static Map<String,Set<String>> macAddressHolder = new HashMap<>();

   public static void reset(){
       macAddressHolder = new HashMap<>();
   }

    public static Map<String, Set<String>> getMacAddressHolder() {
        return macAddressHolder;
    }

    public static void setMacAddressHolder(Map<String, Set<String>> macAddressHolder) {
        DataSetManager.macAddressHolder = macAddressHolder;
    }
}
