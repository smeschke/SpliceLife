package com.example.splicelife;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_EDIT_LOG = 2;
    public static final int REQUEST_CODE_IMPORT = 1;
    private static final int REQUEST_CODE_EDIT_DETAIL = 3;

    private RecyclerView recyclerView;
    private BeltAdapter beltAdapter;
    private BeltViewModel beltViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize RecyclerView and set its layout manager
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize ViewModel
        beltViewModel = new ViewModelProvider(this).get(BeltViewModel.class);

        // Setup Floating Action Button (FAB) to add new belts
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> showAddBeltDialog());

        // Observe the belt list LiveData
        beltViewModel.getAllBelts().observe(this, belts -> {
            beltAdapter = new BeltAdapter(belts, belt -> {
                Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
                intent.putExtra("belt_id", belt.getId());
                startActivityForResult(intent, REQUEST_CODE_EDIT_DETAIL);
            });
            recyclerView.setAdapter(beltAdapter);
        });

        // Attach ItemTouchHelper to RecyclerView to handle swipe gestures
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;  // We don't want drag & drop functionality
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                showDeleteConfirmationDialog(position);  // Show dialog to confirm deletion
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    // Show a confirmation dialog before deleting a belt
    private void showDeleteConfirmationDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Belt")
                .setMessage("Are you sure you want to delete this belt?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    Belt belt = beltAdapter.getBeltAtPosition(position);
                    Executors.newSingleThreadExecutor().execute(() -> beltViewModel.delete(belt));  // Remove belt from the database on a background thread
                })
                .setNegativeButton("Cancel", (dialog, which) -> beltAdapter.notifyItemChanged(position))  // Refresh item if deletion is canceled
                .create()
                .show();
    }

    // Show dialog to add a new belt
    private void showAddBeltDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Belt")
                .setView(getLayoutInflater().inflate(R.layout.dialog_add_belt, null))  // Inflate custom layout for the dialog
                .setPositiveButton("Add", (dialog, id) -> {
                    AlertDialog alertDialog = (AlertDialog) dialog;
                    EditText editTextBeltName = alertDialog.findViewById(R.id.editTextBeltName);
                    String beltName = editTextBeltName.getText().toString();
                    if (beltName.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Belt name cannot be empty", Toast.LENGTH_SHORT).show();
                    } else {
                        addNewBelt(beltName);  // Add the new belt to the database and refresh list
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel())
                .setNeutralButton("Import from CSV", (dialog, id) -> {
                    // Handle importing belts from a CSV file
                    Intent intent = new Intent(MainActivity.this, ImportActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_IMPORT);
                });
        builder.create().show();
    }

    // Add a new belt to the database
    private void addNewBelt(String beltName) {
        Map<String, String> details = new HashMap<>();
        details.put("conveyorName", beltName);
        Belt newBelt = new Belt(details);
        Executors.newSingleThreadExecutor().execute(() -> beltViewModel.insert(newBelt));  // Insert new belt into the database on a background thread
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMPORT && resultCode == RESULT_OK) {
            beltViewModel.loadBelts();  // Reload belts after importing
        } else if ((requestCode == REQUEST_CODE_EDIT_LOG || requestCode == REQUEST_CODE_EDIT_DETAIL) && resultCode == RESULT_OK) {
            int beltId = data != null ? data.getIntExtra("belt_id", -1) : -1;
            if (beltId != -1) {
                refreshBelt(beltId);  // Refresh the updated belt details
            }
        }
    }

    // Refresh the details of a specific belt
    private void refreshBelt(int beltId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Belt updatedBelt = beltViewModel.getBeltById(beltId);  // Retrieve updated belt details from the database
            runOnUiThread(() -> {
                int position = beltAdapter.getPositionById(beltId);
                if (position != -1) {
                    beltAdapter.updateBeltAtPosition(position, updatedBelt);  // Update the belt in the adapter and notify changes
                }
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        beltViewModel.loadBelts();  // Ensure the data is always refreshed when the activity is resumed
    }
}
