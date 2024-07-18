package com.example.splicelife;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditLogActivity extends AppCompatActivity {
    private TextView logsTextView;
    private EditText newLogEntryEditText;
    private Button saveLogButton;
    private Belt belt;
    private BeltDao beltDao;

    private Button copyLastTwoLogsButton;
    private Button copyAllLogsButton;
    private TextView headerTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_log);

        int beltId = getIntent().getIntExtra("belt_id", -1);
        AppDatabase db = MainApplication.getDatabase();
        beltDao = db.beltDao();
        belt = beltDao.getBeltById(beltId);

        logsTextView = findViewById(R.id.logsTextView);
        newLogEntryEditText = findViewById(R.id.newLogEntryEditText);
        saveLogButton = findViewById(R.id.saveLogButton);

        copyLastTwoLogsButton = findViewById(R.id.copyLastTwoLogsButton);
        copyAllLogsButton = findViewById(R.id.copyAllLogsButton);
        headerTextView = findViewById(R.id.headerTextView);

        // Set header text
        String header = "Belt Name: " + belt.getDetails().get("Conveyor Name") + "\n" +
                "Company: " + belt.getDetails().get("Company Name") + "\n" +
                "Location: " + belt.getDetails().get("Conveyor Location");
        headerTextView.setText(header);

        // Get the logs data from the belt object
        String logs = belt.getDetails().get("Logs");
        if (logs != null) {
            logsTextView.setText(logs);
        }

        saveLogButton.setOnClickListener(v -> saveNewLogEntry());
        copyLastTwoLogsButton.setOnClickListener(v -> copyLastTwoLogs());
        copyAllLogsButton.setOnClickListener(v -> copyAllLogs());
    }

    private void saveNewLogEntry() {
        String newLogEntry = newLogEntryEditText.getText().toString();
        if (!newLogEntry.isEmpty()) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            String entryWithTimestamp = timestamp + " - " + newLogEntry;
            String currentLogs = logsTextView.getText().toString();
            String updatedLogs = entryWithTimestamp + "\n\n" + currentLogs;
            logsTextView.setText(updatedLogs);

            // Update the belt object with the new logs
            belt.getDetails().put("Logs", updatedLogs);
            beltDao.update(belt);

            // Clear the input field
            newLogEntryEditText.setText("");

            // Set result indicating changes have been made
            Intent resultIntent = new Intent();
            resultIntent.putExtra("belt_id", belt.getId());
            setResult(RESULT_OK, resultIntent);
        }
    }

    private String getHeader() {
        return "Belt Name: " + belt.getDetails().get("Conveyor Name") + "\n" +
                "Company: " + belt.getDetails().get("Company Name") + "\n" +
                "Location: " + belt.getDetails().get("Conveyor Location") + "\n\n";
    }

    private void copyLastTwoLogs() {
        String[] logEntries = logsTextView.getText().toString().split("\n\n");
        String logsToCopy = logEntries.length >= 2 ? logEntries[0] + "\n\n" + logEntries[1] : logEntries[0];
        logsToCopy = getHeader() + logsToCopy;
        copyToClipboard(logsToCopy);
    }

    private void copyAllLogs() {
        String logsToCopy = logsTextView.getText().toString();
        logsToCopy = getHeader() + logsToCopy;
        copyToClipboard(logsToCopy);
    }

    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Log Data", text);
        clipboard.setPrimaryClip(clip);
    }
}
