package com.example.application.marketox;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.application.marketox.databinding.ActivityAdCreateBinding;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class AdCreateActivity extends AppCompatActivity {

    private ActivityAdCreateBinding binding;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelImagePicked> imagePickedArrayList;
    private AdapterImagesPicked adapterImagesPicked;

    private String brand = "", category = "", address = "";
    private String price = "", title = "", description = "", condition = "";
    private double latitude = 0, longitude = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAdCreateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        // Category & Condition
        binding.categoryAct.setAdapter(
                new ArrayAdapter<>(this, R.layout.row_category_act, Utils.categories));

        binding.conditionAct.setAdapter(
                new ArrayAdapter<>(this, R.layout.row_condition_act, Utils.conditions));

        // Image List
        imagePickedArrayList = new ArrayList<>();
        adapterImagesPicked = new AdapterImagesPicked(this, imagePickedArrayList);
        binding.imagesRv.setAdapter(adapterImagesPicked);

        binding.toolbarBackBtn.setOnClickListener(v -> onBackPressed());
        binding.toolbarAddImageBtn.setOnClickListener(v -> showImagePickOptions());
        binding.postAdBtn.setOnClickListener(v -> validateData());

        // Location picker
        binding.locationAct.setOnClickListener(v -> {
            Intent intent = new Intent(this, LocationPickerActivity.class);
            locationPickerLauncher.launch(intent);
        });
    }

    // ================= LOCATION =================

    private final ActivityResultLauncher<Intent> locationPickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

                            Intent data = result.getData();
                            latitude = data.getDoubleExtra("latitude", 0.0);
                            longitude = data.getDoubleExtra("longitude", 0.0);
                            address = data.getStringExtra("address");

                            binding.locationAct.setText(address);
                        }
                    });

    // ================= VALIDATION =================

    private void validateData() {

        brand = binding.brandEt.getText().toString().trim();
        category = binding.categoryAct.getText().toString().trim();
        address = binding.locationAct.getText().toString().trim();
        price = binding.priceEt.getText().toString().trim();
        title = binding.titleEt.getText().toString().trim();
        description = binding.descritpionEt.getText().toString().trim();
        condition = binding.conditionAct.getText().toString().trim();

        if (brand.isEmpty()) {
            binding.brandEt.setError("Enter Brand");
        } else if (category.isEmpty()) {
            binding.categoryAct.setError("Choose Category");
        } else if (condition.isEmpty()) {
            binding.conditionAct.setError("Choose Condition");
        } else if (title.isEmpty()) {
            binding.titleEt.setError("Enter Title");
        } else if (description.isEmpty()) {
            binding.descritpionEt.setError("Enter Description");
        } else if (imagePickedArrayList.isEmpty()) {
            Utils.toast(this, "Pick at least one image");
        } else {
            postAd();
        }
    }

    // ================= POST AD =================

    private void postAd() {

        progressDialog.setMessage("Posting Ad...");
        progressDialog.show();

        long timestamp = Utils.getTimestamp();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Ads");
        String adId = ref.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", firebaseAuth.getUid());
        hashMap.put("id", adId);
        hashMap.put("timestamp", timestamp);
        hashMap.put("brand", brand);
        hashMap.put("category", category);
        hashMap.put("address", address);
        hashMap.put("price", price);
        hashMap.put("title", title);
        hashMap.put("description", description);
        hashMap.put("condition", condition);
        hashMap.put("latitude", latitude);
        hashMap.put("longitude", longitude);
        hashMap.put("status", Utils.AD_STATUS_AVAILABLE);

        ref.child(adId).setValue(hashMap)
                .addOnSuccessListener(unused -> uploadImages(adId))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Utils.toast(this, "Failed: " + e.getMessage());
                });
    }

    // =================IMAGE UPLOAD =================

    private void uploadImages(String adId) {

        progressDialog.setMessage("Uploading Images...");

        int totalImages = imagePickedArrayList.size();
        int[] uploadedCount = {0};

        for (ModelImagePicked model : imagePickedArrayList) {

            String imageId = model.getId();
            String path = "Ads/" + imageId;

            StorageReference storageReference =
                    FirebaseStorage.getInstance().getReference(path);

            storageReference.putFile(model.getImageuri())
                    .addOnSuccessListener(taskSnapshot -> {

                        taskSnapshot.getStorage().getDownloadUrl()
                                .addOnSuccessListener(uri -> {

                                    HashMap<String, Object> imageMap = new HashMap<>();
                                    imageMap.put("imageUrl", uri.toString());
                                    imageMap.put("id", imageId);

                                    FirebaseDatabase.getInstance().getReference("Ads")
                                            .child(adId)
                                            .child("Images")
                                            .child(imageId)
                                            .setValue(imageMap);

                                    uploadedCount[0]++;

                                    // Finish only when ALL images uploaded
                                    if (uploadedCount[0] == totalImages) {
                                        progressDialog.dismiss();
                                        Utils.toast(this, "Ad Posted Successfully");
                                        finish();
                                    }
                                });

                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Utils.toast(this, "Image Upload Failed: " + e.getMessage());
                    });
        }
    }

    // ================= IMAGE PICK =================

    private void showImagePickOptions() {

        PopupMenu popupMenu = new PopupMenu(this, binding.toolbarAddImageBtn);
        popupMenu.getMenu().add("Gallery");

        popupMenu.setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
            return true;
        });

        popupMenu.show();
    }

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

                            Uri imageUri = result.getData().getData();
                            String id = "" + Utils.getTimestamp();

                            imagePickedArrayList.add(
                                    new ModelImagePicked(id, imageUri, null, false)
                            );

                            adapterImagesPicked.notifyDataSetChanged();
                        }
                    });
}