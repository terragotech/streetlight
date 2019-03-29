package com.terragoedge.slvinterface.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Constants {
    public static final Set<String> slv_pole_status = new HashSet<String>(Arrays.asList(
            new String[] {"Could not be installed","Does not exist","Removed","Pole knocked down"}
            // new String[] {"Complete","Complete, standards not met"}
    ));
}
