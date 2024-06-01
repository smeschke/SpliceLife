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
    private static final int REQUEST_CODE_PICK_CSV = 1;
    private static final int REQUEST_CODE_MAPPING = 2;
    private TextView importInstructionsTextView;
    private Button selectFileButton;
    private Map<String, String> columnToKeyMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        importInstructionsTextView = findViewById(R.id.importInstructionsTextView);
        selectFileButton = findViewById(R.id.selectFileButton);

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
                intent.putExtra("fileUri", fileUri); // Add the fileUri to the intent
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
