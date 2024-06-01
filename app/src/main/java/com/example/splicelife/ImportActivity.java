package com.example.splicelife;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

public class ImportActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PICK_CSV = 1; // Request code for picking a CSV file
    private static final int REQUEST_CODE_MAPPING = 2;  // Request code for column mapping activity
    private TextView importInstructionsTextView;  // TextView to display import instructions
    private Button selectFileButton;  // Button to trigger file selection
    private Map<String, String> columnToKeyMap;  // Map to store column-to-key mappings

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        // Initialize UI components
        importInstructionsTextView = findViewById(R.id.importInstructionsTextView);
        selectFileButton = findViewById(R.id.selectFileButton);

        // Set import instructions
        String importInstructions = "To upload your data:\n\n" +
                "1. Click the 'Select File' button below.\n" +
                "2. Choose the CSV file containing your data from your device.\n" +
                "3. Once the file is selected, you will be prompted to map your data columns to the app's data fields.\n" +
                "4. Review the mappings and make sure they are correct.\n" +
                "5. Confirm the mappings to complete the import process.\n\n" +
                "If you encounter any issues, please contact support for assistance.";
        importInstructionsTextView.setText(importInstructions);

        // Set click listener for the select file button
        selectFileButton.setOnClickListener(v -> {
            // Create an intent to open a file picker
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, REQUEST_CODE_PICK_CSV);  // Start file picker activity
        });
    }

    // Handle results from started activities
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_CSV && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();  // Get the URI of the selected file
            if (fileUri != null) {
                readColumnHeadersFromCSV(fileUri);  // Read the column headers from the selected CSV file
            }
        } else if (requestCode == REQUEST_CODE_MAPPING && resultCode == RESULT_OK && data != null) {
            columnToKeyMap = (Map<String, String>) data.getSerializableExtra("columnToKeyMap");  // Get the column-to-key map from the mapping activity
        }
    }

    // Read column headers from the selected CSV file
    private void readColumnHeadersFromCSV(Uri fileUri) {
        try {
            // Open an input stream from the file URI
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String headerLine = reader.readLine();  // Read the first line (header)
            if (headerLine != null) {
                String[] headers = headerLine.split(",");  // Split the header line into columns
                ArrayList<String> columnHeaders = new ArrayList<>();
                for (String header : headers) {
                    columnHeaders.add(header.trim());  // Trim whitespace and add to list
                }
                // Start the KeySelectionActivity to map columns to keys
                Intent intent = new Intent(this, KeySelectionActivity.class);
                intent.putExtra("fileUri", fileUri);  // Pass the file URI to the next activity
                intent.putStringArrayListExtra("columnHeaders", columnHeaders);  // Pass the column headers to the next activity
                startActivityForResult(intent, REQUEST_CODE_MAPPING);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            importInstructionsTextView.setText("Error reading file: " + e.getMessage());  // Display error message
        }
    }

    // Proceed with the import process using the mapped columns
    private void proceedWithImport() {
        // Implement the import logic here using columnToKeyMap
    }
}
