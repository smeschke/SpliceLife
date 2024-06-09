package com.example.splicelife;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImportActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PICK_CSV = 1;
    private static final int REQUEST_CODE_MAPPING = 2;
    private TextView importInstructionsTextView;
    private Button selectFileButton;
    private Button exportButton;
    private Map<String, String> columnToKeyMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        importInstructionsTextView = findViewById(R.id.importInstructionsTextView);
        selectFileButton = findViewById(R.id.selectFileButton);
        exportButton = findViewById(R.id.exportButton);

        String importInstructions = "To upload your data:\n\n" +
                "1. Click the 'Select File' button below.\n" +
                "2. Choose the CSV file containing your data from your device.\n" +
                "3. Once the file is selected, you will be prompted to map your data columns to the app's data fields.\n" +
                "4. Review the mappings and make sure they are correct.\n" +
                "5. Confirm the mappings to complete the import process.\n\n" +
                "If you encounter any issues, please contact support for assistance.";
        importInstructionsTextView.setText(importInstructions);

        selectFileButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, REQUEST_CODE_PICK_CSV);
        });

        exportButton.setOnClickListener(v -> exportDataToCSV());
    }

    private void exportDataToCSV() {
        new Thread(() -> {
            List<Belt> belts = AppDatabase.getInstance(getApplicationContext()).beltDao().getAllBelts();

            if (belts.isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "No data to export", Toast.LENGTH_LONG).show());
                return;
            }

            // Get all unique keys from the details map
            Set<String> keys = belts.get(0).getDetails().keySet();

            File csvFile = new File(ContextCompat.getExternalFilesDirs(this, null)[0], "belts_data.csv");

            try (FileWriter writer = new FileWriter(csvFile)) {
                // Write the header row
                writer.append("ID");
                for (String key : keys) {
                    writer.append(",").append(key);
                }
                writer.append("\n");

                // Write data rows
                for (Belt belt : belts) {
                    writer.append(String.valueOf(belt.getId()));
                    for (String key : keys) {
                        writer.append(",").append(belt.getDetails().getOrDefault(key, ""));
                    }
                    writer.append("\n");
                }

                runOnUiThread(() -> Toast.makeText(this, "Data exported to " + csvFile.getAbsolutePath(), Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Failed to export data: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_CSV && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                readColumnHeadersFromCSV(fileUri);
            }
        } else if (requestCode == REQUEST_CODE_MAPPING && resultCode == RESULT_OK && data != null) {
            columnToKeyMap = (Map<String, String>) data.getSerializableExtra("columnToKeyMap");
        }
    }

    private void readColumnHeadersFromCSV(Uri fileUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String headerLine = reader.readLine();
            if (headerLine != null) {
                String[] headers = headerLine.split(",");
                ArrayList<String> columnHeaders = new ArrayList<>();
                for (String header : headers) {
                    columnHeaders.add(header.trim());
                }
                Intent intent = new Intent(this, KeySelectionActivity.class);
                intent.putExtra("fileUri", fileUri);
                intent.putStringArrayListExtra("columnHeaders", columnHeaders);
                startActivityForResult(intent, REQUEST_CODE_MAPPING);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            importInstructionsTextView.setText("Error reading file: " + e.getMessage());
        }
    }

    private void proceedWithImport() {
        // Implement the import logic here using columnToKeyMap
    }
}
