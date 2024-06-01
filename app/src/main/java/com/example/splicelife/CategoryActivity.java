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
    private List<String> userDefinedKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        keyCategoryMap = loadKeyCategoryMap(); // Load key-category map
        userDefinedKeys = new ArrayList<>();

        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        int beltId = getIntent().getIntExtra("belt_id", -1);
        AppDatabase db = MainApplication.getDatabase();
        beltDao = db.beltDao();
        belt = beltDao.getBeltById(beltId);

        FloatingActionButton fabAddCustom = findViewById(R.id.fab_add_custom);
        fabAddCustom.setOnClickListener(v -> showAddCustomDialog());

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

    private void showAddCustomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Custom Detail");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_custom, null);
        builder.setView(dialogView);

        EditText editTextKey = dialogView.findViewById(R.id.editTextKey);
        EditText editTextValue = dialogView.findViewById(R.id.editTextValue);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String key = editTextKey.getText().toString();
            String value = editTextValue.getText().toString();
            if (!key.isEmpty() && !value.isEmpty()) {
                addCustomDetail(key, value);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }


    private void addCustomDetail(String key, String value) {
        // Add custom key-value pair to belt details and save to database
        Map<String, String> beltDetails = belt.getDetails();
        beltDetails.put(key, value);
        belt.setDetails(beltDetails);
        beltDao.update(belt); // Save the updated belt back to the database

        // Add the key to user defined keys list if it's not already there
        if (!userDefinedKeys.contains(key)) {
            userDefinedKeys.add(key);
        }

        displayBeltCategories(); // Refresh categories to show the new custom detail
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

    private void displayBeltCategories() {
        if (belt != null) {
            Map<String, String> details = belt.getDetails();

            // Group details by category
            Map<String, List<String>> categoryDetails = new HashMap<>();
            for (Map.Entry<String, String> entry : details.entrySet()) {
                String category = getCategory(entry.getKey());
                categoryDetails.putIfAbsent(category, new ArrayList<>());
                categoryDetails.get(category).add(entry.getKey() + ": " + entry.getValue());
            }

            // Ensure all categories are present in the map, even if they are empty
            for (String category : keyCategoryMap.values()) {
                categoryDetails.putIfAbsent(category, new ArrayList<>());
            }

            // Add placeholder for empty categories
            for (Map.Entry<String, List<String>> entry : categoryDetails.entrySet()) {
                if (entry.getValue().isEmpty()) {
                    entry.getValue().add("No items present");
                }
            }

            // Add user defined category if there are custom keys
            if (!userDefinedKeys.isEmpty()) {
                List<String> userDefinedDetails = new ArrayList<>();
                for (String key : userDefinedKeys) {
                    userDefinedDetails.add(key + ": " + details.get(key));
                }
                categoryDetails.put("User Defined", userDefinedDetails);
            }

            // Create a list of categories
            List<String> categories = new ArrayList<>(categoryDetails.keySet());
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
