package com.example.application.marketox;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.application.marketox.databinding.ActivityProfileEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class ProfileEditActivity extends AppCompatActivity {

    private ActivityProfileEditBinding binding;
    private static final String TAG = "PROFILE_EDIT_TAG";
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private Uri imageUri = null;
    private String myUserType = "";

    private String name = "";
    private String dob = "";
    private String email = "";
    private String phoneCode = "";
    private String phoneNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        loadMyinfo();

        // Handle back button
        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Handle profile image pick
        binding.profileImagePickFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePickDialog();
            }
        });

        // FIX: Handle Update button click
        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });
    }

    private void validateData() {
        name = binding.nameEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        dob = binding.dobEt.getText().toString().trim();
        phoneCode = binding.countryCodePicker.getSelectedCountryCodeWithPlus();
        phoneNumber = binding.phoneNumberEt.getText().toString().trim();

        if (imageUri == null) {
            // No new image selected, just update database info
            updateInfoDb(null);
        } else {
            // New image selected, upload image first then update database
            updateProfileImageStorage();
        }
    }

    private void updateProfileImageStorage() {
        Log.d(TAG, "updateProfileImageStorage");
        progressDialog.setMessage("Uploading profile image...");
        progressDialog.show();

        String filePathAndName = "UserImages/" + "profile_" + firebaseAuth.getUid();

        StorageReference ref = FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putFile(imageUri)
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        progressDialog.setMessage("Uploading profile: " + (int) progress + "%");
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: Profile image uploaded");
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String uploadImageUrl = uriTask.getResult().toString();

                        if (uriTask.isSuccessful()) {
                            updateInfoDb(uploadImageUrl);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        progressDialog.dismiss();
                        Utils.toast(ProfileEditActivity.this, "Failed to upload image: " + e.getMessage());
                    }
                });
    }

    private void updateInfoDb(String imageUrl) {
        progressDialog.setMessage("Updating user info...");
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        // FIX: Removed the extra " " + prefix
        hashMap.put("name", name);
        hashMap.put("dob", dob);

        if (imageUrl != null) {
            hashMap.put("profileImageUrl", imageUrl);
        }

        if (!myUserType.equalsIgnoreCase("Phone")) {
            hashMap.put("email", email);
        }

        if (myUserType.equalsIgnoreCase("Email") || myUserType.equalsIgnoreCase("Google")) {
            hashMap.put("phoneCode", phoneCode);
            hashMap.put("phoneNumber", phoneNumber);
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Info updated");
                        progressDialog.dismiss();
                        Utils.toast(ProfileEditActivity.this, "Profile updated successfully");
                        finish(); // Optional: Close activity and return
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "OnFailure: ", e);
                        progressDialog.dismiss();
                        Utils.toast(ProfileEditActivity.this, "Failed to update: " + e.getMessage());
                    }
                });
    }

    private void loadMyinfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String dob = "" + snapshot.child("dob").getValue();
                        String email = "" + snapshot.child("email").getValue();
                        String name = "" + snapshot.child("name").getValue();
                        String phoneCode = "" + snapshot.child("phoneCode").getValue();
                        String phoneNumber = "" + snapshot.child("phoneNumber").getValue();
                        String profileImageUrl = "" + snapshot.child("profileImageUrl").getValue();
                        myUserType = "" + snapshot.child("userType").getValue();

                        if (myUserType.equalsIgnoreCase("Email") || myUserType.equalsIgnoreCase("Google")) {
                            binding.emailTil.setEnabled(false);
                            binding.emailEt.setEnabled(false);
                        } else {
                            binding.phoneNumberTil.setEnabled(false);
                            binding.phoneNumberEt.setEnabled(false);
                            binding.countryCodePicker.setEnabled(false);
                        }

                        binding.emailEt.setText(email);
                        binding.dobEt.setText(dob);
                        binding.nameEt.setText(name);
                        binding.phoneNumberEt.setText(phoneNumber);

                        try {
                            int phoneCodeInt = Integer.parseInt(phoneCode.replace("+", ""));
                            binding.countryCodePicker.setCountryForPhoneCode(phoneCodeInt);
                        } catch (Exception e) {
                            Log.d(TAG, "onDataChange phoneCode error: ", e);
                        }

                        try {
                            Glide.with(ProfileEditActivity.this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.ic_person2_white)
                                    .into(binding.profileIv);
                        } catch (Exception e) {
                            Log.e(TAG, "Glide error: ", e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void imagePickDialog() {
        PopupMenu popupMenu = new PopupMenu(this, binding.profileImagePickFab);
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Camera");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Gallery");
        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 1) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestCameraPermissions.launch(new String[]{Manifest.permission.CAMERA});
                    } else {
                        requestCameraPermissions.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
                    }
                } else if (id == 2) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pickImageGallery();
                    } else {
                        requestStoragePermissions.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                }
                return false;
            }
        });
    }

    private ActivityResultLauncher<String[]> requestCameraPermissions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    boolean areAllGranted = true;
                    for (Boolean isGranted : result.values()) {
                        areAllGranted = areAllGranted && isGranted;
                    }
                    if (areAllGranted) {
                        pickImageCamera();
                    } else {
                        Utils.toast(ProfileEditActivity.this, "Camera permissions denied");
                    }
                }
            }
    );

    private ActivityResultLauncher<String> requestStoragePermissions = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    if (isGranted) {
                        pickImageGallery();
                    } else {
                        Utils.toast(ProfileEditActivity.this, "Storage permissions denied");
                    }
                }
            }
    );

    private void pickImageCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "TEMP_TITLE");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMP_DESCRIPTION");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private void pickImageGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Glide.with(ProfileEditActivity.this).load(imageUri).into(binding.profileIv);
                    } else {
                        Utils.toast(ProfileEditActivity.this, "Cancelled");
                    }
                }
            }
    );

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        Glide.with(ProfileEditActivity.this).load(imageUri).into(binding.profileIv);
                    } else {
                        Utils.toast(ProfileEditActivity.this, "Cancelled");
                    }
                }
            }
    );
}