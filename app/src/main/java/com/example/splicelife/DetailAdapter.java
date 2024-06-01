package com.example.splicelife;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DetailAdapter extends RecyclerView.Adapter<DetailAdapter.DetailViewHolder> {
    private List<String> details;
    private final OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(String detail);
    }

    public DetailAdapter(List<String> details, OnItemClickListener onItemClickListener) {
        this.details = details;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public DetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_detail, parent, false);
        return new DetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailViewHolder holder, int position) {
        String detail = details.get(position);
        holder.detailTextView.setText(detail);
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(detail));
    }

    @Override
    public int getItemCount() {
        return details.size();
    }

    static class DetailViewHolder extends RecyclerView.ViewHolder {
        TextView detailTextView;

        public DetailViewHolder(@NonNull View itemView) {
            super(itemView);
            detailTextView = itemView.findViewById(R.id.detailTextView);
        }
    }
}
