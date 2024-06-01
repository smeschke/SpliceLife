package com.example.splicelife;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.Executors;

public class BeltViewModel extends AndroidViewModel {

    private BeltDao beltDao;
    private MutableLiveData<List<Belt>> beltsLiveData;

    public BeltViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = MainApplication.getDatabase();
        beltDao = db.beltDao();
        beltsLiveData = new MutableLiveData<>();
        loadBelts();
    }

    // Load all belts from the database and post to LiveData
    public void loadBelts() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Belt> belts = beltDao.getAllBelts();
            beltsLiveData.postValue(belts);
        });
    }

    // Get LiveData for observing the list of belts
    public LiveData<List<Belt>> getAllBelts() {
        return beltsLiveData;
    }

    // Insert a new belt into the database
    public void insert(Belt belt) {
        Executors.newSingleThreadExecutor().execute(() -> {
            beltDao.insert(belt);
            loadBelts();  // Reload belts after insertion
        });
    }

    // Delete a belt from the database
    public void delete(Belt belt) {
        Executors.newSingleThreadExecutor().execute(() -> {
            beltDao.delete(belt);
            loadBelts();  // Reload belts after deletion
        });
    }

    // Get a belt by its ID
    public Belt getBeltById(int beltId) {
        return beltDao.getBeltById(beltId);
    }
}
