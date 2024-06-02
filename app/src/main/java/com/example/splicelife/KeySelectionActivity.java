package com.example.splicelife;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

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

    private TextView tvProgress, tvUserColumnValue;
    private AutoCompleteTextView autoCompleteDbKey;
    private Button btnSelectKey;
    private Spinner spinnerGeneral, spinnerBeltParameters, spinnerLogs;

    private List<String> columnHeaders;
    private int currentIndex = 0;
    private Map<String, String> columnToKeyMap;
    private Uri fileUri;
    private AppDatabase db;
    private String lastSelectedItemGeneral = "";
    private String lastSelectedItemBeltParameters = "";
    private String lastSelectedItemLogs = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_selection);

        // Initialize the database
        db = MainApplication.getDatabase();

        // Initialize UI components
        tvProgress = findViewById(R.id.tvProgress);
        tvUserColumnValue = findViewById(R.id.tvUserColumnValue);
        autoCompleteDbKey = findViewById(R.id.autoCompleteDbKey);
        btnSelectKey = findViewById(R.id.btnSelectKey);
        spinnerGeneral = findViewById(R.id.spinnerGeneral);
        spinnerBeltParameters = findViewById(R.id.spinnerBeltParameters);
        spinnerLogs = findViewById(R.id.spinnerLogs);

        // Extract and categorize belt parameters
        String[] beltParametersSimple = getResources().getStringArray(R.array.beltParametersSimple);
        List<String> generalParams = new ArrayList<>();
        List<String> beltParams = new ArrayList<>();
        List<String> logParams = new ArrayList<>();

        for (String param : beltParametersSimple) {
            String[] parts = param.split(":");
            if (parts.length == 2) {
                switch (parts[1]) {
                    case "general":
                        generalParams.add(parts[0]);
                        break;
                    case "beltParameters":
                        beltParams.add(parts[0]);
                        break;
                    case "logs":
                        logParams.add(parts[0]);
                        break;
                }
            }
        }

        // Set up spinners with categorized parameters
        setupSpinner(spinnerGeneral, generalParams, "general");
        setupSpinner(spinnerBeltParameters, beltParams, "beltParameters");
        setupSpinner(spinnerLogs, logParams, "logs");

        Intent intent = getIntent();
        if (intent != null) {
            columnHeaders = intent.getStringArrayListExtra("columnHeaders");
            fileUri = intent.getParcelableExtra("fileUri");
        }

        columnToKeyMap = new HashMap<>();

        // Populate AutoCompleteTextView with database keys
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, beltParametersSimple);
        autoCompleteDbKey.setAdapter(adapter);

        btnSelectKey.setOnClickListener(v -> {
            if (columnHeaders != null && !columnHeaders.isEmpty()) {
                columnToKeyMap.put(columnHeaders.get(currentIndex), autoCompleteDbKey.getText().toString());
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

    private void setupSpinner(Spinner spinner, List<String> items, String category) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = items.get(position);
                switch (category) {
                    case "general":
                        if (!selectedItem.equals(lastSelectedItemGeneral)) {
                            autoCompleteDbKey.setText(selectedItem);
                            lastSelectedItemGeneral = selectedItem;
                        }
                        break;
                    case "beltParameters":
                        if (!selectedItem.equals(lastSelectedItemBeltParameters)) {
                            autoCompleteDbKey.setText(selectedItem);
                            lastSelectedItemBeltParameters = selectedItem;
                        }
                        break;
                    case "logs":
                        if (!selectedItem.equals(lastSelectedItemLogs)) {
                            autoCompleteDbKey.setText(selectedItem);
                            lastSelectedItemLogs = selectedItem;
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
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
                        Intent mainActivityIntent = new Intent(KeySelectionActivity.this, MainActivity.class);
                        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(mainActivityIntent);
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
        autoCompleteDbKey.setText(currentColumn, false);  // Set default text
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
                beltDao.insert(belt);
            }
        }).start();
    }
}