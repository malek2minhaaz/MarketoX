package com.example.application.marketox;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.application.marketox.databinding.FragmentHomeBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private Context mContext;

    private ArrayList<ModelAd> adArrayList;
    private AdapterAd adapterAd;

    private SharedPreferences locationSp;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private String currentAddress = "";


    private static final int MAX_DISTANCE_TO_LOADS_ADS_Km =50;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater,
                                          android.view.ViewGroup container,
                                          Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull android.view.View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.categoriesRv.setLayoutManager(
                new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));

        binding.adsRv.setLayoutManager(new LinearLayoutManager(mContext));

        //  Initialize adapter ONLY ONCE
        adArrayList = new ArrayList<>();
        adapterAd = new AdapterAd(mContext, adArrayList);
        binding.adsRv.setAdapter(adapterAd);

        // Load saved location
        locationSp = mContext.getSharedPreferences("LOCATION_SP", Context.MODE_PRIVATE);
        currentLatitude = locationSp.getFloat("CURRENT_LATITUDE", 0.0f);
        currentLongitude = locationSp.getFloat("CURRENT_LONGITUDE", 0.0f);
        currentAddress = locationSp.getString("CURRENT_ADDRESS", "Select Location");

        binding.locationTv.setText(currentAddress);

        loadCategories();
        loadAds("All");

        binding.locationCv.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, LocationPickerActivity.class);
            locationPickerActivityResult.launch(intent);
        });
    }

    // ================= LOCATION RESULT =================

    private final ActivityResultLauncher<Intent> locationPickerActivityResult =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == -1 && result.getData() != null) {

                            currentLatitude = result.getData().getDoubleExtra("latitude", 0.0);
                            currentLongitude = result.getData().getDoubleExtra("longitude", 0.0);
                            currentAddress = result.getData().getStringExtra("address");

                            locationSp.edit()
                                    .putFloat("CURRENT_LATITUDE", (float) currentLatitude)
                                    .putFloat("CURRENT_LONGITUDE", (float) currentLongitude)
                                    .putString("CURRENT_ADDRESS", currentAddress)
                                    .apply();

                            binding.locationTv.setText(currentAddress);

                            loadAds("All");
                        }
                    });

    // ================= LOAD CATEGORIES =================

    private void loadCategories() {

        ArrayList<ModelCategory> categoryArrayList = new ArrayList<>();
        categoryArrayList.add(new ModelCategory("All", R.drawable.ic_category_all));

        int length = Math.min(Utils.categories.length, Utils.categoryIcons.length);

        for (int i = 0; i < length; i++) {
            categoryArrayList.add(
                    new ModelCategory(Utils.categories[i], Utils.categoryIcons[i]));
        }

        AdapterCategory adapterCategory =
                new AdapterCategory(mContext, categoryArrayList,
                        modelCategory -> loadAds(modelCategory.getCategory()));

        binding.categoriesRv.setAdapter(adapterCategory);
    }

    // ================= LOAD ADS =================
    private void loadAds(String category) {

        DatabaseReference ref =
                FirebaseDatabase.getInstance().getReference("Ads");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                adArrayList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {

                    ModelAd modelAd = ds.getValue(ModelAd.class);
                    if (modelAd == null) continue;

                    // Category filter only
                    if (!category.equals("All") &&
                            !modelAd.getCategory().equals(category)) {
                        continue;
                    }

                    adArrayList.add(modelAd);
                }

                adapterAd.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    // ================= DISTANCE CALCULATION =================

    private double calculateDistanceKm(double adLat, double adLong) {

        if (currentLatitude == 0.0 || adLat == 0.0) {
            return Double.MAX_VALUE;
        }

        Location start = new Location("start");
        start.setLatitude(currentLatitude);
        start.setLongitude(currentLongitude);

        Location end = new Location("end");
        end.setLatitude(adLat);
        end.setLongitude(adLong);

        return start.distanceTo(end) / 1000;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}