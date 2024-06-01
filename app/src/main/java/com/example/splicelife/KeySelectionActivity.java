// KeySelectionActivity.java
package com.example.splicelife;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeySelectionActivity extends AppCompatActivity {

    private TextView tvProgress, tvUserColumnValue, tvDbKey;
    private Button btnSelectKey;
    private GridLayout gridButtons;

    private List<String> columnHeaders;
    private int currentIndex = 0;
    private Map<String, String> columnToKeyMap;
    private Uri fileUri;
    private AppDatabase db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_selection);

        // Initialize the database
        db = MainApplication.getDatabase();

        tvProgress = findViewById(R.id.tvProgress);
        tvUserColumnValue = findViewById(R.id.tvUserColumnValue);
        tvDbKey = findViewById(R.id.tvDbKey);
        btnSelectKey = findViewById(R.id.btnSelectKey);
        gridButtons = findViewById(R.id.gridButtons);

        Intent intent = getIntent();
        if (intent != null) {
            columnHeaders = intent.getStringArrayListExtra("columnHeaders");
            fileUri = intent.getParcelableExtra("fileUri");

            if (fileUri == null) {
                Log.e("KeySelectionActivity", "File URI is null in Intent");
            } else {
                Log.d("KeySelectionActivity", "File URI received: " + fileUri.toString());
            }

            if (columnHeaders != null) {
                Log.d("KeySelectionActivity", "Column Headers received: " + columnHeaders.toString());
            } else {
                Log.e("KeySelectionActivity", "Column Headers are null in Intent");
            }
        } else {
            Log.e("KeySelectionActivity", "Intent is null");
        }

        columnToKeyMap = new HashMap<>();

        String[] beltParametersSimple = getResources().getStringArray(R.array.beltParametersSimple);
        populateButtons(beltParametersSimple);

        btnSelectKey.setOnClickListener(v -> {
            if (columnHeaders != null && !columnHeaders.isEmpty()) {
                columnToKeyMap.put(columnHeaders.get(currentIndex), tvDbKey.getText().toString());
                moveToNextColumn();
            }
        });

        if (columnHeaders != null && !columnHeaders.isEmpty()) {
            updateColumnDisplay();
        } else {
            tvUserColumnValue.setText("No columns available");
            btnSelectKey.setEnabled(false);
        }
    }

    private void populateButtons(String[] parameters) {
        for (String param : parameters) {
            String[] parts = param.split(":");
            String buttonText = parts.length > 1 ? parts[0] : param;

            Button button = new Button(this);
            button.setText(buttonText);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
            button.setLayoutParams(params);
            button.setPadding(16, 16, 16, 16);

            button.setOnClickListener(v -> tvDbKey.setText(buttonText));
            gridButtons.addView(button);
        }
    }

    private void moveToNextColumn() {
        currentIndex++;
        if (currentIndex < columnHeaders.size()) {
            updateColumnDisplay();
        } else {
            showConfirmationDialog();
        }
    }

    private void showConfirmationDialog() {
        String mappingInfo = buildMappingInfoString();
        new AlertDialog.Builder(this)
                .setTitle("Confirm Mappings")
                .setMessage("Do you like the map? If not, you can remap.\n\n" + mappingInfo)
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (fileUri != null) {
                        List<Map<String, String>> csvData = readDataFromCSV(fileUri);
                        List<Belt> belts = mapData(csvData);
                        insertData(belts);
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("columnToKeyMap", (HashMap<String, String>) columnToKeyMap);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        Log.e("KeySelectionActivity", "File URI is null when trying to read CSV");
                    }
                })
                .setNegativeButton("No", (dialog, which) -> {
                    currentIndex = 0;
                    columnToKeyMap.clear();
                    updateColumnDisplay();
                })
                .setCancelable(false)
                .show();
    }

    private String buildMappingInfoString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : columnToKeyMap.entrySet()) {
            sb.append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    private void updateColumnDisplay() {
        String currentColumn = columnHeaders.get(currentIndex);
        tvUserColumnValue.setText("Current Column: " + currentColumn);
        tvProgress.setText(String.format("Processing %d of %d Columns", currentIndex + 1, columnHeaders.size()));
        tvDbKey.setText(currentColumn);
    }


    private List<Map<String, String>> readDataFromCSV(Uri fileUri) {
        List<Map<String, String>> data = new ArrayList<>();
        Log.d("KeySelectionActivity", "Starting to read CSV data");
        if (fileUri == null) {
            Log.e("KeySelectionActivity", "File URI is null");
            return data;
        }
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            if (inputStream == null) {
                Log.e("KeySelectionActivity", "InputStream is null");
                return data;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            CSVReader csvReader = new CSVReader(reader);
            List<String[]> csvLines = csvReader.readAll();
            csvReader.close();

            if (csvLines.isEmpty()) {
                Log.e("KeySelectionActivity", "CSV file is empty");
                return data;
            }

            List<String> headers = Arrays.asList(csvLines.get(0));
            Log.d("KeySelectionActivity", "CSV headers: " + headers.toString());

            for (int i = 1; i < csvLines.size(); i++) {
                String[] values = csvLines.get(i);
                if (values.length != headers.size()) {
                    Log.e("KeySelectionActivity", "Mismatch between number of headers and values in row: " + Arrays.toString(values));
                    continue;
                }
                Map<String, String> row = new HashMap<>();
                for (int j = 0; j < values.length; j++) {
                    row.put(headers.get(j), values[j].trim());
                }
                data.add(row);
                Log.d("KeySelectionActivity", "Added row: " + row.toString());
            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
            Log.e("KeySelectionActivity", "Error reading CSV", e);
        }
        return data;
    }



    private List<Belt> mapData(List<Map<String, String>> csvData) {
        List<Belt> belts = new ArrayList<>();
        for (Map<String, String> row : csvData) {
            Map<String, String> mappedRow = new HashMap<>();
            for (Map.Entry<String, String> entry : columnToKeyMap.entrySet()) {
                String csvColumn = entry.getKey();
                String dbKey = entry.getValue();
                if (row.containsKey(csvColumn)) {
                    mappedRow.put(dbKey, row.get(csvColumn));
                }
            }
            belts.add(new Belt(mappedRow));
        }
        return belts;
    }

    private void insertData(List<Belt> belts) {
        new Thread(() -> {
            BeltDao beltDao = db.beltDao();
            for (Belt belt : belts) {
                try {
                    Log.d("KeySelectionActivity", "Inserting belt: " + belt.getDetails().toString());
                    beltDao.insert(belt);
                    Log.d("KeySelectionActivity", "Inserted belt: " + belt.getDetails().toString());
                } catch (Exception e) {
                    Log.e("KeySelectionActivity", "Error inserting belt", e);
                }
            }

            // Retrieve and log all belts after insertion
            List<Belt> allBelts = beltDao.getAllBelts();
            for (Belt belt : allBelts) {
                Log.d("KeySelectionActivity", "Retrieved belt from DB: " + belt.getDetails().toString());
            }
            runOnUiThread(() -> {
                Intent intent = new Intent(KeySelectionActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            });
        }).start();
    }
}