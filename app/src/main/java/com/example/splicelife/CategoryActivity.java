package com.example.splicelife;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryActivity extends AppCompatActivity {
    private static final int REQUEST_EDIT_DETAIL = 1;
    private RecyclerView categoryRecyclerView;
    private CategoryAdapter categoryAdapter;
    private Belt belt;
    private BeltDao beltDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        int beltId = getIntent().getIntExtra("belt_id", -1);
        AppDatabase db = MainApplication.getDatabase();
        beltDao = db.beltDao();
        belt = beltDao.getBeltById(beltId);

        if (belt != null) {
            displayBeltCategories();
        } else {
            Toast.makeText(this, "Belt data not found.", Toast.LENGTH_SHORT).show();
            finish();
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("updatedBeltId", belt.getId());
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_DETAIL && resultCode == RESULT_OK) {
            int updatedBeltId = data != null ? data.getIntExtra("updatedBeltId", -1) : -1;
            if (updatedBeltId != -1) {
                belt = beltDao.getBeltById(updatedBeltId);
                displayBeltCategories();
            }
        }
    }

    private void displayBeltCategories() {
        Map<String, String> details = belt.getDetails();
        Map<String, List<String>> categoryDetails = new HashMap<>();

        Resources res = getResources();
        String[] categoryKeys = res.getStringArray(R.array.categories);
        List<String> categories = new ArrayList<>();

        // Initialize category details list
        for (String category : categoryKeys) {
            if (!"logs".equals(category)) {
                categories.add(category);
                categoryDetails.putIfAbsent(category, new ArrayList<>());
            }
        }

        // Map details to categories
        for (Map.Entry<String, String> entry : details.entrySet()) {
            String category = getCategory(entry.getKey());
            String displayText = entry.getKey() + "\n" + entry.getValue();

            if (categoryDetails.containsKey(category)) {
                categoryDetails.get(category).add(displayText);
            } else {
                Log.w("CategoryActivity", "Category not found for key: " + entry.getKey());
            }
        }

        // Log the category details for debugging
        for (Map.Entry<String, List<String>> entry : categoryDetails.entrySet()) {
            Log.d("CategoryActivity", "Category: " + entry.getKey() + ", Items: " + entry.getValue());
        }

        categoryAdapter = new CategoryAdapter(categories, categoryDetails, this, belt);
        categoryRecyclerView.setAdapter(categoryAdapter);
    }

    private String getCategory(String key) {
        KeyCategoryMapping keyCategoryMapping = new KeyCategoryMapping(this);
        return keyCategoryMapping.getCategory(key);
    }

    @Override
    protected void onResume() {
        super.onResume();
        int beltId = getIntent().getIntExtra("belt_id", -1);
        if (beltId != -1) {
            belt = beltDao.getBeltById(beltId);
            if (belt != null) {
                displayBeltCategories();
            } else {
                Toast.makeText(this, "Failed to load belt details.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
