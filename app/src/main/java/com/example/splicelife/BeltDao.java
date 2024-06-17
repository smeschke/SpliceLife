// File: BeltDao.java
package com.example.splicelife;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BeltDao {
    @Insert
    void insert(Belt belt);

    @Query("SELECT * FROM belts WHERE id = :id")
    Belt getBeltById(int id);

    @Query("SELECT * FROM belts")
    List<Belt> getAllBelts();

    @Update
    void update(Belt belt);


    @Delete
    void delete(Belt belt);  // Add this method
}

