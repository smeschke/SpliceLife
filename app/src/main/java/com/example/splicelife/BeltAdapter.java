package com.example.splicelife;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BeltAdapter extends RecyclerView.Adapter<BeltAdapter.BeltViewHolder> {
    private final OnItemClickListener onItemClickListener;
    private List<Belt> beltList;

    public BeltAdapter(List<Belt> beltList, OnItemClickListener onItemClickListener) {
        this.beltList = beltList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public BeltViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_belt, parent, false);
        return new BeltViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BeltViewHolder holder, int position) {
        Belt belt = beltList.get(position);
        final String[] keys = {"Conveyor Name", "Company Name", "Conveyor Location", "Belt Width", "Net Endless Length", "Tensile Strength", "Number or Plies/Cords", "Top Cover Thickness", "Bottom Cover Thickness", "Rubber Type"};

        SpannableStringBuilder callout = new SpannableStringBuilder();

// Add conveyorName in bold
        String conveyorName = belt.getDetails().get(keys[0]) + "\n";
        SpannableString boldConveyorName = new SpannableString(conveyorName);
        boldConveyorName.setSpan(new StyleSpan(Typeface.BOLD), 0, conveyorName.length(), 0);
        callout.append(boldConveyorName);

// Add the rest of the details
        callout.append(belt.getDetails().get(keys[1]) + "\n");
        callout.append(belt.getDetails().get(keys[2]) + "\n\n");
        callout.append(belt.getDetails().get(keys[3]) + " X " + belt.getDetails().get(keys[4]) + "\n");
        callout.append(belt.getDetails().get(keys[6]) + " / " + belt.getDetails().get(keys[5]) + "\n");
        callout.append(belt.getDetails().get(keys[7]) + " X " + belt.getDetails().get(keys[8]));

        if (callout.length() == 0) {
            callout.append("No belt name entered");
        }

        holder.beltNameTextView.setText(callout);

        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(belt));

        // Fetch and set log details
        String logDetails = belt.getDetails().get("Logs");
        if (logDetails == null || logDetails.isEmpty()) {
            logDetails = "\n\ninitial commit\nmaintenance log\n";
        }
        String[] logLines = logDetails.split("\n");
        StringBuilder truncatedLog = new StringBuilder("");

        if (logLines.length > 3) {
            for (int i = 0; i < 3; i++) {
                truncatedLog.append(logLines[i]).append("\n");
            }
            truncatedLog.append(".....");
        } else {
            truncatedLog.append(logDetails);
        }
        holder.logDetailsTextView.setText(truncatedLog);

        holder.logDetailsTextView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), EditLogActivity.class);
            intent.putExtra("belt_id", belt.getId());
            ((Activity) holder.itemView.getContext()).startActivityForResult(intent, MainActivity.REQUEST_CODE_EDIT_LOG);
        });


    }

    @Override
    public int getItemCount() {
        return beltList.size();
    }

    public void updateBeltList(List<Belt> newBeltList) {
        this.beltList = newBeltList;
        notifyDataSetChanged();
    }

    public void removeBelt(int position) {
        beltList.remove(position);
        notifyItemRemoved(position);
    }

    public Belt getBeltAtPosition(int position) {
        return beltList.get(position);
    }

    public int getPositionById(int beltId) {
        for (int i = 0; i < beltList.size(); i++) {
            if (beltList.get(i).getId() == beltId) {
                return i;
            }
        }
        return -1;
    }

    public void updateBeltAtPosition(int position, Belt updatedBelt) {
        beltList.set(position, updatedBelt);
        notifyItemChanged(position);
    }

    public interface OnItemClickListener {
        void onItemClick(Belt belt);
    }

    public static class BeltViewHolder extends RecyclerView.ViewHolder {
        TextView beltNameTextView;
        TextView logDetailsTextView;

        public BeltViewHolder(@NonNull View itemView) {
            super(itemView);
            beltNameTextView = itemView.findViewById(R.id.beltName);
            logDetailsTextView = itemView.findViewById(R.id.logDetails);
        }
    }
}
