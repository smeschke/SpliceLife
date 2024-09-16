package com.example.splicelife;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private final List<String> categories;
    private final Map<String, List<String>> categoryDetails;
    private final Context context;
    private final Belt belt;

    public CategoryAdapter(List<String> categories, Map<String, List<String>> categoryDetails, Context context, Belt belt) {
        this.categories = categories;
        this.categoryDetails = categoryDetails;
        this.context = context;
        this.belt = belt;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String categoryKey = categories.get(position);
        holder.categoryTitle.setText(categoryKey.replace("_", " ").toUpperCase()); // Display key as uppercase for readability

        StringBuilder details = new StringBuilder();
        List<String> detailsList = categoryDetails.get(categoryKey);
        if (detailsList != null) {
            for (String detail : detailsList) {
                details.append(detail).append("\n\n");
            }
        }

        holder.categoryDetails.setText(details.toString().trim());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditDetailActivity.class);
            intent.putExtra("category", categoryKey);
            intent.putExtra("belt_id", belt.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryTitle;
        TextView categoryDetails;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTitle = itemView.findViewById(R.id.categoryTitle);
            categoryDetails = itemView.findViewById(R.id.categoryDetails);
        }
    }
}
