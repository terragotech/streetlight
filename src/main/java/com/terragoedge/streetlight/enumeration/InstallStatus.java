package com.terragoedge.streetlight.enumeration;

public enum InstallStatus {
    Photocell_Only("Photocell Only"),Node_Only("Node Only"),Installed("Installed"),To_be_installed("To be installed"),Removed("Removed"),Pole_Knocked_Down("Pole Knocked Down"),Verified("Verified"),Installation_Removed("Installation Removed"),To_be_verified("To be verified");

    String val;
    public String getValue(){
        return val;
    }
    InstallStatus(String val){
        this.val = val;
    }
}
