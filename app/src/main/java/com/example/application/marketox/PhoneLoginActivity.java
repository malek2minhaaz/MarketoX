package com.example.application.marketox;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.application.marketox.databinding.ActivityPhoneLoginBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private ActivityPhoneLoginBinding binding;

    private static final String TAG = "PHONE_LOGIN";

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private PhoneAuthProvider.ForceResendingToken forceResendingToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String verificationId;

    private String phoneCode, phoneNumber, phoneNumberWithCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPhoneLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        // Initial UI
        binding.phoneInputRl.setVisibility(View.VISIBLE);
        binding.optInputUrl.setVisibility(View.GONE);

        initCallbacks();

        // Back


        // Send OTP
        binding.sendOtpBtn.setOnClickListener(v -> validatePhone());

        // Verify OTP
        binding.verifyOtpBtn.setOnClickListener(v -> verifyOtp());

        // Resend OTP
        binding.resendOtpTv.setOnClickListener(v -> resendOtp());
    }

    // ---------------- VALIDATION ----------------
    private void validatePhone() {

        phoneCode = binding.phoneCodeTil.getSelectedCountryCodeWithPlus();
        phoneNumber = binding.phoneNumberEt.getText().toString().trim();
        phoneNumberWithCode = phoneCode + phoneNumber;

        if (phoneNumber.isEmpty() || phoneNumber.length() < 6) {
            binding.phoneNumberEt.setError("Enter valid phone number");
            binding.phoneNumberEt.requestFocus();
            return;
        }

        sendOtp();
    }

    // ---------------- SEND OTP ----------------
    private void sendOtp() {

        progressDialog.setMessage("Sending OTP to " + phoneNumberWithCode);
        progressDialog.show();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phoneNumberWithCode)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(callbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // ---------------- CALLBACKS ----------------
    private void initCallbacks() {

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                signInWithCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                progressDialog.dismiss();
                Utils.toast(PhoneLoginActivity.this, e.getMessage());
            }

            @Override
            public void onCodeSent(@NonNull String id,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                verificationId = id;
                forceResendingToken = token;

                progressDialog.dismiss();

                binding.phoneInputRl.setVisibility(View.GONE);
                binding.optInputUrl.setVisibility(View.VISIBLE);

                binding.loginLabelTv.setText(
                        "Enter OTP sent to " + phoneNumberWithCode
                );

                Utils.toast(PhoneLoginActivity.this, "OTP sent");
            }
        };
    }

    // ---------------- VERIFY OTP ----------------
    private void verifyOtp() {

        String otp = binding.otpEt.getText().toString().trim();

        if (otp.isEmpty() || otp.length() < 6) {
            binding.otpEt.setError("Enter valid OTP");
            binding.otpEt.requestFocus();
            return;
        }

        progressDialog.setMessage("Verifying OTP");
        progressDialog.show();

        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(verificationId, otp);

        signInWithCredential(credential);
    }

    // ---------------- RESEND OTP ----------------
    private void resendOtp() {

        progressDialog.setMessage("Resending OTP...");
        progressDialog.show();

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(phoneNumberWithCode)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(callbacks)
                        .setForceResendingToken(forceResendingToken)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // ---------------- SIGN IN ----------------
    private void signInWithCredential(PhoneAuthCredential credential) {

        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {

                    if (authResult.getAdditionalUserInfo().isNewUser()) {
                        saveUserToDatabase();
                    } else {
                        startActivity(new Intent(this, MainActivity.class));
                        finishAffinity();
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Utils.toast(this, e.getMessage());
                });
    }

    // ---------------- SAVE USER ----------------
    private void saveUserToDatabase() {

        progressDialog.setMessage("Saving user info...");
        progressDialog.show();

        String uid = firebaseAuth.getUid();
        long timestamp = System.currentTimeMillis();

        HashMap<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("email", "");
        map.put("phone", phoneNumber);
        map.put("phoneCode", phoneCode);
        map.put("name", "");
        map.put("profileImageUrl", "");
        map.put("userType", "Phone");
        map.put("onlineStatus", true);
        map.put("timestamp", timestamp);

        DatabaseReference ref =
                FirebaseDatabase.getInstance().getReference("Users");

        ref.child(uid)
                .setValue(map)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    startActivity(new Intent(this, MainActivity.class));
                    finishAffinity();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Utils.toast(this, e.getMessage());
                });
    }
}
