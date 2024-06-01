// File: MainApplication.java
package com.example.splicelife;

import android.app.Application;
import androidx.room.Room;

public class MainApplication extends Application {
    private static AppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "splice-life-db")
                .allowMainThreadQueries()
                .build();
    }

    public static AppDatabase getDatabase() {
        return database;
    }
}
