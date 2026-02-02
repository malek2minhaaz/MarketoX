package com.example.application.marketox;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.application.marketox.databinding.ActivityLoginOptionBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginOptionActivity extends AppCompatActivity {


    private ActivityLoginOptionBinding binding;
    private static final String TAG = "LOGIN_OPTION_TAG";
    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginOptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // Instead of R.string.default_web_client_id
                .requestIdToken("392516370502-dth56vs8ocvtb9c2a93joa0vdj2gq0sc.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        binding.closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });
        //handle email login button

        binding.LoginEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginOptionActivity.this, LoginEmailActivity.class));

            }
        });

        binding.loginPhoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginOptionActivity.this, PhoneLoginActivity.class));

            }
        });


        binding.LogingoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginGoogleLogin();

            }
        });

    }

    private void beginGoogleLogin() {
        Log.d(TAG, "beginGoogleLogin: Begin Google Login");
        Intent googleSignInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignnInARL.launch(googleSignInIntent);
    }

    private ActivityResultLauncher<Intent> googleSignnInARL = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "onActivityResult: ");

                    // MODIFIED: We check the data regardless of the resultCode to catch errors like 12500 or 10
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        Log.d(TAG, "onActivityResult : Account ID : " + account.getId());
                        firebaseAuthWithGoogleAccount(account.getIdToken());
                    } catch (ApiException e) {
                        // This block will now run if there is a config error, showing you the real code
                        Log.e(TAG, "onActivityResult: Google Sign In Failed code=" + e.getStatusCode(), e);
                        Utils.toast(LoginOptionActivity.this, "Sign In Failed. Error Code: " + e.getStatusCode());
                    }
                }
            }
    );

    private void firebaseAuthWithGoogleAccount(String idToken) {
        Log.d(TAG, "firebaseAuthWithGoogleAccount: idToken " + idToken);

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        if (authResult.getAdditionalUserInfo().isNewUser()) {
                            Log.d(TAG, "OnSuccess: New User, Account Created");
                            updateUserInfoDb();
                        } else {
                            Log.d(TAG, "OnSuccess: Existing User,Logged In ");
                            startActivity(new Intent(LoginOptionActivity.this, MainActivity.class));
                            finishAffinity();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                    }
                });
    }

    private void updateUserInfoDb() {
        Log.d(TAG, "updateUserInfoDb: ");

        progressDialog.setMessage("Saving User Info");
        progressDialog.show();

        long timestamp = Utils.getTimestamp();
        String registerUserEmail = firebaseAuth.getCurrentUser().getEmail();
        String registerUserUid = firebaseAuth.getUid();
        String name = firebaseAuth.getCurrentUser().getDisplayName();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", registerUserUid);
        hashMap.put("email", registerUserEmail);
        hashMap.put("name", "" + name);
        hashMap.put("phone", "");
        hashMap.put("phoneCode", "");
        hashMap.put("profileImageUrl", "");
        hashMap.put("DOB ", "");
        hashMap.put("userType ", "Google");
        hashMap.put("typingTo", "");
        hashMap.put("timestamp", timestamp);
        hashMap.put("onlineStatus", true);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(registerUserUid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess :Info Saved ...");
                        progressDialog.dismiss();

                        startActivity(new Intent(LoginOptionActivity.this, MainActivity.class));
                        finishAffinity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "OnFailure : ", e);
                        progressDialog.dismiss();
                        Utils.toast(LoginOptionActivity.this, "Failed to Save user info due to " + e.getMessage());
                    }
                });
    }
}
