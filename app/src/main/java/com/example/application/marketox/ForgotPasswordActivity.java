package com.example.application.marketox;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.application.marketox.databinding.ActivityForgotPasswordBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;

    private static final String TAG="FORGOT_PASS_TAG";

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait ");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth=FirebaseAuth.getInstance();

        //toolbar Back btn
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });
        //submit btn
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();


            }
        });

    }
    private String email="";


    private void validateData(){
        Log.d(TAG,"validateData: validating data");

        email=binding.emailEt.getText().toString().trim();
        Log.d(TAG,"validateData: email: "+email);

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailEt.setError("Invalid Email Pattern!");
            binding.emailEt.requestFocus();
        }  else{
            sendPasswordRecoverInstructions();

        }

    }

    private  void sendPasswordRecoverInstructions(){
        Log.d(TAG,"sendPasswordRecoverInstructions: sending password recover instructions to "+email);
        progressDialog.setMessage("Sending password recover instructions to "+email);
        progressDialog.show();

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Utils.toast(ForgotPasswordActivity.this,"Instructions to reset password sent!"+email);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"onFailure: ",e);
                        progressDialog.dismiss();
                        Utils.toast(ForgotPasswordActivity.this,"Failed to send due to "+e.getMessage());

                    }
                });





    }
}