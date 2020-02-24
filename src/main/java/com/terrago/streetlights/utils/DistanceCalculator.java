package com.terrago.streetlights.utils;

public class DistanceCalculator {
    public static Double getDistance(LatLong2 el,LatLong2 sl)
    {
        Double result = null;
        double R = 6371E+3;
        double P1 = Math.toRadians(el.lat);
        double P2 = Math.toRadians(sl.lat);
        double dP = Math.toRadians(sl.lat - el.lat);
        double dL = Math.toRadians(sl.lng - el.lng);
        double a = Math.sin(dP/2) * Math.sin(dP/2) + Math.cos(P1) * Math.cos(P2) * Math.sin(dL/2) * Math.sin(dL/2);
        double c = 2 * Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
        double d = R * c;
        result = d;
        return result;
    }
}
