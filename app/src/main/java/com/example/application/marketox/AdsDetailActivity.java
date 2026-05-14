package com.example.application.marketox;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.application.marketox.databinding.ActivityAdsDetailBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;

public class AdsDetailActivity extends AppCompatActivity {

    private ActivityAdsDetailBinding binding;
    private static final String TAG = "AD_DETAILS_TAG";

    private FirebaseAuth firebaseAuth;

    private String adId = "";
    private double adLatitude = 0;
    private double adLongitude = 0;
    private String sellerUid = "";
    private String sellerPhone = "";

    private boolean favorite = false;
    private ArrayList<ModelImageSlider> imageSliderArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Proper ViewBinding
        binding = ActivityAdsDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        adId = getIntent().getStringExtra("adId");
        Log.d(TAG, "onCreate: adId = " + adId);

        if (adId == null) {
            finish();
            return;
        }

        if (firebaseAuth.getCurrentUser() != null) {
            checkIsFavorite();
        }

        loadAdDetails();
        loadAdImages();

        // Back
        binding.toolbarBackBtn.setOnClickListener(v -> onBackPressed());

        // Delete
        binding.toolbarDeleteBtn.setOnClickListener(v -> confirmDelete());

        // Favorite
        binding.toolbarFavBtn.setOnClickListener(v -> {
            if (favorite) {
                Utils.removeFromFavourite(this, adId);
            } else {
                Utils.addToFavourite(this, adId);
            }
        });

        // Buttons
        binding.callBtn.setOnClickListener(v ->
                Utils.callIntent(this, sellerPhone));

        binding.smsBtn.setOnClickListener(v ->
                Utils.smsIntent(this, sellerPhone));

        binding.mapBtn.setOnClickListener(v ->
                Utils.mapIntent(this, adLatitude, adLongitude));

        binding.chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(AdsDetailActivity.this,ChatActivity.class);
                intent.putExtra("receiptUid",sellerUid);
                startActivity(intent);

            }
        });
    }




    // ================= LOAD AD DETAILS =================
    private void loadAdDetails() {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Ads");

        ref.child(adId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        ModelAd modelAd = snapshot.getValue(ModelAd.class);

                        if (modelAd == null) {
                            Log.d(TAG, "Ad not found");
                            return;
                        }

                        sellerUid = modelAd.getUid();

                        binding.titleTv.setText(modelAd.getTitle());
                        binding.descriptionTv.setText(modelAd.getDescription());
                        binding.addressTv.setText(modelAd.getAddress());
                        binding.priceTv.setText(modelAd.getPrice());
                        binding.conditionTv.setText(modelAd.getCondition());

                        adLatitude = modelAd.getLatitude();
                        adLongitude = modelAd.getLongitude();

                        String formattedDate =
                                Utils.formatTimestampDateTime(modelAd.getTimestamp());

                        binding.dateTv.setText(formattedDate);

                        // 🔥 OWNER vs OTHER USER LOGIC
                        if (firebaseAuth.getCurrentUser() != null
                                && sellerUid != null
                                && sellerUid.equals(firebaseAuth.getUid())) {

                            // Owner
                            binding.toolbarDeleteBtn.setVisibility(View.VISIBLE);
                            binding.toolbarEditBtn.setVisibility(View.VISIBLE);
                            binding.toolbarFavBtn.setVisibility(View.GONE);

                            binding.chatBtn.setVisibility(View.GONE);
                            binding.callBtn.setVisibility(View.GONE);
                            binding.smsBtn.setVisibility(View.GONE);

                        } else {

                            // Other User
                            binding.toolbarDeleteBtn.setVisibility(View.GONE);
                            binding.toolbarEditBtn.setVisibility(View.GONE);
                            binding.toolbarFavBtn.setVisibility(View.VISIBLE);

                            binding.chatBtn.setVisibility(View.VISIBLE);
                            binding.callBtn.setVisibility(View.VISIBLE);
                            binding.smsBtn.setVisibility(View.VISIBLE);
                        }

                        loadSellerDetails();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "loadAdDetails: ", error.toException());
                    }
                });
    }

    // ================= LOAD SELLER =================
    private void loadSellerDetails() {

        if (sellerUid == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Users");

        ref.child(sellerUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String phoneCode = "" + snapshot.child("phoneCode").getValue();
                        String phoneNumber = "" + snapshot.child("phoneNumber").getValue();
                        String name = "" + snapshot.child("name").getValue();
                        String profileImageUrl = "" + snapshot.child("profileImageUrl").getValue();

                        Long timestampLong =
                                snapshot.child("timestamp").getValue(Long.class);

                        long timestamp = timestampLong != null ? timestampLong : 0;

                        String formattedDate =
                                Utils.formatTimestampDateTime(timestamp);

                        sellerPhone = phoneCode + phoneNumber;

                        binding.sellerNameTv.setText(name);
                        binding.memberSinceTv.setText(formattedDate);

                        Glide.with(AdsDetailActivity.this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.ic_person2_white)
                                .into(binding.sellerProfileIv);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "loadSellerDetails: ", error.toException());
                    }
                });
    }

    // ================= FAVORITE CHECK =================
    private void checkIsFavorite() {

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Users");

        ref.child(firebaseAuth.getUid())
                .child("Favorites")
                .child(adId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        favorite = snapshot.exists();

                        if (favorite) {
                            binding.toolbarFavBtn
                                    .setImageResource(R.drawable.ic_fav_yes);
                        } else {
                            binding.toolbarFavBtn
                                    .setImageResource(R.drawable.ic_fav_no);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    // ================= LOAD IMAGES =================
    private void loadAdImages() {

        imageSliderArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Ads");

        ref.child(adId)
                .child("Images")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        imageSliderArrayList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelImageSlider model =
                                    ds.getValue(ModelImageSlider.class);
                            if (model != null)
                                imageSliderArrayList.add(model);
                        }

                        AdapterImageSlider adapter =
                                new AdapterImageSlider(
                                        AdsDetailActivity.this,
                                        imageSliderArrayList);

                        binding.imageSlidervp.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    // ================= DELETE =================
    private void confirmDelete() {

        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Ad")
                .setMessage("Are you sure you want to delete this ad?")
                .setPositiveButton("DELETE", (dialog, which) -> deleteAd())
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void deleteAd() {

        DatabaseReference ref =
                FirebaseDatabase.getInstance().getReference("Ads");

        ref.child(adId)
                .removeValue()
                .addOnSuccessListener(unused -> {
                    Utils.toast(this, "Ad deleted");
                    finish();
                })
                .addOnFailureListener(e ->
                        Utils.toast(this,
                                "Failed: " + e.getMessage()));
    }
}