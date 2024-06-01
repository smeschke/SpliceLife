// File: AppDatabase.java
package com.example.splicelife;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Belt.class}, version = 1)
@TypeConverters({KeyValueConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract BeltDao beltDao();
}
