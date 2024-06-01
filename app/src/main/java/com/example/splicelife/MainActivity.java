package com.example.splicelife;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_EDIT_LOG = 2;
    public static final int REQUEST_CODE_IMPORT = 1;
    private RecyclerView recyclerView;
    private static final int REQUEST_CODE_EDIT_DETAIL = 3;
    private BeltAdapter beltAdapter;
    private BeltDao beltDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        AppDatabase db = MainApplication.getDatabase();
        beltDao = db.beltDao();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> showAddBeltDialog());

        loadBelts();

        // Attach ItemTouchHelper to RecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                showDeleteConfirmationDialog(position);
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void showDeleteConfirmationDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Belt")
                .setMessage("Are you sure you want to delete this belt?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    Belt belt = beltAdapter.getBeltAtPosition(position);
                    beltDao.delete(belt); // Remove from database
                    beltAdapter.removeBelt(position); // Remove from adapter and notify
                })
                .setNegativeButton("Cancel", (dialog, which) -> beltAdapter.notifyItemChanged(position))
                .create()
                .show();
    }

    private void loadBelts() {
        List<Belt> beltList = beltDao.getAllBelts();
        beltAdapter = new BeltAdapter(beltList, belt -> {
            Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
            intent.putExtra("belt_id", belt.getId());
            startActivityForResult(intent, REQUEST_CODE_EDIT_DETAIL);
        });
        recyclerView.setAdapter(beltAdapter);
        int itemCount = beltAdapter.getItemCount();
    }

        private void showAddBeltDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Belt")
                .setView(getLayoutInflater().inflate(R.layout.dialog_add_belt, null))
                .setPositiveButton("Add", (dialog, id) -> {
                    AlertDialog alertDialog = (AlertDialog) dialog;
                    EditText editTextBeltName = alertDialog.findViewById(R.id.editTextBeltName);
                    String beltName = editTextBeltName.getText().toString();
                    addNewBelt(beltName);
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel())
                .setNeutralButton("Import from CSV", (dialog, id) -> {
                    Intent intent = new Intent(MainActivity.this, ImportActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_IMPORT);
                });
        builder.create().show();
    }



    private void addNewBelt(String beltName) {
        Map<String, String> details = new HashMap<>();
        details.put("conveyorName", beltName);
        Belt newBelt = new Belt(details);
        beltDao.insert(newBelt);
        loadBelts();  // Refresh the list
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMPORT && resultCode == RESULT_OK) {
            loadBelts();
        } else if ((requestCode == REQUEST_CODE_EDIT_LOG || requestCode == REQUEST_CODE_EDIT_DETAIL) && resultCode == RESULT_OK) {
            int beltId = data != null ? data.getIntExtra("belt_id", -1) : -1;
            if (beltId != -1) {
                refreshBelt(beltId);
            }
        }
    }

    private void refreshBelt(int beltId) {
        runOnUiThread(() -> {
            Belt updatedBelt = beltDao.getBeltById(beltId);
            int position = beltAdapter.getPositionById(beltId);
            if (position != -1) {
                beltAdapter.updateBeltAtPosition(position, updatedBelt);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBelts();  // Ensure the data is always refreshed when the activity is resumed
    }
}