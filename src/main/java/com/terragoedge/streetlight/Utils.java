package com.terragoedge.streetlight;

import com.terragoedge.streetlight.enumeration.FixtureCodeType;

public class Utils {

    public static String getAtlasPage(String atlasPage){
        atlasPage = atlasPage.replaceAll("-","");
        if(atlasPage.length() > 4){
            return atlasPage.substring(0,4);
        }else{
            return atlasPage;
        }
    }

    public static String getAtlasGroup(String proposedContext){
        if(proposedContext.contains("Alley")){
            return "AL";
        }else if(proposedContext.contains("Viaduct")){
            return "VI";
        }else{
            return "00";
        }
    }

    public static String getFixtureCode(String fixtureCode){
        if(fixtureCode.equals("0")){
            return FixtureCodeType.OTHER.getValue();
        }else{
            FixtureCodeType fixtureCodeType = FixtureCodeType.get(fixtureCode);
            if(fixtureCodeType == null){
                return FixtureCodeType.OTHER.getValue();
            }
            return fixtureCodeType.getValue();
        }
    }
}
