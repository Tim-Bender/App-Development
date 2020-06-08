package com.example.Spudnik;

import java.util.HashMap;

public class GattAttributes {

    private static HashMap<String,String> attributes = new HashMap<>();
    public static String PIN_INFORMATION="00002a37-0000-1000-8000-00805f9b34fb";

    static{
        attributes.put("0000180d-0000-1000-8000-00805f9b34fb","Pin connection service");
    }

    public static String lookup(String uuid,String defaultName){
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
