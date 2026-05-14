package com.example.application.marketox;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.application.marketox.databinding.ActivityRegisterEmailBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterEmailActivity extends AppCompatActivity {

    private ActivityRegisterEmailBinding binding;

    private static final String TAG = "REGISTER_EMAIL";

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private String email, password, confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        // Register Button
        binding.registerBtn.setOnClickListener(v -> validateData());

        // Already have account → Login
        binding.haveAccountTv.setOnClickListener(v ->
                startActivity(new Intent(RegisterEmailActivity.this, LoginEmailActivity.class))
        );
    }

    // ---------------- VALIDATION ----------------
    private void validateData() {

        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();
        confirmPassword = binding.cPpasswordEt.getText().toString().trim();

        Log.d(TAG, "validateData: email=" + email);

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEt.setError("Invalid email address");
            binding.emailEt.requestFocus();
        }
        else if (password.isEmpty()) {
            binding.passwordEt.setError("Enter password");
            binding.passwordEt.requestFocus();
        }
        else if (password.length() < 6) {
            binding.passwordEt.setError("Password must be at least 6 characters");
            binding.passwordEt.requestFocus();
        }
        else if (!password.equals(confirmPassword)) {
            binding.cPpasswordEt.setError("Passwords do not match");
            binding.cPpasswordEt.requestFocus();
        }
        else {
            createAccount();
        }
    }

    // ---------------- CREATE ACCOUNT ----------------
    private void createAccount() {

        progressDialog.setMessage("Creating account...");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG, "onSuccess: Account created");
                        saveUserToDatabase();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.e(TAG, "onFailure: ", e);
                        Utils.toast(RegisterEmailActivity.this,
                                "Registration failed: " + e.getMessage());
                    }
                });
    }

    // ---------------- SAVE USER INFO ----------------
    private void saveUserToDatabase() {

        progressDialog.setMessage("Saving user information...");

        String uid = firebaseAuth.getUid();
        String userEmail = firebaseAuth.getCurrentUser().getEmail();
        long timestamp = System.currentTimeMillis();

        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("uid", uid);
        userMap.put("email", userEmail);
        userMap.put("name", "");
        userMap.put("phone", "");
        userMap.put("profileImageUrl", "");
        userMap.put("userType", "Email");
        userMap.put("onlineStatus", true);
        userMap.put("timestamp", timestamp);

        DatabaseReference reference =
                FirebaseDatabase.getInstance().getReference("Users");

        reference.child(uid)
                .setValue(userMap)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Log.d(TAG, "onSuccess: User saved");

                    startActivity(new Intent(RegisterEmailActivity.this, LoginEmailActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "onFailure: ", e);

                    Utils.toast(RegisterEmailActivity.this,
                            "Failed to save data: " + e.getMessage());
                });
    }
}
