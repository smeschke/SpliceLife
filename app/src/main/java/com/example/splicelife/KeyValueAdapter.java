// File: KeyValueAdapter.java
package com.example.splicelife;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeyValueAdapter extends RecyclerView.Adapter<KeyValueAdapter.KeyValueViewHolder> {

    private List<Map.Entry<String, String>> keyValueList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Map.Entry<String, String> entry);
    }

    public KeyValueAdapter(Map<String, String> keyValueMap, OnItemClickListener onItemClickListener) {
        this.keyValueList = new ArrayList<>(keyValueMap.entrySet());
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public KeyValueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_key_value, parent, false);
        return new KeyValueViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull KeyValueViewHolder holder, int position) {
        Map.Entry<String, String> entry = keyValueList.get(position);
        holder.keyTextView.setText(entry.getKey());
        holder.valueTextView.setText(entry.getValue());

        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(entry));
    }

    @Override
    public int getItemCount() {
        return keyValueList.size();
    }

    public static class KeyValueViewHolder extends RecyclerView.ViewHolder {
        public TextView keyTextView;
        public TextView valueTextView;

        public KeyValueViewHolder(View view) {
            super(view);
            keyTextView = view.findViewById(R.id.keyTextView);
            valueTextView = view.findViewById(R.id.valueTextView);
        }
    }
}
