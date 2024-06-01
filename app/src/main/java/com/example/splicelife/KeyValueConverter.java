// File: KeyValueConverter.java
package com.example.splicelife;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public class KeyValueConverter {
    private static Gson gson = new Gson();

    @TypeConverter
    public static Map<String, String> fromString(String value) {
        Type mapType = new TypeToken<Map<String, String>>() {}.getType();
        return gson.fromJson(value, mapType);
    }

    @TypeConverter
    public static String fromMap(Map<String, String> map) {
        return gson.toJson(map);
    }
}
