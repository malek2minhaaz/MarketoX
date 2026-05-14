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

import com.example.application.marketox.databinding.FragmentMyAdsFavBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyAdsFav extends Fragment {

    private FragmentMyAdsFavBinding binding;
    private static final String TAG = "FAV_TAG";

    private Context mContext;
    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelAd> adArrayList;
    private AdapterAd adapterAd;

    public MyAdsFav() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMyAdsFavBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        // 1. Initialize List and Adapter once during setup
        adArrayList = new ArrayList<>();
        adapterAd = new AdapterAd(mContext, adArrayList);
        binding.adsRv.setAdapter(adapterAd);

        if (firebaseAuth.getCurrentUser() != null) {
            loadAds();
        } else {
            Log.e(TAG, "onViewCreated: User not logged in!");
        }
    }

    private void loadAds() {
        Log.d(TAG, "loadAds: Loading favorite ads...");

        DatabaseReference favRef = FirebaseDatabase.getInstance().getReference("Users");
        favRef.child(firebaseAuth.getUid()).child("Favourites")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Clear the list before adding new data to prevent duplicates
                        adArrayList.clear();

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            // Get the adId from the Favorites node
                            String adId = "" + ds.child("adId").getValue();
                            Log.d(TAG, "onDataChange: Found favorite adId: " + adId);

                            // Fetch the full details of the ad using the adId
                            DatabaseReference adRef = FirebaseDatabase.getInstance().getReference("Ads");
                            adRef.child(adId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot adSnapshot) {
                                    ModelAd modelAd = adSnapshot.getValue(ModelAd.class);

                                    if (modelAd != null) {
                                        adArrayList.add(modelAd);
                                        // Notify the adapter that an item was added
                                        adapterAd.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e(TAG, "onCancelled: " + error.getMessage());
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "onCancelled: " + error.getMessage());
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}