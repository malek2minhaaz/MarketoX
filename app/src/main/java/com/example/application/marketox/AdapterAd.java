package com.example.application.marketox;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.application.marketox.databinding.RowAdBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdapterAd extends RecyclerView.Adapter<AdapterAd.HolderAd> implements Filterable {

    private static final String TAG = "AD_ADAPTER_TAG";
    private Context context;
    public ArrayList<ModelAd> adArrayList; // List displayed in UI
    private ArrayList<ModelAd> filterList; // Backup list for searching
    private FilterAd filter;

    public AdapterAd(Context context, ArrayList<ModelAd> adArrayList) {
        this.context = context;
        this.adArrayList = adArrayList;
        // FIX: Create a NEW ArrayList copy so filterList isn't cleared when adArrayList is
        this.filterList = new ArrayList<>(adArrayList);
    }

    @NonNull
    @Override
    public HolderAd onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowAdBinding rowBinding = RowAdBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderAd(rowBinding);
    }


    @Override
    public void onBindViewHolder(@NonNull HolderAd holder, int position) {
        ModelAd modelAd = adArrayList.get(position);

        // ... existing UI binding code ...
        holder.binding.titleTv.setText(modelAd.getTitle());
        holder.binding.descriptionTv.setText(modelAd.getDescription());
        holder.binding.addressTv.setText(modelAd.getAddress());
        holder.binding.conditionTv.setText(modelAd.getCondition());
        holder.binding.priceTv.setText(modelAd.getPrice());
        holder.binding.dateTv.setText(Utils.formatTimestampDateTime(modelAd.getTimestamp()));

        // Check if the item is in favorites to set the correct icon
        checkIsFavorite(modelAd, holder);

        loadFirstImage(modelAd, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context,AdsDetailActivity.class);
                intent.putExtra("adId",modelAd.getId());
                context.startActivity(intent);

            }
        });

        holder.binding.favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Access Firebase Auth here
                com.google.firebase.auth.FirebaseAuth firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance();

                if (firebaseAuth.getCurrentUser() == null) {
                    // User not logged in
                    Utils.toast(context, "You are not logged in");
                } else {
                    // User is logged in, handle favorite logic
                    boolean favourite = modelAd.isFavourite();
                    if (favourite) {
                        Utils.removeFromFavourite(context, modelAd.getId());
                    } else {
                        Utils.addToFavourite(context, modelAd.getId());
                    }
                }
            }
        });
    }

    private void checkIsFavorite(ModelAd modelAd, HolderAd holder) {
        com.google.firebase.auth.FirebaseAuth firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            holder.binding.favBtn.setImageResource(R.drawable.ic_fav_yes); // Your default icon
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Favorites").child(modelAd.getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean favorite = snapshot.exists();
                        modelAd.setFavourite(favorite);
                        if (favorite) {
                            holder.binding.favBtn.setImageResource(R.drawable.ic_fav_yes);
                        } else {
                            holder.binding.favBtn.setImageResource(R.drawable.ic_fav_no);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }




    // IMPORTANT: Add this method to update the backup list when data changes in Home Fragment
    public void updateList(ArrayList<ModelAd> newList) {
        this.adArrayList = newList;
        this.filterList = new ArrayList<>(newList); // Refresh the backup list
        notifyDataSetChanged();
    }

    private void loadFirstImage(ModelAd modelAd, HolderAd holder) {
        String adId = modelAd.getId();
        if (adId == null) return;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Ads");
        reference.child(adId).child("Images").limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                String imageUrl = "" + ds.child("imageUrl").getValue();
                                Glide.with(context)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.ic_image_gray)
                                        .into(holder.binding.Imageiv);
                            }
                        } else {
                            holder.binding.Imageiv.setImageResource(R.drawable.ic_image_gray);
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    @Override
    public int getItemCount() {
        return adArrayList != null ? adArrayList.size() : 0;
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new FilterAd(this, filterList);
        }
        return filter;
    }

    class HolderAd extends RecyclerView.ViewHolder {
        RowAdBinding binding;
        public HolderAd(@NonNull RowAdBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}