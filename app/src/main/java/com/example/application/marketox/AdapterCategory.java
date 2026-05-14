package com.example.application.marketox;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.application.marketox.databinding.RowCategoryBinding;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.ArrayList;
import java.util.Random;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.HolderCategory> {

    // REMOVED: private RowCategoryBinding binding; (Don't put binding here!)

    private Context context;
    private ArrayList<ModelCategory> categoryArrayList;
    private RvListenerCategory rvListenerCategory;

    public AdapterCategory(Context context, ArrayList<ModelCategory> categoryArrayList, RvListenerCategory rvListenerCategory) {
        this.context = context;
        this.categoryArrayList = categoryArrayList;
        this.rvListenerCategory = rvListenerCategory;
    }

    @NonNull
    @Override
    public HolderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create binding LOCALLY for each row
        RowCategoryBinding rowBinding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderCategory(rowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCategory holder, int position) {
        ModelCategory modelCategory = categoryArrayList.get(position);

        String category = modelCategory.getCategory();
        int icon = modelCategory.getIcon();

        // Set data using the binding from the holder
        holder.binding.categoryTitleTv.setText(category);
        holder.binding.categoryIconIv.setImageResource(icon);

        // Random color logic
        Random random = new Random();
        int color = Color.argb(255, random.nextInt(255), random.nextInt(255), random.nextInt(255));
        holder.binding.categoryIconIv.setBackgroundColor(color);

        holder.itemView.setOnClickListener(v -> {
            rvListenerCategory.onCategoryClick(modelCategory);
        });
    }

    @Override
    public int getItemCount() {
        return categoryArrayList.size();
    }

    class HolderCategory extends RecyclerView.ViewHolder {

        // Store the binding inside the Holder
        RowCategoryBinding binding;

        public HolderCategory(@NonNull RowCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}