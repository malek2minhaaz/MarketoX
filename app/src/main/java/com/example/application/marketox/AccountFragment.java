package com.example.application.marketox;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.application.marketox.databinding.FragmentAccountBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;

    private FirebaseAuth firebaseAuth;

    private static final String TAG = "ACCOUNT_TAG";

    private Context mContext;

    @Override
    public void onAttach(Context context) {
        mContext = context;
        super.onAttach(context);
    }

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAccountBinding.inflate(LayoutInflater.from(mContext), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        loadMyinfo();

        binding.logoutCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                startActivity(new Intent(mContext, MainActivity.class));
                getActivity().finishAffinity();
            }
        });

        binding.editprofileCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, ProfileEditActivity.class));
            }
        });
        binding.changePasswordCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext,ChangePasswordActivity.class));

            }
        });
    }

    private void loadMyinfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Fetch data from snapshot
                        String dob = "" + snapshot.child("dob").getValue();
                        String email = "" + snapshot.child("email").getValue();
                        String name = "" + snapshot.child("name").getValue();
                        String phoneCode = "" + snapshot.child("phoneCode").getValue();
                        String phoneNumber = "" + snapshot.child("phoneNumber").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String userType = "" + snapshot.child("userType").getValue();
                        String profileImageUrl = "" + snapshot.child("profileImageUrl").getValue();

                        String phone = phoneCode + phoneNumber;

                        if (timestamp.equals("null")) {
                            timestamp = "0";
                        }

                        // Convert timestamp to human-readable date for "Member Since"
                        String formattedDate = Utils.formatTimestampDateTime(Long.parseLong(timestamp));

                        // Set data to UI
                        binding.emailTv.setText(email);
                        binding.nameTv.setText(name);

                        // FIX: Use the 'dob' variable here instead of 'formattedDate'
                        binding.dobTv.setText(dob);

                        // Use 'formattedDate' only for account creation date
                        binding.memberSinceTv.setText(formattedDate);
                        binding.phoneTv.setText(phone);

                        if (userType.equals("Email")) {
                            if (firebaseAuth.getCurrentUser() != null) {
                                boolean isVerified = firebaseAuth.getCurrentUser().isEmailVerified();
                                if (isVerified) {
                                    binding.verificationTv.setText("Verified");
                                } else {
                                    binding.verificationTv.setText("Not Verified");
                                }
                            }
                        } else {
                            // Phone and Google users are usually verified by default
                            binding.verificationTv.setText("Verified");
                        }

                        try {
                            Glide.with(mContext)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.ic_person2_white)
                                    .into(binding.profileIv);
                        } catch (Exception e) {
                            Log.e(TAG, "onDataChange Glide error:", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "onCancelled: ", error.toException());
                    }
                });
    }
}