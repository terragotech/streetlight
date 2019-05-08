package com.terragoedge.streetlight.enumeration;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public enum FixtureCodeType {
    AVIATION("Aviation","AV"),
    COBRAHEAD_ALLEY("Cobrahead Alley","CA"),
    COBRAHEAD_OTHER("Cobrahead Other","CO"),
    COBRAHEAD_STREET("Cobrahead Street","CS"),
    FLOOD("Flood","FL"),
    PENDANT("Pendant","PN"),
    PIGGY_BACK("Piggy-Back","PB"),
    POST_TOP_ACORN("Post-Top Acorn","PT"),
    VIADUCT("Viaduct","VT"),
    NONE("None","OT"),
    OTHER("Other","OT");

    private final String key;
    private final String value;

    FixtureCodeType(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
    private static final Map<String, FixtureCodeType> fixtureCodeTypeMap = new HashMap<>();
    static {
        for(FixtureCodeType fixtureCodeType : FixtureCodeType.values()){
            fixtureCodeTypeMap.put(fixtureCodeType.getKey(),fixtureCodeType);
        }
    }
    public static FixtureCodeType get(String key){
        return fixtureCodeTypeMap.get(key);
    }

    @Override
    public String toString() {
        return "FixtureCodeType{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
