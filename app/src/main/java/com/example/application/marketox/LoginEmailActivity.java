package com.example.application.marketox;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.application.marketox.databinding.ActivityLoginEmailBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginEmailActivity extends AppCompatActivity {

    private ActivityLoginEmailBinding binding;

    private static final String TAG = "LOGIN_EMAIL";

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding
        binding = ActivityLoginEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        // Login Button
        binding.loginBtn.setOnClickListener(v -> validateData());

        // Register Text
        binding.registerTv.setOnClickListener(v ->
                startActivity(new Intent(LoginEmailActivity.this, RegisterEmailActivity.class))

        );
        //
        binding.forgotPasswordTv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginEmailActivity.this, ForgotPasswordActivity.class));

            }

        });
    }



    // ---------------- VALIDATION ----------------
    private void validateData() {

        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();

        Log.d(TAG, "validateData: email = " + email);

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEt.setError("Invalid email address");
            binding.emailEt.requestFocus();
        }
        else if (password.isEmpty()) {
            binding.passwordEt.setError("Please enter password");
            binding.passwordEt.requestFocus();
        }
        else {
            loginUser();
        }
    }

    // ---------------- LOGIN ----------------
    private void loginUser() {

        progressDialog.setMessage("Logging in...");
        progressDialog.show();

        firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        progressDialog.dismiss();
                        Log.d(TAG, "onSuccess: Login successful");

                        startActivity(new Intent(LoginEmailActivity.this, MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        progressDialog.dismiss();
                        Log.e(TAG, "onFailure: ", e);

                        Utils.toast(LoginEmailActivity.this,
                                "Login failed: " + e.getMessage());
                    }
                });
    }
}
