package com.example.splicelife;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EditDetailActivity extends AppCompatActivity {
    private RecyclerView detailRecyclerView;
    private DetailAdapter detailAdapter;
    private String category;
    private Belt belt;
    private BeltDao beltDao;
    private List<String> beltParameters;
    private List<String> details;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_detail);

        category = getIntent().getStringExtra("category");
        int beltId = getIntent().getIntExtra("belt_id", -1);

        // Retrieve the belt object from the database
        AppDatabase db = MainApplication.getDatabase();
        beltDao = db.beltDao();
        belt = beltDao.getBeltById(beltId);

        if (belt == null) {
            // Handle the case where belt is null
            finish();
            return;
        }

        detailRecyclerView = findViewById(R.id.detailRecyclerView);
        detailRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        beltParameters = loadBeltParameters();
        details = generateDetailsList(beltParameters, belt, category);

        detailAdapter = new DetailAdapter(details, this::showEditDialog);
        detailRecyclerView.setAdapter(detailAdapter);

        // Handle back press using OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setResult(RESULT_OK);  // Ensure result is set when back is pressed
                finish();
            }
        });
    }

    private List<String> loadBeltParameters() {
        Resources res = getResources();
        int arrayId = res.getIdentifier(category.toLowerCase(), "array", getPackageName());
        List<String> beltParametersList = new ArrayList<>();

        if (arrayId != 0) {
            String[] beltParametersArray = res.getStringArray(arrayId);
            for (String parameter : beltParametersArray) {
                beltParametersList.add(parameter);
            }
        }

        return beltParametersList;
    }

    private List<String> generateDetailsList(List<String> beltParameters, Belt belt, String category) {
        Map<String, String> beltDetails = belt.getDetails();
        List<String> detailsList = new ArrayList<>();

        // Load details in order defined by strings.xml
        for (String parameter : beltParameters) {
            String value = beltDetails.get(parameter);
            detailsList.add(parameter + "\n" + (value != null ? value : ""));
        }
        return detailsList;
    }


    private void showEditDialog(String detail) {
        int indexOfNewline = detail.indexOf('\n');
        String key = indexOfNewline != -1 ? detail.substring(0, indexOfNewline).trim() : detail.trim();
        String currentValue = indexOfNewline != -1 ? detail.substring(indexOfNewline + 1).trim() : "";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Value for " + key);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_detail, null);
        builder.setView(dialogView);

        EditText editText = dialogView.findViewById(R.id.editTextDetail);
        editText.setText(currentValue); // Set current value in the EditText

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newValue = editText.getText().toString();
            saveDetail(key, newValue);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }


    private void saveDetail(String key, String newValue) {
        // Update the belt object with the new value
        Map<String, String> beltDetails = belt.getDetails();
        beltDetails.put(key, newValue); // Update the detail
        belt.setDetails(beltDetails);
        beltDao.update(belt); // Save the updated belt back to the database

        // Update the displayed details list and notify the adapter
        for (int i = 0; i < details.size(); i++) {
            if (details.get(i).startsWith(key + "\n")) { // Correct separator
                details.set(i, key + "\n" + newValue); // Update the detail in the list
                break;
            }
        }
        detailAdapter.notifyDataSetChanged(); // Refresh the RecyclerView

        // Set result to indicate data has changed
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updatedBeltId", belt.getId());
        setResult(RESULT_OK, resultIntent);
    }
}
