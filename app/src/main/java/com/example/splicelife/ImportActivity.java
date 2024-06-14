package com.example.splicelife;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;

public class ImportActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_CODE_MANAGE_EXTERNAL_STORAGE = 2;
    private TextView importInstructionsTextView;
    private Button exportJsonButton;

    private BeltViewModel beltViewModel;
    private List<Belt> belts;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        importInstructionsTextView = findViewById(R.id.importInstructionsTextView);
        exportJsonButton = findViewById(R.id.exportJsonButton);

        String importInstructions = "To export your data to a JSON file:\n\n" +
                "1. Click the 'Export to JSON' button below.\n" +
                "2. The file will be saved to the Documents folder on your device.\n" +
                "If you encounter any issues, please contact support for assistance.";
        importInstructionsTextView.setText(importInstructions);

        exportJsonButton.setOnClickListener(v -> exportDataToJSON());

        // Request storage permissions if not granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, REQUEST_CODE_MANAGE_EXTERNAL_STORAGE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
            }
        }

        // Initialize BeltViewModel
        beltViewModel = new ViewModelProvider(this).get(BeltViewModel.class);

        // Observe the belt list LiveData
        beltViewModel.getAllBelts().observe(this, belts -> {
            this.belts = belts;
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                Toast.makeText(this, "Permission denied to write to external storage", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_MANAGE_EXTERNAL_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // Permission granted
                } else {
                    Toast.makeText(this, "Permission denied to manage external storage", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void exportDataToJSON() {
        new Thread(() -> {
            try {
                if (belts == null || belts.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(this, "No data to export", Toast.LENGTH_LONG).show());
                    return;
                }

                JSONArray jsonArray = new JSONArray();

                for (Belt belt : belts) {
                    JSONObject beltJson = new JSONObject();
                    beltJson.put("id", belt.getId());
                    Map<String, String> details = belt.getDetails();
                    for (Map.Entry<String, String> entry : details.entrySet()) {
                        beltJson.put(entry.getKey(), entry.getValue());
                    }
                    jsonArray.put(beltJson);
                }

                File documentsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                if (!documentsFolder.exists()) {
                    documentsFolder.mkdirs(); // Create the directory if it doesn't exist
                }

                File jsonFile = new File(documentsFolder, "belts_data.json");
                FileWriter writer = new FileWriter(jsonFile);
                writer.write(jsonArray.toString(4)); // 4 is the number of spaces for indentation
                writer.close();

                String filePath = jsonFile.getAbsolutePath();
                Log.d("ExportDataToJSON", "Data exported to " + filePath);
                runOnUiThread(() -> Toast.makeText(this, "Data exported to " + filePath, Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                Log.e("ExportDataToJSON", "Failed to export data: " + e.getMessage(), e);
                runOnUiThread(() -> Toast.makeText(this, "Failed to export data: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
