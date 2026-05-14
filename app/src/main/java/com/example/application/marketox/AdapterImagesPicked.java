package com.example.application.marketox;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// Removed ArrayAdapter import
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.application.marketox.databinding.RowImagesPickedBinding;

import java.util.ArrayList; // Added ArrayList import

public class AdapterImagesPicked extends RecyclerView.Adapter<AdapterImagesPicked.HolderImagesPicked>{

    private static final String TAG = "IMAGES_TAG";

    private Context context;
    // 1. Changed ArrayAdapter to ArrayList
    private ArrayList<ModelImagePicked> imagePickedArrayList;

    public AdapterImagesPicked(Context context, ArrayList<ModelImagePicked> imagePickedArrayList) {
        this.context = context;
        this.imagePickedArrayList = imagePickedArrayList;
    }

    @NonNull
    @Override
    public HolderImagesPicked onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 2. Inflate binding inside onCreateViewHolder locally
        RowImagesPickedBinding binding = RowImagesPickedBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderImagesPicked(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderImagesPicked holder, int position) {
        // 3. .get(position) now works because we changed to ArrayList
        ModelImagePicked modelImagePicked = imagePickedArrayList.get(position);
        Uri imageuri = modelImagePicked.getImageuri();

        Log.d(TAG, "onBindViewHolder: imageuri " + imageuri);

        try {
            Glide.with(context)
                    .load(imageuri)
                    .placeholder(R.drawable.ic_image_gray)
                    .into(holder.Imageiv);
        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder: ", e);
        }

        holder.closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 4. Fixed: using 'modelImagePicked' (the correct variable) instead of 'model'
                imagePickedArrayList.remove(modelImagePicked);
                notifyItemRemoved(holder.getAdapterPosition());
                notifyItemRangeChanged(holder.getAdapterPosition(), imagePickedArrayList.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        // 5. .size() now works because we changed to ArrayList
        return imagePickedArrayList.size();
    }

    class HolderImagesPicked extends RecyclerView.ViewHolder {

        ImageView Imageiv;
        ImageButton closeBtn;

        public HolderImagesPicked(@NonNull RowImagesPickedBinding binding) {
            super(binding.getRoot());

            Imageiv = binding.Imageiv;
            closeBtn = binding.closeBtn;
        }
    }
}