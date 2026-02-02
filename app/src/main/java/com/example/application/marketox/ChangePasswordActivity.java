package com.example.application.marketox;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.application.marketox.databinding.ActivityChangePasswordBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private ActivityChangePasswordBinding binding;

    private  static  final String TAG="CHANGE_PASS_TAG";
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();


        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });
        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();

            }
        });
    }
    private String currentPassword="",newPassword="",confirmNewPassword="";

    private void validateData(){
        Log.d(TAG,"validateData :");
        currentPassword=binding.currentPasswordEt.getText().toString().trim();
        newPassword=binding.newPasswordEt.getText().toString().trim();
        confirmNewPassword=binding.confirmnewPasswordEt.getText().toString().trim();

        Log.d(TAG,"validateData: currentPassword:"+currentPassword);
        Log.d(TAG,"validateData: newPassword:"+newPassword);
        Log.d(TAG,"validateData: confirmNewPassword:"+confirmNewPassword);


        if(currentPassword.isEmpty()){
            binding.currentPasswordEt.setError("Enter Your Current Password");
            binding.currentPasswordEt.requestFocus();

        } else if (newPassword.isEmpty()) {
            binding.newPasswordEt.setError("Enter new Password");
            binding.newPasswordEt.requestFocus();
        } else if (confirmNewPassword.isEmpty()){
            binding.confirmnewPasswordEt.setError("Enter Confirm Password");
            binding.confirmnewPasswordEt.requestFocus();
        } else if (!newPassword.equals(confirmNewPassword)) {
            binding.confirmnewPasswordEt.setError("Password Doesn't Match");
            binding.confirmnewPasswordEt.requestFocus();
        }else{
            authenticateUserForUpdatePassword();


        }
    }
    private void authenticateUserForUpdatePassword(){
        Log.d(TAG,"authenticateUserForUpdatePassword: ");
        progressDialog.setMessage("Authenticating User...");
        progressDialog.show();

        AuthCredential authCredential= EmailAuthProvider.getCredential(firebaseUser.getEmail(),currentPassword);
        firebaseUser.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        updatePassword();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"onFailure : ",e);
                        progressDialog.dismiss();
                        Utils.toast(ChangePasswordActivity.this,"Authentication Failed due to "+e.getMessage());


                    }
                });


    }
    private void updatePassword(){
        Log.d(TAG,"updatePassword :");
        progressDialog.setMessage("Updating Password...");
        progressDialog.show();

        firebaseUser.updatePassword(newPassword)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Utils.toast(ChangePasswordActivity.this,"Password Updated Successfully");


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"OnFailure : ",e);
                        progressDialog.dismiss();
                        Utils.toast(ChangePasswordActivity.this,"Failed to Update Password due to "+e.getMessage());

                    }
                });


    }
}