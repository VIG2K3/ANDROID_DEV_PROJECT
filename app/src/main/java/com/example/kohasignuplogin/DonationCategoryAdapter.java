package com.example.kohasignuplogin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DonationCategoryAdapter extends RecyclerView.Adapter<DonationCategoryAdapter.ViewHolder> {

    private List<DonationCategory> categoryList;

    public DonationCategoryAdapter(List<DonationCategory> categoryList) {
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_donation_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DonationCategory category = categoryList.get(position);
        holder.textCategoryName.setText(category.getCategory());
        holder.textDescription.setText(category.getDescription());
        holder.textPoints.setText("Points: " + category.getPoints());
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textCategoryName, textDescription, textPoints;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textCategoryName = itemView.findViewById(R.id.textCategoryName);
            textDescription = itemView.findViewById(R.id.textDescription);
            textPoints = itemView.findViewById(R.id.textPoints);
        }
    }
}
