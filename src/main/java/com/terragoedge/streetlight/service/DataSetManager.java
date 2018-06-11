package com.terragoedge.streetlight.service;

import com.terragoedge.edgeserver.AddressSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DataSetManager {

   private static Map<String,Set<String>> macAddressHolder = new HashMap<>();

    private static HashSet<AddressSet> addressSets = new HashSet<>();


   public static void reset(){
       macAddressHolder = new HashMap<>();
   }

    public static Map<String, Set<String>> getMacAddressHolder() {
        return macAddressHolder;
    }

    public static void setMacAddressHolder(Map<String, Set<String>> macAddressHolder) {
        DataSetManager.macAddressHolder = macAddressHolder;
    }

    public static HashSet<AddressSet> getAddressSets() {
        return addressSets;
    }

    public static void setAddressSets(HashSet<AddressSet> addressSets) {
        DataSetManager.addressSets = addressSets;
    }
}
