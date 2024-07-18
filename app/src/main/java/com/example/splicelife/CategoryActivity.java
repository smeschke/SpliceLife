package com.example.splicelife;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
    private Map<String, String> keyCategoryMap;
    private Map<String, String> keyHumanNameMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        keyCategoryMap = loadKeyCategoryMap(); // Load key-category map
        keyHumanNameMap = loadKeyHumanNameMap(); // Load key-human name map

        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        int beltId = getIntent().getIntExtra("belt_id", -1);
        AppDatabase db = MainApplication.getDatabase();
        beltDao = db.beltDao();
        belt = beltDao.getBeltById(beltId);


        displayBeltCategories(); // Display categories with details

        // Handle back press using OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setResult(RESULT_OK);  // Ensure result is set when back is pressed
                finish();
            }
        });


// Handle back press using OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("updatedBeltId", belt.getId());
                setResult(RESULT_OK, resultIntent);  // Ensure result is set when back is pressed
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
                Intent resultIntent = new Intent();
                resultIntent.putExtra("updatedBeltId", updatedBeltId);
                setResult(RESULT_OK, resultIntent);
                finish();  // Close the activity
            }
        }
    }

    private Map<String, String> loadKeyCategoryMap() {
        Map<String, String> map = new HashMap<>();
        Resources res = getResources();
        String[] beltParameters = res.getStringArray(R.array.beltParameters);

        // Parse each parameter string into key and category and put them into the map
        for (String parameter : beltParameters) {
            String[] parts = parameter.split(":");
            if (parts.length == 2) {
                map.put(parts[0], parts[1]);
            }
        }

        return map;
    }

    private Map<String, String> loadKeyHumanNameMap() {
        Map<String, String> map = new HashMap<>();
        Resources res = getResources();
        String[] beltParametersHuman = res.getStringArray(R.array.beltParametersHuman);

        for (String parameter : beltParametersHuman) {
            String[] parts = parameter.split(":");
            if (parts.length == 2) {
                map.put(parts[0], parts[1]);
            }
        }

        return map;
    }
    private void displayBeltCategories() {
        if (belt != null) {
            Map<String, String> details = belt.getDetails();

            // Group details by category
            Map<String, List<String>> categoryDetails = new HashMap<>();
            for (Map.Entry<String, String> entry : details.entrySet()) {
                String category = getCategory(entry.getKey());

                // Skip the logs category
                if ("logs".equals(category)) {
                    continue;
                }

                String humanReadableName = keyHumanNameMap.get(entry.getKey());
                String displayText = humanReadableName != null
                        ? humanReadableName + ": \n" + entry.getValue()
                        : entry.getKey() + ": " + entry.getValue();
                categoryDetails.putIfAbsent(category, new ArrayList<>());
                categoryDetails.get(category).add(displayText + "\n");
            }

            // Ensure all categories are present in the map, even if they are empty
            for (String category : keyCategoryMap.values()) {
                if (!"logs".equals(category)) { // Skip adding logs category
                    categoryDetails.putIfAbsent(category, new ArrayList<>());
                }
            }

            // Add placeholder for empty categories
            for (Map.Entry<String, List<String>> entry : categoryDetails.entrySet()) {
                if (entry.getValue().isEmpty()) {
                    entry.getValue().add("No items present");
                }
            }

            // Create a list of categories
            List<String> categories = new ArrayList<>();

            // Add prioritized categories first
            if (categoryDetails.containsKey("general")) {
                categories.add("general");
            }
            if (categoryDetails.containsKey("beltParameters")) {
                categories.add("beltParameters");
            }
            if (categoryDetails.containsKey("spliceDetails")) {
                categories.add("spliceDetails");
            }
            if (categoryDetails.containsKey("tensioningSystem")) {
                categories.add("tensioningSystem");
            }
            if (categoryDetails.containsKey("pulleys")) {
                categories.add("pulleys");
            }

            // Add remaining categories
            for (String category : categoryDetails.keySet()) {
                if (!categories.contains(category)) {
                    categories.add(category);
                }
            }

            categoryAdapter = new CategoryAdapter(categories, categoryDetails, this, belt); // Pass context and belt object
            categoryRecyclerView.setAdapter(categoryAdapter);
        }
    }

    private String getCategory(String key) {
        return keyCategoryMap.getOrDefault(key, "User Defined");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the belt object from the database and display categories
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
