package com.terragoedge.streetlight;

import com.terragoedge.streetlight.enumeration.FixtureCodeType;

public class Utils {

    public static String getAtlasPage(String atlasPage){
        return atlasPage.replaceAll("-","");
    }

    public static String getAtlasGroup(String proposedContext){
        switch (proposedContext){
            case "Alley":
                return "AL";
            case "Viaduct":
                return "VI";
            default:
                return "00";
        }
    }

    public static String getFixtureCode(String fixtureCode){
        if(fixtureCode.equals("0")){
            return FixtureCodeType.OTHER.getValue();
        }else{
            FixtureCodeType fixtureCodeType = FixtureCodeType.get(fixtureCode);
            if(fixtureCodeType == null){
                return null;
            }
            return fixtureCodeType.getValue();
        }
    }
}
