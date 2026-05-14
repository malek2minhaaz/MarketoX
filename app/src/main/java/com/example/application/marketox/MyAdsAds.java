package com.example.application.marketox;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.application.marketox.databinding.FragmentMyAdsAdsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyAdsAds extends Fragment {

    private FragmentMyAdsAdsBinding binding;
    private static final String TAG = "MY_ADS_TAG";

    private Context mContext;
    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelAd> adArrayList;
    private AdapterAd adapterAd;

    public MyAdsAds() {
        // Required empty public constructor
    }

    // ✅ FIX 1: Set context properly
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    // ✅ FIX 2: Initialize binding properly
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentMyAdsAdsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        // Safety check
        if (firebaseAuth.getCurrentUser() == null) {
            Log.e(TAG, "User not logged in!");
            return;
        }

        loadAds();
    }

    private void loadAds() {

        adArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");

        ref.orderByChild("uid")
                .equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        adArrayList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {

                            try {
                                ModelAd modelAd = ds.getValue(ModelAd.class);

                                // ✅ Null safety added
                                if (modelAd != null) {
                                    adArrayList.add(modelAd);
                                }

                            } catch (Exception e) {
                                Log.e(TAG, "Parsing error: ", e);
                            }
                        }

                        adapterAd = new AdapterAd(mContext, adArrayList);
                        binding.adsRv.setAdapter(adapterAd);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Database error: " + error.getMessage());
                    }
                });
    }

    // Optional but recommended (memory safe)
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}