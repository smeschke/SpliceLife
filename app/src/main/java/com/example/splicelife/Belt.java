// File: Belt.java
package com.example.splicelife;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Map;

@Entity(tableName = "belts")
public class Belt {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @TypeConverters(KeyValueConverter.class)
    public Map<String, String> details;

    // Constructors, getters, and setters
    public Belt(Map<String, String> details) {
        this.details = details;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }
}
